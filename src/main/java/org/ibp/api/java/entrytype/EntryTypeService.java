package org.ibp.api.java.entrytype;

import org.generationcp.middleware.domain.dms.Enumeration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EntryTypeService {

	List<Enumeration> getEntryTypes(String programUuid);

}
