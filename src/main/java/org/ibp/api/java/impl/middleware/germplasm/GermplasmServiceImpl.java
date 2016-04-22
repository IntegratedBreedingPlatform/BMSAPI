
package org.ibp.api.java.impl.middleware.germplasm;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.domain.germplasm.DescendantTree;
import org.ibp.api.domain.germplasm.DescendantTreeTreeNode;
import org.ibp.api.domain.germplasm.GermplasmName;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.domain.germplasm.PedigreeTree;
import org.ibp.api.domain.germplasm.PedigreeTreeNode;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GermplasmServiceImpl implements GermplasmService {

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private PedigreeService pedigreeService;

	@Autowired
	private PedigreeDataManager pedigreeDataManager;

	@Autowired
	private CrossExpansionProperties crossExpansionProperties;

	@Autowired
	private LocationDataManager locationDataManger;

	@Autowired
	private GermplasmGroupingService germplasmGroupingService;

	@Override
	public List<GermplasmSummary> searchGermplasm(String searchText) {
		List<GermplasmSummary> results = new ArrayList<GermplasmSummary>();
		try {
			List<Germplasm> searchResults = this.germplasmDataManager.searchForGermplasm(searchText, Operation.LIKE, false, false);
			for (Germplasm germplasm : searchResults) {
				results.add(this.populateGermplasmSummary(germplasm));
			}
		} catch (MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
		return results;
	}

	private GermplasmSummary populateGermplasmSummary(Germplasm germplasm) throws MiddlewareQueryException {
		if (germplasm == null) {
			return null;
		}
		GermplasmSummary summary = new GermplasmSummary();
		summary.setGermplasmId(germplasm.getGid().toString());
		summary.setParent1Id(germplasm.getGpid1() != null && germplasm.getGpid1() != 0 ? germplasm.getGpid1().toString() : "Unknown");
		summary.setParent2Id(germplasm.getGpid2() != null && germplasm.getGpid2() != 0 ? germplasm.getGpid2().toString() : "Unknown");

		summary.setPedigreeString(this.pedigreeService.getCrossExpansion(germplasm.getGid(), crossExpansionProperties));

		// FIXME - select in a loop ... Middleware service should handle all this in main query.
		List<Name> namesByGID = this.germplasmDataManager.getNamesByGID(new Integer(germplasm.getGid()), null, null);
		List<GermplasmName> names = new ArrayList<GermplasmName>();
		for (Name gpName : namesByGID) {
			GermplasmName germplasmName = new GermplasmName();
			germplasmName.setName(gpName.getNval());
			UserDefinedField nameType = this.germplasmDataManager.getUserDefinedFieldByID(gpName.getTypeId());
			if (nameType != null) {
				germplasmName.setNameTypeCode(nameType.getFcode());
				germplasmName.setNameTypeDescription(nameType.getFname());
			}
			names.add(germplasmName);
		}
		summary.addNames(names);

		Method germplasmMethod = this.germplasmDataManager.getMethodByID(germplasm.getMethodId());
		if (germplasmMethod != null && germplasmMethod.getMname() != null) {
			summary.setBreedingMethod(germplasmMethod.getMname());
		}

		Location germplasmLocation = this.locationDataManger.getLocationByID(germplasm.getLocationId());
		if (germplasmLocation != null && germplasmLocation.getLname() != null) {
			summary.setLocation(germplasmLocation.getLname());
		}
		return summary;
	}

	@Override
	public GermplasmSummary getGermplasm(String germplasmId) {
		Germplasm germplasm;
		try {
			germplasm = this.germplasmDataManager.getGermplasmByGID(Integer.valueOf(germplasmId));
			return this.populateGermplasmSummary(germplasm);
		} catch (NumberFormatException | MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	void setGermplasmDataManager(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	void setPedigreeService(PedigreeService pedigreeService) {
		this.pedigreeService = pedigreeService;
	}

	void setLocationDataManger(LocationDataManager locationDataManger) {
		this.locationDataManger = locationDataManger;
	}

	@Override
	public PedigreeTree getPedigreeTree(String germplasmId, Integer levels) {

		if (levels == null) {
			levels = DEFAULT_PEDIGREE_LEVELS;
		}
		GermplasmPedigreeTree mwTree = this.pedigreeDataManager.generatePedigreeTree(Integer.valueOf(germplasmId), levels);

		PedigreeTree pedigreeTree = new PedigreeTree();
		pedigreeTree.setRoot(this.traversePopulate(mwTree.getRoot()));

		return pedigreeTree;
	}

	@Override
	public DescendantTree getDescendantTree(String germplasmId) {
		Germplasm germplasm = this.germplasmDataManager.getGermplasmByGID(Integer.valueOf(germplasmId));
		GermplasmPedigreeTree mwTree = this.germplasmGroupingService.getDescendantTree(germplasm);

		DescendantTree descendantTree = new DescendantTree();
		descendantTree.setRoot(this.traversePopulateDescendatTree(mwTree.getRoot()));

		return descendantTree;
	}

	private DescendantTreeTreeNode traversePopulateDescendatTree(GermplasmPedigreeTreeNode mwTreeNode) {
		DescendantTreeTreeNode treeNode = new DescendantTreeTreeNode();
		treeNode.setGermplasmId(mwTreeNode.getGermplasm().getGid());
		treeNode.setProgenitors(mwTreeNode.getGermplasm().getGnpgs());
		treeNode.setMethodId(mwTreeNode.getGermplasm().getMethodId());
		treeNode.setParent1Id(mwTreeNode.getGermplasm().getGpid1());
		treeNode.setParent2Id(mwTreeNode.getGermplasm().getGpid2());
		treeNode.setManagementGroupId(mwTreeNode.getGermplasm().getMgid());

		Name preferredName = mwTreeNode.getGermplasm().findPreferredName();
		treeNode.setName(preferredName != null ? preferredName.getNval() : null);

		List<DescendantTreeTreeNode> nodeChildren = new ArrayList<>();
		for (GermplasmPedigreeTreeNode mwChild : mwTreeNode.getLinkedNodes()) {
			nodeChildren.add(this.traversePopulateDescendatTree(mwChild));
		}
		treeNode.setChildren(nodeChildren);
		return treeNode;
	}

	private PedigreeTreeNode traversePopulate(GermplasmPedigreeTreeNode mwTreeNode) {
		PedigreeTreeNode treeNode = new PedigreeTreeNode();
		treeNode.setGermplasmId(mwTreeNode.getGermplasm().getGid().toString());
		treeNode.setName(mwTreeNode.getGermplasm().getPreferredName() != null ? mwTreeNode.getGermplasm().getPreferredName().getNval()
				: null);

		List<PedigreeTreeNode> nodeParents = new ArrayList<>();
		for (GermplasmPedigreeTreeNode mwParent : mwTreeNode.getLinkedNodes()) {
			nodeParents.add(this.traversePopulate(mwParent));
		}
		treeNode.setParents(nodeParents);
		return treeNode;
	}
}
