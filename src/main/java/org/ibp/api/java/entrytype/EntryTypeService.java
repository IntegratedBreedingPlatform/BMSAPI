package org.ibp.api.java.entrytype;

import org.generationcp.middleware.domain.dms.Enumeration;

import java.util.List;

public interface EntryTypeService {

	List<Enumeration> getEntryTypes(String programUuid);

}
