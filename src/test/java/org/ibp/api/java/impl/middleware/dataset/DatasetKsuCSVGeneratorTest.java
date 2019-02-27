package org.ibp.api.java.impl.middleware.dataset;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

@RunWith(MockitoJUnitRunner.class)
public class DatasetKsuCSVGeneratorTest {
	private DatasetKsuCSVGenerator generator;

	@Test
	public void testGenerateTraitAndSelectionVariablesFile() throws IOException {
		this.generator = new DatasetKsuCSVGenerator();
		final String filename = "filename";
		final File
			file = this.generator.generateTraitAndSelectionVariablesFile(new ArrayList<String[]>(), filename);
		Assert.assertEquals(filename, file.getName());
	}
}
