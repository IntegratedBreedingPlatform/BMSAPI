package org.ibp.api.java.germplasm;

import java.io.File;

public interface GermplasmTemplateExportService {

	File export(String cropName, String programUUID, boolean updateFormat);

}
