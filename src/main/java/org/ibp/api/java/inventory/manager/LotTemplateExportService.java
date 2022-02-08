package org.ibp.api.java.inventory.manager;

import org.generationcp.middleware.api.location.LocationDTO;
import org.ibp.api.domain.ontology.VariableDetails;

import java.io.File;
import java.util.List;

public interface LotTemplateExportService {

	File export(String cropName, List<LocationDTO> locations, List<VariableDetails> scales);
}
