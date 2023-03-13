package org.ibp.api.java.impl.middleware.dataset;

import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@RunWith(MockitoJUnitRunner.class)
public class DatasetKsuExcelGeneratorTest {

	@InjectMocks
	private DatasetKsuExcelGenerator generator;

	@Test
	public void testGenerateSingleInstanceFile() throws IOException {
		final String filename = "filename";
		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setName("DATASET");
		final File file = this.generator.generateSingleInstanceFile(1, datasetDTO, new ArrayList<MeasurementVariable>(), new ArrayList<ObservationUnitRow>(),
			new HashMap<>(), filename, null);
		Assert.assertEquals(filename, file.getName());
	}

	@Test
	public void testGenerateTraitAndSelectionVariablesFile() throws IOException {
		final String filename = "filename";
		final File file = this.generator.generateTraitAndSelectionVariablesFile(new ArrayList<String[]>(), filename);
		Assert.assertEquals(filename, file.getName());
	}

}
