package org.ibp.api.java.inventory.manager;

import org.generationcp.middleware.api.location.LocationDTO;
import org.ibp.api.domain.ontology.VariableDetails;

import java.io.File;
import java.util.List;

public interface LotTemplateExportService {

	File export(final List<LocationDTO> locations, final List<VariableDetails> scales);

}
