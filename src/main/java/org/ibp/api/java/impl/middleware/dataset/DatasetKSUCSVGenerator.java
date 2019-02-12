package org.ibp.api.java.impl.middleware.dataset;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.java.dataset.DatasetFileGenerator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class DatasetKSUCSVGenerator extends DatasetCSVGenerator implements DatasetFileGenerator {

	@Override
	public File generateMultiInstanceFile(final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap, final List<MeasurementVariable> columns,
		final String fileNameFullPath) throws IOException {
		//Do nothing. Implement for the singleFile download KSU CSV option
		return null;
	}
}
