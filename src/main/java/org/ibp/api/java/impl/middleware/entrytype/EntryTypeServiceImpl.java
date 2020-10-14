package org.ibp.api.java.impl.middleware.entrytype;

import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.OntologyService;
import org.ibp.api.java.entrytype.EntryTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


@Service
@Transactional
public class EntryTypeServiceImpl implements EntryTypeService {

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private OntologyService ontologyService;

	@Override
	public List<Enumeration> getEntryTypes(final String programUuid) {
		return this.ontologyDataManager.getStandardVariable(TermId.ENTRY_TYPE.getId(), programUuid).getEnumerations();
	}

	@Override
	public void addEntryType(final String programUuid, final Enumeration entryType) {
		// TODO add validation
		final StandardVariable stdVar =
			this.ontologyService.getStandardVariable(TermId.ENTRY_TYPE.getId(), programUuid);
		this.ontologyService.saveOrUpdateStandardVariableEnumeration(stdVar, entryType);
	}

	@Override
	public void updateEntryType(final String programUuid, final Enumeration entryType) {
		// TODO add validation
		final StandardVariable stdVar =
			this.ontologyService.getStandardVariable(TermId.ENTRY_TYPE.getId(), programUuid);
		this.ontologyService.saveOrUpdateStandardVariableEnumeration(stdVar, entryType);
	}

	@Override
	public void deleteEntryType(final Integer entryTypeId) {
		// TODO add validation
		this.ontologyService.deleteStandardVariableValidValue(TermId.ENTRY_TYPE.getId(), entryTypeId);
	}

}
