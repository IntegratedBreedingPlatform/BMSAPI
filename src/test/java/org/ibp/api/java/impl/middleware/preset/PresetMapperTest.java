package org.ibp.api.java.impl.middleware.preset;

import static org.junit.Assert.*;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.rest.preset.domain.FilePresetConfigurationDTO;
import org.ibp.api.rest.preset.domain.LabelPrintingPresetDTO;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

public class PresetMapperTest extends ApiUnitTestBase {

	private String name;

	private String programUUID;

	private String toolSection;

	private Integer toolId;

	private String type;

	private List<List<Integer>> selectedField;

	private LabelPrintingPresetDTO.BarcodeSetting barcodeSetting;

	private FilePresetConfigurationDTO filePresetConfigurationDTO;

	private String configuration;

	@Autowired
	private PresetMapper presetMapper;

	@Before
	public void init() {
		programUUID = RandomStringUtils.randomAlphabetic(10);
		name = RandomStringUtils.randomAlphabetic(10);
		toolSection = RandomStringUtils.randomAlphabetic(10);
		toolId = RandomUtils.nextInt();
		type = "LabelPrintingPreset";
		selectedField = Arrays.asList(Arrays.asList(4, 13));
		barcodeSetting =
			new LabelPrintingPresetDTO.BarcodeSetting(Boolean.TRUE, Boolean.FALSE, Arrays.asList(2));
		filePresetConfigurationDTO = new FilePresetConfigurationDTO();
		filePresetConfigurationDTO.setOutputType("csv");
		configuration =
			"{\"type\":\"LabelPrintingPreset\",\"selectedFields\":[[4,13]],\"barcodeSetting\":{\"barcodeNeeded\":true,\"automaticBarcode\":false,\"barcodeFields\":[2]},\"fileConfiguration\":{\"outputType\":\"csv\"}}";
	}

	@Test
	public void testMapProgramPreset() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setName(name);
		presetDTO.setToolSection(toolSection);
		presetDTO.setToolId(toolId);
		presetDTO.setProgramUUID(programUUID);
		presetDTO.setType(type);
		presetDTO.setSelectedFields(selectedField);
		presetDTO.setBarcodeSetting(barcodeSetting);
		presetDTO.setFileConfiguration(filePresetConfigurationDTO);

		final ProgramPreset programPreset = presetMapper.map(presetDTO);
		assertEquals(programPreset.getName(), presetDTO.getName());
		assertEquals(programPreset.getToolId(), presetDTO.getToolId());
		assertEquals(programPreset.getToolSection(), presetDTO.getToolSection());
		assertEquals(programPreset.getProgramUuid(), presetDTO.getProgramUUID());
		assertEquals(programPreset.getName(), presetDTO.getName());
		assertEquals(programPreset.getConfiguration(), configuration);

	}

	@Test
	public void testMapPresetDTO() {

	}

}
