package org.ibp.api.rest.labelprinting;

import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.filegenerator.CSVLabelsFileGenerator;
import org.ibp.api.rest.labelprinting.filegenerator.LabelsFileGenerator;
import org.ibp.api.rest.labelprinting.filegenerator.PDFLabelsFileGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import javax.servlet.http.HttpServletRequest;

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

	@Mock
	private HttpServletRequest request;

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

	@Test(expected = AccessDeniedException.class)
	public void testGetLabelPrintingStrategyUnauthorized() {
		this.labelPrintingResource.setRequest(this.request);
		Mockito.when(this.request.isUserInRole(PermissionsEnum.ADMIN.name())).thenReturn(false);
		Mockito.when(this.request.isUserInRole(PermissionsEnum.CROP_MANAGEMENT.name())).thenReturn(false);
		Mockito.when(this.request.isUserInRole(PermissionsEnum.MANAGE_INVENTORY.name())).thenReturn(false);
		Mockito.when(this.request.isUserInRole(PermissionsEnum.MANAGE_LOTS.name())).thenReturn(false);
		Mockito.when(this.request.isUserInRole(PermissionsEnum.LOT_LABEL_PRINTING.name())).thenReturn(false);
		final LabelPrintingStrategy labelPrintingStrategy = this.labelPrintingResource.getLabelPrintingStrategy("Lot");
	}

	@Test
	public void testGetLabelPrintingStrategyCropManagement() {
		this.labelPrintingResource.setRequest(this.request);
		Mockito.when(this.request.isUserInRole(PermissionsEnum.ADMIN.name())).thenReturn(false);
		Mockito.when(this.request.isUserInRole(PermissionsEnum.CROP_MANAGEMENT.name())).thenReturn(true);
		Exception e = null;
		try {
			final LabelPrintingStrategy labelPrintingStrategy = this.labelPrintingResource.getLabelPrintingStrategy("Lot");
		}catch (final AccessDeniedException ex) {
			e = ex;
		}
		Assert.assertNull(e);
	}
}
