package org.ibp.api.java.germplasm;

import org.generationcp.middleware.pojos.Method;
import org.ibp.api.domain.germplasm.GermplasmName;
import org.ibp.api.domain.location.LocationDto;
import org.ibp.api.domain.ontology.VariableDetails;

import java.io.File;
import java.util.List;

public interface GermplasmTemplateExportService {

	File export(final String cropName, final String programUUID);

}
