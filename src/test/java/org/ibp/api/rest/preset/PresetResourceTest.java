package org.ibp.api.rest.preset;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.apache.commons.lang3.SerializationUtils;
import org.generationcp.commons.constant.ToolSection;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.preset.PresetService;
import org.ibp.api.rest.preset.domain.FilePresetConfigurationDTO;
import org.ibp.api.rest.preset.domain.LabelPrintingPresetDTO;
import org.ibp.api.rest.preset.domain.PresetDTO;
import org.ibp.api.rest.preset.domain.PresetType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

/**
 * Created by clarysabel on 2/26/19.
 */
public class PresetResourceTest extends ApiUnitTestBase {

	private String name;

	private String programUUID;

	private String toolSection;

	private Integer toolId;

	private String type;

	private List<List<Integer>> selectedField;

	private LabelPrintingPresetDTO.BarcodeSetting barcodeSetting;

	private FilePresetConfigurationDTO filePresetConfigurationDTO;

	@Autowired
	private PresetService bmsapiPresetService;


	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public PresetService bmsapiPresetService() {
			return Mockito.mock(PresetService.class);
		}
	}

	@Before
	public void setup() throws Exception {
		super.setUp();
		programUUID = org.apache.commons.lang3.RandomStringUtils.randomAlphabetic(10);
		name = org.apache.commons.lang3.RandomStringUtils.randomAlphabetic(10);
		toolSection = ToolSection.DATASET_LABEL_PRINTING_PRESET.name();
		toolId = 23;

		type = PresetType.LABEL_PRINTING_PRESET.getName();
		selectedField = Arrays.asList(Arrays.asList(4, 13));
		barcodeSetting = new LabelPrintingPresetDTO.BarcodeSetting(Boolean.TRUE, Boolean.FALSE, Arrays.asList(2));
		filePresetConfigurationDTO = new FilePresetConfigurationDTO();
		filePresetConfigurationDTO.setOutputType("csv");
	}

	@Test
	public void createPreset_Ok() throws Exception {

		final PresetDTO presetDTO = buildPresetDTO();

		final PresetDTO savedPresetDTO = SerializationUtils.clone(presetDTO);
		savedPresetDTO.setId(1);

		doReturn(savedPresetDTO).when(this.bmsapiPresetService).savePreset(this.cropName, presetDTO);

		this.mockMvc.perform(MockMvcRequestBuilders.put("/crops/{crop}/presets", this.cropName).contentType(this.contentType)
				.content(this.convertObjectToByte(presetDTO))).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", is(savedPresetDTO.getId())));

	}

	@Test
	public void getPresets_Ok() throws Exception {
		final List<PresetDTO> presetDTOs = Arrays.asList(buildPresetDTO());
		doReturn(presetDTOs).when(this.bmsapiPresetService).getPresets(programUUID, toolId, toolSection);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/crops/{crop}/presets", this.cropName).param("programUUID", programUUID)
				.param("toolId", String.valueOf(toolId)).param("toolSection", toolSection)).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(presetDTOs.size())));
	}

	@Test
	public void deletePreset_Ok() throws Exception {

		doNothing().when(this.bmsapiPresetService).deletePreset(this.cropName, 1);

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/crops/{crop}/presets/{presetId}", this.cropName, 1))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	private PresetDTO buildPresetDTO() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(type);
		presetDTO.setName(name);
		presetDTO.setToolId(toolId);
		presetDTO.setProgramUUID(programUUID);
		presetDTO.setSelectedFields(selectedField);
		FilePresetConfigurationDTO filePresetConfigurationDTO = new FilePresetConfigurationDTO();
		filePresetConfigurationDTO.setOutputType("csv");
		presetDTO.setFileConfiguration(filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(barcodeSetting);
		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting =
				new LabelPrintingPresetDTO.BarcodeSetting(false, false, new ArrayList<>());
		presetDTO.setBarcodeSetting(barcodeSetting);
		return presetDTO;
	}

}
