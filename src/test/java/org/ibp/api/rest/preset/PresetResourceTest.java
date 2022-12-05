package org.ibp.api.rest.preset;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.apache.commons.lang3.SerializationUtils;
import org.generationcp.commons.constant.ToolSection;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.preset.PresetService;
import org.generationcp.middleware.domain.labelprinting.FilePresetConfigurationDTO;
import org.generationcp.middleware.domain.labelprinting.LabelPrintingPresetDTO;
import org.generationcp.middleware.domain.labelprinting.PresetDTO;
import org.generationcp.middleware.domain.labelprinting.PresetType;
import org.ibp.api.rest.labelprinting.domain.FieldType;
import org.ibp.api.rest.labelprinting.domain.LabelPrintingFieldUtils;
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

	private List<List<String>> selectedField;

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
		this.programUUID = org.apache.commons.lang3.RandomStringUtils.randomAlphabetic(10);
		this.name = org.apache.commons.lang3.RandomStringUtils.randomAlphabetic(10);
		this.toolSection = ToolSection.DATASET_LABEL_PRINTING_PRESET.name();
		this.toolId = 23;

		this.type = PresetType.LABEL_PRINTING_PRESET.getName();
		this.selectedField = Arrays.asList(Arrays.asList(LabelPrintingFieldUtils.buildCombinedKey(FieldType.STATIC, 4),
			LabelPrintingFieldUtils.buildCombinedKey(FieldType.STATIC, 13)));
		this.barcodeSetting = new LabelPrintingPresetDTO.BarcodeSetting(Boolean.TRUE, Boolean.FALSE,
			Arrays.asList(LabelPrintingFieldUtils.buildCombinedKey(FieldType.STATIC, 2)));
		this.filePresetConfigurationDTO = new FilePresetConfigurationDTO();
		this.filePresetConfigurationDTO.setOutputType("csv");
	}

	@Test
	public void createPreset_Ok() throws Exception {

		final PresetDTO presetDTO = this.buildPresetDTO();

		final PresetDTO savedPresetDTO = SerializationUtils.clone(presetDTO);
		savedPresetDTO.setId(1);

		doReturn(savedPresetDTO).when(this.bmsapiPresetService).savePreset(this.cropName, presetDTO);

		this.mockMvc.perform(MockMvcRequestBuilders.put("/crops/{cropname}/programs/{programUUID}/presets", this.cropName, this.programUUID).contentType(this.contentType)
				.content(this.convertObjectToByte(presetDTO))).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", is(savedPresetDTO.getId())));

	}

	@Test
	public void getPresets_Ok() throws Exception {
		final List<PresetDTO> presetDTOs = Arrays.asList(this.buildPresetDTO());
		doReturn(presetDTOs).when(this.bmsapiPresetService).getPresets(this.programUUID, this.toolId, this.toolSection);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/crops/{cropname}/programs/{programUUID}/presets", this.cropName, this.programUUID)
				.param("toolId", String.valueOf(this.toolId)).param("toolSection", this.toolSection)).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(presetDTOs.size())));
	}

	@Test
	public void deletePreset_Ok() throws Exception {

		doNothing().when(this.bmsapiPresetService).deletePreset(this.cropName, 1);

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/crops/{cropname}/programs/{programUUID}/presets/{presetId}", this.cropName, this.programUUID, 1))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	private PresetDTO buildPresetDTO() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(this.type);
		presetDTO.setName(this.name);
		presetDTO.setToolId(this.toolId);
		presetDTO.setProgramUUID(this.programUUID);
		presetDTO.setSelectedFields(this.selectedField);
		final FilePresetConfigurationDTO filePresetConfigurationDTO = new FilePresetConfigurationDTO();
		filePresetConfigurationDTO.setOutputType("csv");
		presetDTO.setFileConfiguration(filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(this.barcodeSetting);
		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting =
				new LabelPrintingPresetDTO.BarcodeSetting(false, false, new ArrayList<>());
		presetDTO.setBarcodeSetting(barcodeSetting);
		return presetDTO;
	}

}
