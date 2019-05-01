package org.ibp.api.java.impl.middleware.preset;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.middleware.manager.api.PresetService;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.program.ProgramSummary;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ConflictException;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.java.ontology.VariableService;
import org.ibp.api.java.program.ProgramService;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.preset.domain.FilePresetConfigurationDTO;
import org.ibp.api.rest.preset.domain.LabelPrintingPresetDTO;
import org.ibp.api.rest.preset.domain.PresetDTO;
import org.ibp.api.rest.preset.domain.PresetType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by clarysabel on 2/25/19.
 */
public class PresetDTOValidatorTest extends ApiUnitTestBase {

	private static String CROP_NAME = "maize";

	private String name;

	private String programUUID;

	private String toolSection;

	private Integer toolId;

	private String type;

	private List<List<Integer>> selectedField;

	private LabelPrintingPresetDTO.BarcodeSetting barcodeSetting;

	private FilePresetConfigurationDTO filePresetConfigurationDTO;

	@Mock
	private ProgramService programService;

	@Mock
	private PresetService presetService;

	@Mock
	private VariableService variableService;

	@InjectMocks
	private PresetDTOValidator presetDTOValidator;

	@Before
	public void init() {
		programUUID = RandomStringUtils.randomAlphabetic(10);
		name = RandomStringUtils.randomAlphabetic(10);
		toolSection = ToolSection.DATASET_LABEL_PRINTING_PRESET.name();
		toolId = 23;

		type = PresetType.LABEL_PRINTING_PRESET.getName();
		selectedField = Arrays.asList(Arrays.asList(4, 13));
		barcodeSetting = new LabelPrintingPresetDTO.BarcodeSetting(Boolean.TRUE, Boolean.FALSE, Arrays.asList(2));
		filePresetConfigurationDTO = new FilePresetConfigurationDTO();
		filePresetConfigurationDTO.setOutputType("csv");

	}


	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfToolSectionIsNull() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfToolSectionIsInvalid() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setId(RandomUtils.nextInt());
		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfToolSectionIsEmpty() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setToolId(toolId);
		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfToolSectionIsNotValid() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setToolId(toolId);
		presetDTO.setToolSection(RandomStringUtils.random(2));
		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfTypeIsEmpty() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setToolId(toolId);
		presetDTO.setToolSection(toolSection);
		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfTypeIsNotAPresetType() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setToolId(toolId);
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(RandomStringUtils.random(3));
		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfProgramUUIDDoesNotExist() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setToolId(toolId);
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(type);
		presetDTO.setProgramUUID(programUUID);
		Mockito.doReturn(null).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfNameIsEmpty() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setToolId(toolId);
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(type);
		presetDTO.setProgramUUID(programUUID);
		Mockito.doReturn(new ProgramSummary()).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ConflictException.class)
	public void validate_ThrowsException_IfNameAlreadyExists() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setToolId(toolId);
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(type);
		presetDTO.setName(name);
		presetDTO.setProgramUUID(programUUID);

		final List<ProgramPreset> programPresets = Arrays.asList(new ProgramPreset());
		Mockito.doReturn(new ProgramSummary()).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		Mockito.doReturn(programPresets).when(presetService)
				.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
						presetDTO.getToolSection());

		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = NotSupportedException.class)
	public void validate_ThrowsException_IfPresetTypeNotImplementedYet() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setToolId(toolId);
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(PresetType.CROSSING_PRESET.getName());
		presetDTO.setName(name);
		presetDTO.setProgramUUID(programUUID);

		Mockito.doReturn(new ProgramSummary()).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		Mockito.doReturn(new ArrayList<>()).when(presetService)
				.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
						presetDTO.getToolSection());

		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfFileConfigurationIsNull() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolId(toolId);
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(type);
		presetDTO.setName(name);
		presetDTO.setProgramUUID(programUUID);

		Mockito.doReturn(new ProgramSummary()).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		Mockito.doReturn(new ArrayList<>()).when(presetService)
				.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
						presetDTO.getToolSection());

		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfBarcodeSettingIsNull() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(type);
		presetDTO.setName(name);
		presetDTO.setToolId(toolId);
		presetDTO.setProgramUUID(programUUID);
		presetDTO.setFileConfiguration(filePresetConfigurationDTO);

		Mockito.doReturn(new ProgramSummary()).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		Mockito.doReturn(new ArrayList<>()).when(presetService)
				.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
						presetDTO.getToolSection());

		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfSelectedFieldsIsNull() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(type);
		presetDTO.setName(name);
		presetDTO.setToolId(toolId);
		presetDTO.setProgramUUID(programUUID);
		presetDTO.setFileConfiguration(filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(barcodeSetting);

		Mockito.doReturn(new ProgramSummary()).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		Mockito.doReturn(new ArrayList<>()).when(presetService)
				.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
						presetDTO.getToolSection());

		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfSelectedFieldsInvalid() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(type);
		presetDTO.setName(name);
		presetDTO.setToolId(toolId);
		presetDTO.setProgramUUID(programUUID);
		presetDTO.setFileConfiguration(filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(barcodeSetting);

		final List<List<Integer>> selectedFields = Arrays.asList(Arrays.asList(-1));
		presetDTO.setSelectedFields(selectedFields);

		Mockito.doReturn(null).when(variableService).getVariableById(CROP_NAME, presetDTO.getProgramUUID(), "-1");

		Mockito.doReturn(new ProgramSummary()).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		Mockito.doReturn(new ArrayList<>()).when(presetService)
				.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
						presetDTO.getToolSection());

		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfIsBarcodeAndAutomaticNeededButFieldsAreProvided() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(type);
		presetDTO.setName(name);
		presetDTO.setToolId(toolId);
		presetDTO.setProgramUUID(programUUID);
		presetDTO.setFileConfiguration(filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(barcodeSetting);

		presetDTO.setSelectedFields(selectedField);

		selectedField.forEach(list -> {
			list.forEach(id -> Mockito.doReturn(new VariableDetails()).when(variableService)
					.getVariableById(CROP_NAME, presetDTO.getProgramUUID(), String.valueOf(id)));
		});

		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting =
				new LabelPrintingPresetDTO.BarcodeSetting(true, true, Arrays.asList(1));
		presetDTO.setBarcodeSetting(barcodeSetting);

		Mockito.doReturn(new ProgramSummary()).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		Mockito.doReturn(new ArrayList<>()).when(presetService)
				.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
						presetDTO.getToolSection());

		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfIsBarcodeAndNotAutomaticNeededButFieldsAreNotProvided() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolId(toolId);
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(type);
		presetDTO.setName(name);
		presetDTO.setProgramUUID(programUUID);
		presetDTO.setFileConfiguration(filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(barcodeSetting);

		presetDTO.setSelectedFields(selectedField);

		selectedField.forEach(list -> {
			list.forEach(id -> Mockito.doReturn(new VariableDetails()).when(variableService)
					.getVariableById(CROP_NAME, presetDTO.getProgramUUID(), String.valueOf(id)));
		});

		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting =
				new LabelPrintingPresetDTO.BarcodeSetting(true, false, new ArrayList<>());
		presetDTO.setBarcodeSetting(barcodeSetting);

		Mockito.doReturn(new ProgramSummary()).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		Mockito.doReturn(new ArrayList<>()).when(presetService)
				.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
						presetDTO.getToolSection());

		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfIsBarcodeAndNotAutomaticNeededButFieldsAreInvalid() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolId(toolId);
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(type);
		presetDTO.setName(name);
		presetDTO.setProgramUUID(programUUID);
		presetDTO.setFileConfiguration(filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(barcodeSetting);

		presetDTO.setSelectedFields(selectedField);

		selectedField.forEach(list -> {
			list.forEach(id -> Mockito.doReturn(new VariableDetails()).when(variableService)
					.getVariableById(CROP_NAME, presetDTO.getProgramUUID(), String.valueOf(id)));
		});

		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting =
				new LabelPrintingPresetDTO.BarcodeSetting(true, false, Arrays.asList(-1));
		Mockito.doReturn(null).when(variableService).getVariableById(CROP_NAME, presetDTO.getProgramUUID(), "-1");

		presetDTO.setBarcodeSetting(barcodeSetting);

		Mockito.doReturn(new ProgramSummary()).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		Mockito.doReturn(new ArrayList<>()).when(presetService)
				.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
						presetDTO.getToolSection());

		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfNoBarcodeNeededButAutomaticIsTrue() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(type);
		presetDTO.setName(name);
		presetDTO.setToolId(toolId);
		presetDTO.setProgramUUID(programUUID);
		presetDTO.setFileConfiguration(filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(barcodeSetting);

		presetDTO.setSelectedFields(selectedField);

		selectedField.forEach(list -> {
			list.forEach(id -> Mockito.doReturn(new VariableDetails()).when(variableService)
					.getVariableById(CROP_NAME, presetDTO.getProgramUUID(), String.valueOf(id)));
		});

		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting = new LabelPrintingPresetDTO.BarcodeSetting(false, true, null);
		presetDTO.setBarcodeSetting(barcodeSetting);

		Mockito.doReturn(new ProgramSummary()).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		Mockito.doReturn(new ArrayList<>()).when(presetService)
				.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
						presetDTO.getToolSection());

		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfNoBarcodeNeededButBarcodeFieldsAreProvided() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(type);
		presetDTO.setName(name);
		presetDTO.setToolId(toolId);
		presetDTO.setProgramUUID(programUUID);
		presetDTO.setFileConfiguration(filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(barcodeSetting);

		presetDTO.setSelectedFields(selectedField);

		selectedField.forEach(list -> {
			list.forEach(id -> Mockito.doReturn(new VariableDetails()).when(variableService)
					.getVariableById(CROP_NAME, presetDTO.getProgramUUID(), String.valueOf(id)));
		});

		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting =
				new LabelPrintingPresetDTO.BarcodeSetting(false, false, Arrays.asList(1));
		presetDTO.setBarcodeSetting(barcodeSetting);

		Mockito.doReturn(new ProgramSummary()).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		Mockito.doReturn(new ArrayList<>()).when(presetService)
				.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
						presetDTO.getToolSection());

		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfFileTypeIsNull() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(type);
		presetDTO.setName(name);
		presetDTO.setToolId(toolId);
		presetDTO.setProgramUUID(programUUID);
		FilePresetConfigurationDTO filePresetConfigurationDTO = new FilePresetConfigurationDTO();
		presetDTO.setFileConfiguration(filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(barcodeSetting);

		presetDTO.setSelectedFields(selectedField);

		selectedField.forEach(list -> {
			list.forEach(id -> Mockito.doReturn(new VariableDetails()).when(variableService)
					.getVariableById(CROP_NAME, presetDTO.getProgramUUID(), String.valueOf(id)));
		});

		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting = new LabelPrintingPresetDTO.BarcodeSetting(false, false, null);
		presetDTO.setBarcodeSetting(barcodeSetting);

		Mockito.doReturn(new ProgramSummary()).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		Mockito.doReturn(new ArrayList<>()).when(presetService)
				.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
						presetDTO.getToolSection());

		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test(expected = NotSupportedException.class)
	public void validate_ThrowsException_IfFileTypeIsNotSupported() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(type);
		presetDTO.setName(name);
		presetDTO.setToolId(toolId);
		presetDTO.setProgramUUID(programUUID);
		FilePresetConfigurationDTO filePresetConfigurationDTO = new FilePresetConfigurationDTO();
		filePresetConfigurationDTO.setOutputType(FileType.XLS.getExtension());
		presetDTO.setFileConfiguration(filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(barcodeSetting);

		presetDTO.setSelectedFields(selectedField);

		selectedField.forEach(list -> {
			list.forEach(id -> Mockito.doReturn(new VariableDetails()).when(variableService)
					.getVariableById(CROP_NAME, presetDTO.getProgramUUID(), String.valueOf(id)));
		});

		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting = new LabelPrintingPresetDTO.BarcodeSetting(false, false, null);
		presetDTO.setBarcodeSetting(barcodeSetting);

		Mockito.doReturn(new ProgramSummary()).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		Mockito.doReturn(new ArrayList<>()).when(presetService)
				.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
						presetDTO.getToolSection());

		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}

	@Test
	public void validate_Ok() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(toolSection);
		presetDTO.setType(type);
		presetDTO.setName(name);
		presetDTO.setToolId(toolId);
		presetDTO.setProgramUUID(programUUID);
		FilePresetConfigurationDTO filePresetConfigurationDTO = new FilePresetConfigurationDTO();
		filePresetConfigurationDTO.setOutputType("csv");
		presetDTO.setFileConfiguration(filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(barcodeSetting);

		presetDTO.setSelectedFields(selectedField);

		selectedField.forEach(list -> {
			list.forEach(id -> Mockito.doReturn(new VariableDetails()).when(variableService)
					.getVariableById(CROP_NAME, presetDTO.getProgramUUID(), String.valueOf(id)));
		});

		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting = new LabelPrintingPresetDTO.BarcodeSetting(false, false, null);
		presetDTO.setBarcodeSetting(barcodeSetting);

		Mockito.doReturn(new ProgramSummary()).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		Mockito.doReturn(new ArrayList<>()).when(presetService)
				.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
						presetDTO.getToolSection());

		presetDTOValidator.validate(CROP_NAME, presetDTO);
	}
}
