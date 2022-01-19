package org.ibp.api.java.inventory.manager;

import org.ibp.api.domain.location.LocationDto;
import org.ibp.api.domain.ontology.VariableDetails;

import java.io.File;
import java.util.List;

public interface LotTemplateExportService {

	File export(final String cropName, final List<LocationDto> locations, final List<VariableDetails> scales);
}
