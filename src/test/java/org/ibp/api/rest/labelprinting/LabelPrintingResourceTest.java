package org.ibp.api.rest.labelprinting;

import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.rest.common.FileType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LabelPrintingResourceTest {

	@Mock
	private LabelPrintingStrategy subObservationDatasetLabelPrinting;

	@Mock
	private CSVLabelsFileGenerator csvLabelsFileGenerator;

	@Mock
	private PDFLabelsFileGenerator pdfLabelsFileGenerator;

	@InjectMocks
	private LabelPrintingResource labelPrintingResource;

	@Test
	public void testGetLabelsFileGenerator() {
		Mockito.when(subObservationDatasetLabelPrinting.getSupportedFileTypes()).thenReturn(SubObservationDatasetLabelPrinting.SUPPORTED_FILE_TYPES);
		try {
			this.labelPrintingResource.getLabelsFileGenerator(FileType.XLS.getExtension(), this.subObservationDatasetLabelPrinting);
			Assert.fail("Should throw a NotSupportedException");
		} catch (NotSupportedException ex) {
		}

		LabelsFileGenerator fileGenerator = this.labelPrintingResource.getLabelsFileGenerator(FileType.CSV.getExtension(), this.subObservationDatasetLabelPrinting);
		Assert.assertEquals(this.csvLabelsFileGenerator, fileGenerator);

		fileGenerator = this.labelPrintingResource.getLabelsFileGenerator(FileType.PDF.getExtension(), this.subObservationDatasetLabelPrinting);
		Assert.assertEquals(this.pdfLabelsFileGenerator, fileGenerator);

	}
}
