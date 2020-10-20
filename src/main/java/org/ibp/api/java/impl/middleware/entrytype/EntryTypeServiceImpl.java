package org.ibp.api.java.impl.middleware.entrytype;

import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
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

	@Override
	public List<Enumeration> getEntryTypes(final String programUuid) {
		return this.ontologyDataManager.getStandardVariable(TermId.ENTRY_TYPE.getId(), programUuid).getEnumerations();
	}
}
