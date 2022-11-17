package org.ibp.api.rest.labelprinting;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class LotLabelPrintingTest {

	final static Integer GID = 11;

	@InjectMocks
	private LotLabelPrinting lotLabelPrinting;

	@Test
	public void testGetDataRow_For_ShortAttributeValues() {
		final Integer attributeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<String> keys = new HashSet<>(Collections.singletonList("VARIABLE_" + attributeId));
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		attributeValues.put(GID, new HashMap<>());
		final String attributeValue = RandomStringUtils.randomAlphanumeric(100);
		attributeValues.get(GID).put(attributeId, attributeValue);
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setFileType(FileType.PDF);
		final ExtendedLotDto lotDto = new ExtendedLotDto();
		lotDto.setGid(GID);
		lotDto.setGermplasmUUID(RandomStringUtils.randomAlphanumeric(36));
		final Map<String, String> dataRow =
			this.lotLabelPrinting.getDataRow(labelsGeneratorInput, keys, lotDto, attributeValues, attributeValues, new HashMap<>());
		Assert.assertEquals(1, dataRow.keySet().size());
		Assert.assertEquals(attributeValue, dataRow.get("VARIABLE_" + attributeId));
	}

	@Test
	public void testGetDataRow_For_LongAttributeValuesNotTruncated_WhenCSVFileType() {
		final Integer attributeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<String> keys = new HashSet<>(Collections.singletonList("VARIABLE_" + attributeId));
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		attributeValues.put(GID, new HashMap<>());
		final String attributeValue = RandomStringUtils.randomAlphanumeric(4000);
		attributeValues.get(GID).put(attributeId, attributeValue);
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setFileType(FileType.CSV);
		final ExtendedLotDto lotDto = new ExtendedLotDto();
		lotDto.setGid(GID);
		lotDto.setGermplasmUUID(RandomStringUtils.randomAlphanumeric(36));
		final Map<String, String> dataRow =
			this.lotLabelPrinting.getDataRow(labelsGeneratorInput, keys, lotDto, attributeValues, attributeValues, new HashMap<>());
		Assert.assertEquals(1, dataRow.keySet().size());
		// Verify that attribute values is not truncated for CSV file type
		Assert.assertEquals(attributeValue, dataRow.get("VARIABLE_" + attributeId));
	}

	@Test
	public void testGetDataRow_For_LongAttributeValuesNotTruncated_WhenXLSFileType() {
		final Integer attributeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<String> keys = new HashSet<>(Collections.singletonList("VARIABLE_" + attributeId));
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		attributeValues.put(GID, new HashMap<>());
		final String attributeValue = RandomStringUtils.randomAlphanumeric(4000);
		attributeValues.get(GID).put(attributeId, attributeValue);
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setFileType(FileType.XLS);
		final ExtendedLotDto lotDto = new ExtendedLotDto();
		lotDto.setGid(GID);
		lotDto.setGermplasmUUID(RandomStringUtils.randomAlphanumeric(36));
		final Map<String, String> dataRow =
			this.lotLabelPrinting.getDataRow(labelsGeneratorInput, keys, lotDto, attributeValues, attributeValues, new HashMap<>());
		Assert.assertEquals(1, dataRow.keySet().size());
		// Verify that attribute values is not truncated for XLS file type
		Assert.assertEquals(attributeValue, dataRow.get("VARIABLE_" + attributeId));
	}

	@Test
	public void testGetDataRow_For_TruncateLongAttributeValues_WhenPDFExportType() {
		final Integer attributeId = Integer.valueOf(RandomStringUtils.randomNumeric(5));
		final Set<String> keys = new HashSet<>(Collections.singletonList("VARIABLE_" + attributeId));
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		attributeValues.put(GID, new HashMap<>());
		final String attributeValue = RandomStringUtils.randomAlphanumeric(4000);
		attributeValues.get(GID).put(attributeId, attributeValue);
		final LabelsGeneratorInput labelsGeneratorInput = new LabelsGeneratorInput();
		labelsGeneratorInput.setFileType(FileType.PDF);
		final ExtendedLotDto lotDto = new ExtendedLotDto();
		lotDto.setGid(GID);
		lotDto.setGermplasmUUID(RandomStringUtils.randomAlphanumeric(36));
		final Map<String, String> dataRow =
			this.lotLabelPrinting.getDataRow(labelsGeneratorInput, keys, lotDto, attributeValues, attributeValues, new HashMap<>());
		Assert.assertEquals(1, dataRow.keySet().size());
		// Verify that attribute values is truncated for PDF file type
		Assert.assertEquals(attributeValue.substring(0, GermplasmLabelPrinting.ATTRIBUTE_DISPLAY_MAX_LENGTH) + "...", dataRow.get("VARIABLE_" + attributeId));
	}

}
