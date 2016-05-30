
package org.ibp.api.java.impl.middleware.germplasm;

import java.util.List;

import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmNameType;
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
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.domain.germplasm.PedigreeTree;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class GermplasmServiceImplTest {

	private GermplasmServiceImpl germplasmServiceImpl;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private PedigreeService pedigreeService;

	@Mock
	private LocationDataManager locationDataManger;

    @Mock
    private PedigreeDataManager pedigreeDataManager;

    @Mock
    private GermplasmGroupingService germplasmGroupingService;

    @Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		this.germplasmServiceImpl = new GermplasmServiceImpl();
		this.germplasmServiceImpl.setGermplasmDataManager(this.germplasmDataManager);
		this.germplasmServiceImpl.setPedigreeService(this.pedigreeService);
		this.germplasmServiceImpl.setLocationDataManger(this.locationDataManger);
        this.germplasmServiceImpl.setPedigreeDataManager(this.pedigreeDataManager);
        this.germplasmServiceImpl.setGermplasmGroupingService(this.germplasmGroupingService);
	}

	@Test
	public void testSearchGermplasm() throws MiddlewareQueryException {

		Germplasm gp = new Germplasm();
		gp.setGid(3);
		gp.setGpid1(1);
		gp.setGpid2(2);
		gp.setMethodId(1);
		gp.setLocationId(1);

		List<Germplasm> middlewareSearchResults = Lists.newArrayList(gp);
		Mockito.when(this.germplasmDataManager.searchForGermplasm(Mockito.any(GermplasmSearchParameter.class)))
				.thenReturn(middlewareSearchResults);
		String gpPedigree = "CML1/CML2";
		Mockito.when(this.pedigreeService.getCrossExpansion(Matchers.anyInt(), Matchers.any(CrossExpansionProperties.class))).thenReturn(
				gpPedigree);

		Name gpName = new Name();
		gpName.setGermplasmId(gp.getGid());
		gpName.setNval("CML1");
		List<Name> gpNames = Lists.newArrayList(gpName);
		Mockito.when(this.germplasmDataManager.getNamesByGID(Matchers.anyInt(), Matchers.anyInt(), Matchers.any(GermplasmNameType.class)))
				.thenReturn(gpNames);

		Method gpMethod = new Method();
		gpMethod.setMname("Backcross");
		Mockito.when(this.germplasmDataManager.getMethodByID(Matchers.anyInt())).thenReturn(gpMethod);

		Location gpLocation = new Location();
		gpLocation.setLname("Mexico");
		Mockito.when(this.locationDataManger.getLocationByID(Matchers.anyInt())).thenReturn(gpLocation);

        UserDefinedField userDefinedField = new UserDefinedField();
        userDefinedField.setFcode("Fcode");
        userDefinedField.setFname("Description");
        Mockito.when(this.germplasmDataManager.getUserDefinedFieldByID(gpName.getTypeId())).thenReturn(userDefinedField);

		List<GermplasmSummary> germplasmSummaries = this.germplasmServiceImpl.searchGermplasm("CML", 1, 20);
		Assert.assertTrue(!germplasmSummaries.isEmpty());

		Assert.assertEquals(gp.getGid().toString(), germplasmSummaries.get(0).getGermplasmId());
		Assert.assertEquals(gp.getGpid1().toString(), germplasmSummaries.get(0).getParent1Id());
		Assert.assertEquals(gp.getGpid2().toString(), germplasmSummaries.get(0).getParent2Id());
		Assert.assertEquals(gpPedigree, germplasmSummaries.get(0).getPedigreeString());
		Assert.assertEquals(gpMethod.getMname(), germplasmSummaries.get(0).getBreedingMethod());
		Assert.assertEquals(gpLocation.getLname(), germplasmSummaries.get(0).getLocation());
		Assert.assertEquals(gpName.getNval(), germplasmSummaries.get(0).getNames().get(0).getName());
	}

    @Test(expected = ApiRuntimeException.class)
    public void testSearchGermplasmException() throws MiddlewareQueryException {

        Germplasm gp = new Germplasm();
        gp.setGid(3);
        gp.setGpid1(1);
        gp.setGpid2(2);
        gp.setMethodId(1);
        gp.setLocationId(1);

        Mockito.when(this.germplasmDataManager.searchForGermplasm(Mockito.any(GermplasmSearchParameter.class))).thenThrow(new MiddlewareQueryException("MiddleQuery Exception"));
        this.germplasmServiceImpl.searchGermplasm("CML", 1, 20);
    }

    @Test
    public void testGetGermplasm() throws Exception{

        Germplasm germplasm = new Germplasm();
        germplasm.setGid(3);
        germplasm.setGpid1(1);
        germplasm.setGpid2(2);
        germplasm.setMethodId(1);
        germplasm.setLocationId(1);

        Mockito.when(this.germplasmDataManager.getGermplasmByGID(germplasm.getGid())).thenReturn(germplasm);

        GermplasmSummary summary = this.germplasmServiceImpl.getGermplasm(String.valueOf(germplasm.getGid()));

        Assert.assertNotNull(summary);
        Assert.assertEquals(summary.getGermplasmId() , String.valueOf(germplasm.getGid()));
        Assert.assertEquals(summary.getParent1Id() , String.valueOf(germplasm.getGpid1()));
        Assert.assertEquals(summary.getParent2Id() , String.valueOf(germplasm.getGpid2()));
    }

    @Test(expected = ApiRuntimeException.class)
    public void testGetGermplasmWithWrongInput() throws Exception{

        Germplasm germplasm = new Germplasm();
        germplasm.setGid(null);

        Mockito.when(this.germplasmDataManager.getGermplasmByGID(germplasm.getGid())).thenReturn(germplasm);

        this.germplasmServiceImpl.getGermplasm(String.valueOf(germplasm.getGid()));
    }

    @Test
    public void testGetNullGermplasm() throws Exception{

        Germplasm germplasm = new Germplasm();
        germplasm.setGid(2);

        GermplasmSummary summary = this.germplasmServiceImpl.getGermplasm(String.valueOf(germplasm.getGid()));

        Assert.assertEquals(null , summary);

    }

    @Test
    public void testGeneratePedigreeTree() throws Exception{

        Germplasm germplasm = new Germplasm();
        germplasm.setGid(3);
        germplasm.setGpid1(1);
        germplasm.setGpid2(2);
        germplasm.setMethodId(1);
        germplasm.setLocationId(1);

        GermplasmPedigreeTree tree = new GermplasmPedigreeTree();
        String mainNodeName = "Test Germplasm", firstChildName = "Test Gerplasm first child", secondChildName = "Test Gerplasm second child";
        Integer mainNodeGid = 3, firstGid = 2, secondGid = 1;
        GermplasmPedigreeTreeNode rootNode = this.createNode(mainNodeGid, mainNodeName, firstGid, firstChildName, secondGid, secondChildName);
        tree.setRoot(rootNode);

        Mockito.when(this.pedigreeDataManager.generatePedigreeTree(germplasm.getGid(), GermplasmService.DEFAULT_PEDIGREE_LEVELS)).thenReturn(tree);

        PedigreeTree pedigreeTree = this.germplasmServiceImpl.getPedigreeTree(String.valueOf(germplasm.getGid()) , null);

        Assert.assertNotNull(pedigreeTree);
        Assert.assertEquals(pedigreeTree.getRoot().getGermplasmId() , String.valueOf(germplasm.getGid()));
        Assert.assertEquals(pedigreeTree.getRoot().getParents().get(0).getGermplasmId(), String.valueOf(firstGid));
        Assert.assertEquals(pedigreeTree.getRoot().getParents().get(1).getGermplasmId(), String.valueOf(secondGid));
        Assert.assertEquals(pedigreeTree.getRoot().getParents().get(0).getName(), firstChildName);
        Assert.assertEquals(pedigreeTree.getRoot().getParents().get(1).getName(), secondChildName);
    }

    private GermplasmPedigreeTreeNode createNode(Integer gid, String name, Integer parent1Gid, String parent1Name, Integer parent2Gid,
                                                 String parent2Name) {
        GermplasmPedigreeTreeNode mainNode = new GermplasmPedigreeTreeNode();
        Germplasm germplasm = new Germplasm();
        germplasm.setGid(gid);
        Name preferredName = new Name();
        preferredName.setNval(name);
        germplasm.setPreferredName(preferredName);
        mainNode.setGermplasm(germplasm);

        GermplasmPedigreeTreeNode parent1Node = new GermplasmPedigreeTreeNode();
        Germplasm germplasmParent1 = new Germplasm();
        germplasmParent1.setGid(parent1Gid);
        Name germplasmParent1Name = new Name();
        germplasmParent1Name.setNval(parent1Name);
        germplasmParent1.setPreferredName(germplasmParent1Name);
        parent1Node.setGermplasm(germplasmParent1);

        GermplasmPedigreeTreeNode parent2Node = new GermplasmPedigreeTreeNode();
        Germplasm germplasmParent2 = new Germplasm();
        germplasmParent2.setGid(parent2Gid);
        Name germplasmParent2Name = new Name();
        germplasmParent2Name.setNval(parent2Name);
        germplasmParent2.setPreferredName(germplasmParent2Name);
        parent2Node.setGermplasm(germplasmParent2);

        mainNode.setLinkedNodes(Lists.newArrayList(parent1Node, parent2Node));

        return mainNode;
    }

    @Test
    public void testGetDescendantTree() throws Exception{

        Germplasm germplasm = new Germplasm();
        germplasm.setGid(3);
        germplasm.setGpid1(1);
        germplasm.setGpid2(2);
        germplasm.setMethodId(1);
        germplasm.setLocationId(1);

        GermplasmPedigreeTree tree = new GermplasmPedigreeTree();
        String mainNodeName = "Test Germplasm", parent1Name = "Test Gerplasm Male Parent", parent2Name = "Test Gerplasm Female Parent";
        Integer mainNodeGid = 3, parent1Gid = 2, parent2Gid = 1;
        GermplasmPedigreeTreeNode rootNode = this.createNode(mainNodeGid, mainNodeName, parent1Gid, parent1Name, parent2Gid, parent2Name);
        tree.setRoot(rootNode);

        Mockito.when(this.germplasmDataManager.getGermplasmByGID(germplasm.getGid())).thenReturn(germplasm);
        Mockito.when(this.germplasmGroupingService.getDescendantTree(germplasm)).thenReturn(tree);

        DescendantTree descendantTree = this.germplasmServiceImpl.getDescendantTree(String.valueOf(germplasm.getGid()));

        Assert.assertNotNull(descendantTree);
        Assert.assertEquals(descendantTree.getRoot().getGermplasmId() , mainNodeGid);
        Assert.assertEquals(descendantTree.getRoot().getChildren().get(0).getGermplasmId() , parent1Gid);
        Assert.assertEquals(descendantTree.getRoot().getChildren().get(1).getGermplasmId() , parent2Gid);
    }

}
