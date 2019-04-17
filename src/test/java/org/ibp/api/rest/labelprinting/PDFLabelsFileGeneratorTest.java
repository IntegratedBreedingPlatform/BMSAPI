package org.ibp.api.rest.labelprinting;

import com.google.zxing.common.BitMatrix;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.domain.oms.TermId;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.template.LabelPaper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class PDFLabelsFileGeneratorTest {

	@InjectMocks
	private PDFLabelsFileGenerator pdfLabelsFileGenerator;


	@Test
	public void testGetBarcodeLabel() {
		final Map<Integer, String> labels = this.mockLabels();
		final List<Integer> barcodeFields = this.mockBarcodeFields();
		final Map<Integer, Field> keyFieldMap = this.mockKeyFieldMap();
		String expectedBarcodeWithoutLabel = "";
		String expectedBarcodeWithLabel = "";
		for(Integer barcodeField: barcodeFields) {
			if(!expectedBarcodeWithLabel.isEmpty()) {
				expectedBarcodeWithLabel += PDFLabelsFileGenerator.BARCODE_SEPARATOR;
				expectedBarcodeWithoutLabel += PDFLabelsFileGenerator.BARCODE_SEPARATOR;
			}

			expectedBarcodeWithLabel += keyFieldMap.get(barcodeField).getName() + PDFLabelsFileGenerator.FIELDNAME_VALUE_SEPARATOR + labels.get(barcodeField);
			expectedBarcodeWithoutLabel += labels.get(barcodeField);
		}
		final String barcodeWithoutLabel = this.pdfLabelsFileGenerator.getBarcodeLabel(labels, barcodeFields, keyFieldMap, false);
		final String barcodeWithLabel = this.pdfLabelsFileGenerator.getBarcodeLabel(labels, barcodeFields, keyFieldMap, true);
		Assert.assertEquals(expectedBarcodeWithLabel, barcodeWithLabel);
		Assert.assertEquals(expectedBarcodeWithoutLabel, barcodeWithoutLabel);
	}

	@Test
	public void testGenerateLabelText() {
		final Map<Integer, String> labels = this.mockLabels();
		final Map<Integer, Field> keyFieldMap = this.mockKeyFieldMap();
		final String expectedLabelText = keyFieldMap.get(TermId.ENTRY_CODE.getId()).getName() + PDFLabelsFileGenerator.FIELDNAME_VALUE_SEPARATOR + labels.get(TermId.ENTRY_CODE.getId());
		final String labelText = this.pdfLabelsFileGenerator.generateLabelText(labels, Arrays.asList(TermId.ENTRY_CODE.getId()), keyFieldMap, 0);
		Assert.assertEquals(expectedLabelText, labelText);
	}

	@Test
	public void testTruncateBarcodeLabelForCode() {
		final String label = RandomStringUtils.random(266);
		Assert.assertEquals(266, label.length());
		final String truncatedLabel = this.pdfLabelsFileGenerator.truncateBarcodeLabelForCode(label);
		Assert.assertEquals(PDFLabelsFileGenerator.BARCODE_LABEL_LIMIT, truncatedLabel.length());
	}

	@Test
	public void testEncodeBarcode() {
		final String label = "LABEL";
		final BitMatrix image = this.pdfLabelsFileGenerator.encodeBarcode(label);
		Assert.assertNotNull(image);
	}

	@Test
	public void testGetDocument() throws DocumentException, IOException {
		LabelPaper paper = LabelPaper.PAPER_3_BY_7_A4;
		final FileOutputStream fileOutputStream = new FileOutputStream("temp");
		Document document = this.pdfLabelsFileGenerator.getDocument(fileOutputStream, paper, 1);
		Assert.assertNotNull(document);
		Assert.assertEquals(PageSize.A4, document.getPageSize());

		paper = LabelPaper.PAPER_3_BY_7_LETTER;
		document = this.pdfLabelsFileGenerator.getDocument(fileOutputStream, paper, 2);
		Assert.assertEquals(PageSize.LETTER, document.getPageSize());
		fileOutputStream.close();
	}

	@Test
	public void testGetBarcodeImage() throws BadElementException, IOException {
		final List<File> files = new ArrayList<>();
		final com.lowagie.text.Image image = this.pdfLabelsFileGenerator.getBarcodeImage(files, "label");
		Assert.assertNotNull(image);
		files.get(0).delete();
	}

	@Test
	public void testGenerate() throws IOException{
		final LabelsGeneratorInput input = new LabelsGeneratorInput();
		input.setAllAvailablefields(this.mockAvailableFields());
		input.setFileName("filename");
		input.setBarcodeRequired(false);
		input.setNumberOfRowsPerPageOfLabel("7");
		input.setSizeOfLabelSheet("1");
		final LabelsData data = new LabelsData(TermId.OBS_UNIT_ID.getId(), Arrays.asList(this.mockLabels()));
		final File file = this.pdfLabelsFileGenerator.generate(input, data);
		Assert.assertNotNull(file);
		Assert.assertEquals("filename.pdf", file.getName());
		file.delete();
	}

	private Set<Field> mockAvailableFields() {
		final Set<Field> availableFields = new HashSet<>();
		availableFields.add(new Field(TermId.ENTRY_CODE.getId(), TermId.ENTRY_CODE.name()));
		availableFields.add(new Field(TermId.GID.getId(), TermId.GID.name()));
		availableFields.add(new Field(TermId.OBS_UNIT_ID.getId(), TermId.OBS_UNIT_ID.name()));
		return availableFields;
	}

	private Map<Integer, String> mockLabels() {
		final Map<Integer, String> labels = new HashMap<>();
		labels.put(TermId.ENTRY_CODE.getId(), "1");
		labels.put(TermId.GID.getId(), "2");
		labels.put(TermId.OBS_UNIT_ID.getId(), "123");
		return labels;
	}

	private List<Integer> mockBarcodeFields() {
		final List<Integer> barcodeFields = new ArrayList<>();
		barcodeFields.add(TermId.ENTRY_CODE.getId());
		barcodeFields.add(TermId.GID.getId());
		barcodeFields.add(TermId.OBS_UNIT_ID.getId());
		return barcodeFields;
	}

	private Map<Integer, Field> mockKeyFieldMap() {
		final Map<Integer, Field> keyFieldMap = new HashMap<>();
		keyFieldMap.put(TermId.ENTRY_CODE.getId(), new Field(TermId.ENTRY_CODE.getId(), TermId.ENTRY_CODE.name()));
		keyFieldMap.put(TermId.GID.getId(), new Field(TermId.GID.getId(), TermId.GID.name()));
		keyFieldMap.put(TermId.OBS_UNIT_ID.getId(), new Field(TermId.OBS_UNIT_ID.getId(), TermId.OBS_UNIT_ID.name()));
		return keyFieldMap;
	}
}
