package org.ibp.api.java.germplasm;

import java.io.File;

public interface GermplasmListTemplateExportService {

	File export(String cropName, String programUUID, boolean isGermplasmListUpdateFormat);
}
