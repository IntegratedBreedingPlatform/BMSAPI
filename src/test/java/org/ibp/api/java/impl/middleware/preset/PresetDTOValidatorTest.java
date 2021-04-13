package org.ibp.api.java.impl.middleware.preset;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.manager.api.PresetService;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ConflictException;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.java.ontology.VariableService;
import org.ibp.api.java.program.ProgramService;
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

	private static final String CROP_NAME = "maize";

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
		this.programUUID = RandomStringUtils.randomAlphabetic(10);
		ContextHolder.setCurrentProgram(this.programUUID);
		this.name = RandomStringUtils.randomAlphabetic(10);
		this.toolSection = ToolSection.DATASET_LABEL_PRINTING_PRESET.name();
		this.toolId = 23;

		this.type = PresetType.LABEL_PRINTING_PRESET.getName();
		this.selectedField = Arrays.asList(Arrays.asList(4, 13));
		this.barcodeSetting = new LabelPrintingPresetDTO.BarcodeSetting(Boolean.TRUE, Boolean.FALSE, Arrays.asList(2));
		this.filePresetConfigurationDTO = new FilePresetConfigurationDTO();
		this.filePresetConfigurationDTO.setOutputType("csv");

	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfToolSectionIsNull() {
		final PresetDTO presetDTO = new PresetDTO();
		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfToolSectionIsInvalid() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setId(RandomUtils.nextInt());
		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfToolSectionIsEmpty() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setToolId(this.toolId);
		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfToolSectionIsNotValid() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setToolId(this.toolId);
		presetDTO.setToolSection(RandomStringUtils.random(2));
		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfTypeIsEmpty() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setToolId(this.toolId);
		presetDTO.setToolSection(this.toolSection);
		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfTypeIsNotAPresetType() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setToolId(this.toolId);
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(RandomStringUtils.random(3));
		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfProgramUUIDDoesNotExist() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setToolId(this.toolId);
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(this.type);
		presetDTO.setProgramUUID(this.programUUID);
		Mockito.doReturn(null).when(this.programService).getByUUIDAndCrop(CROP_NAME, this.programUUID);
		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfNameIsEmpty() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setToolId(this.toolId);
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(this.type);
		presetDTO.setProgramUUID(this.programUUID);
		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(CROP_NAME, this.programUUID);
		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ConflictException.class)
	public void validate_ThrowsException_IfNameAlreadyExists() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setToolId(this.toolId);
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(this.type);
		presetDTO.setName(this.name);
		presetDTO.setProgramUUID(this.programUUID);

		final List<ProgramPreset> programPresets = Arrays.asList(new ProgramPreset());
		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(CROP_NAME, this.programUUID);
		Mockito.doReturn(programPresets).when(this.presetService)
			.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
				presetDTO.getToolSection());

		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = NotSupportedException.class)
	public void validate_ThrowsException_IfPresetTypeNotImplementedYet() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setToolId(this.toolId);
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(PresetType.CROSSING_PRESET.getName());
		presetDTO.setName(this.name);
		presetDTO.setProgramUUID(this.programUUID);

		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(CROP_NAME, this.programUUID);
		Mockito.doReturn(new ArrayList<>()).when(this.presetService)
			.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
				presetDTO.getToolSection());

		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfFileConfigurationIsNull() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolId(this.toolId);
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(this.type);
		presetDTO.setName(this.name);
		presetDTO.setProgramUUID(this.programUUID);

		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(CROP_NAME, this.programUUID);
		Mockito.doReturn(new ArrayList<>()).when(this.presetService)
			.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
				presetDTO.getToolSection());

		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfBarcodeSettingIsNull() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(this.type);
		presetDTO.setName(this.name);
		presetDTO.setToolId(this.toolId);
		presetDTO.setProgramUUID(this.programUUID);
		presetDTO.setFileConfiguration(this.filePresetConfigurationDTO);

		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(CROP_NAME, this.programUUID);
		Mockito.doReturn(new ArrayList<>()).when(this.presetService)
			.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
				presetDTO.getToolSection());

		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfSelectedFieldsIsNull() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(this.type);
		presetDTO.setName(this.name);
		presetDTO.setToolId(this.toolId);
		presetDTO.setProgramUUID(this.programUUID);
		presetDTO.setFileConfiguration(this.filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(this.barcodeSetting);

		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(CROP_NAME, this.programUUID);
		Mockito.doReturn(new ArrayList<>()).when(this.presetService)
			.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
				presetDTO.getToolSection());

		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfSelectedFieldsInvalid() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(this.type);
		presetDTO.setName(this.name);
		presetDTO.setToolId(this.toolId);
		presetDTO.setProgramUUID(this.programUUID);
		presetDTO.setFileConfiguration(this.filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(this.barcodeSetting);

		final List<List<Integer>> selectedFields = Arrays.asList(Arrays.asList(-1));
		presetDTO.setSelectedFields(selectedFields);

		Mockito.doReturn(null).when(this.variableService).getVariableById(CROP_NAME, presetDTO.getProgramUUID(), "-1");

		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(CROP_NAME, this.programUUID);
		Mockito.doReturn(new ArrayList<>()).when(this.presetService)
			.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
				presetDTO.getToolSection());

		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfIsBarcodeAndAutomaticNeededButFieldsAreProvided() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(this.type);
		presetDTO.setName(this.name);
		presetDTO.setToolId(this.toolId);
		presetDTO.setProgramUUID(this.programUUID);
		presetDTO.setFileConfiguration(this.filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(this.barcodeSetting);

		presetDTO.setSelectedFields(this.selectedField);

		this.selectedField.forEach(list -> {
			list.forEach(id -> Mockito.doReturn(new VariableDetails()).when(this.variableService)
				.getVariableById(CROP_NAME, presetDTO.getProgramUUID(), String.valueOf(id)));
		});

		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting =
			new LabelPrintingPresetDTO.BarcodeSetting(true, true, Arrays.asList(1));
		presetDTO.setBarcodeSetting(barcodeSetting);

		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(CROP_NAME, this.programUUID);
		Mockito.doReturn(new ArrayList<>()).when(this.presetService)
			.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
				presetDTO.getToolSection());

		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfIsBarcodeAndNotAutomaticNeededButFieldsAreNotProvided() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolId(this.toolId);
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(this.type);
		presetDTO.setName(this.name);
		presetDTO.setProgramUUID(this.programUUID);
		presetDTO.setFileConfiguration(this.filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(this.barcodeSetting);

		presetDTO.setSelectedFields(this.selectedField);

		this.selectedField.forEach(list -> {
			list.forEach(id -> Mockito.doReturn(new VariableDetails()).when(this.variableService)
				.getVariableById(CROP_NAME, presetDTO.getProgramUUID(), String.valueOf(id)));
		});

		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting =
			new LabelPrintingPresetDTO.BarcodeSetting(true, false, new ArrayList<>());
		presetDTO.setBarcodeSetting(barcodeSetting);

		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(CROP_NAME, this.programUUID);
		Mockito.doReturn(new ArrayList<>()).when(this.presetService)
			.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
				presetDTO.getToolSection());

		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfIsBarcodeAndNotAutomaticNeededButFieldsAreInvalid() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolId(this.toolId);
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(this.type);
		presetDTO.setName(this.name);
		presetDTO.setProgramUUID(this.programUUID);
		presetDTO.setFileConfiguration(this.filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(this.barcodeSetting);

		presetDTO.setSelectedFields(this.selectedField);

		this.selectedField.forEach(list -> {
			list.forEach(id -> Mockito.doReturn(new VariableDetails()).when(this.variableService)
				.getVariableById(CROP_NAME, presetDTO.getProgramUUID(), String.valueOf(id)));
		});

		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting =
			new LabelPrintingPresetDTO.BarcodeSetting(true, false, Arrays.asList(-1));
		Mockito.doReturn(null).when(this.variableService).getVariableById(CROP_NAME, presetDTO.getProgramUUID(), "-1");

		presetDTO.setBarcodeSetting(barcodeSetting);

		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(CROP_NAME, this.programUUID);
		Mockito.doReturn(new ArrayList<>()).when(this.presetService)
			.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
				presetDTO.getToolSection());

		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfNoBarcodeNeededButAutomaticIsTrue() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(this.type);
		presetDTO.setName(this.name);
		presetDTO.setToolId(this.toolId);
		presetDTO.setProgramUUID(this.programUUID);
		presetDTO.setFileConfiguration(this.filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(this.barcodeSetting);

		presetDTO.setSelectedFields(this.selectedField);

		this.selectedField.forEach(list -> {
			list.forEach(id -> Mockito.doReturn(new VariableDetails()).when(this.variableService)
				.getVariableById(CROP_NAME, presetDTO.getProgramUUID(), String.valueOf(id)));
		});

		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting = new LabelPrintingPresetDTO.BarcodeSetting(false, true, null);
		presetDTO.setBarcodeSetting(barcodeSetting);

		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(CROP_NAME, this.programUUID);
		Mockito.doReturn(new ArrayList<>()).when(this.presetService)
			.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
				presetDTO.getToolSection());

		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfNoBarcodeNeededButBarcodeFieldsAreProvided() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(this.type);
		presetDTO.setName(this.name);
		presetDTO.setToolId(this.toolId);
		presetDTO.setProgramUUID(this.programUUID);
		presetDTO.setFileConfiguration(this.filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(this.barcodeSetting);

		presetDTO.setSelectedFields(this.selectedField);

		this.selectedField.forEach(list -> {
			list.forEach(id -> Mockito.doReturn(new VariableDetails()).when(this.variableService)
				.getVariableById(CROP_NAME, presetDTO.getProgramUUID(), String.valueOf(id)));
		});

		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting =
			new LabelPrintingPresetDTO.BarcodeSetting(false, false, Arrays.asList(1));
		presetDTO.setBarcodeSetting(barcodeSetting);

		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(CROP_NAME, this.programUUID);
		Mockito.doReturn(new ArrayList<>()).when(this.presetService)
			.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
				presetDTO.getToolSection());

		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfFileTypeIsNull() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(this.type);
		presetDTO.setName(this.name);
		presetDTO.setToolId(this.toolId);
		presetDTO.setProgramUUID(this.programUUID);
		final FilePresetConfigurationDTO filePresetConfigurationDTO = new FilePresetConfigurationDTO();
		presetDTO.setFileConfiguration(filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(this.barcodeSetting);

		presetDTO.setSelectedFields(this.selectedField);

		this.selectedField.forEach(list -> {
			list.forEach(id -> Mockito.doReturn(new VariableDetails()).when(this.variableService)
				.getVariableById(CROP_NAME, presetDTO.getProgramUUID(), String.valueOf(id)));
		});

		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting = new LabelPrintingPresetDTO.BarcodeSetting(false, false, null);
		presetDTO.setBarcodeSetting(barcodeSetting);

		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(CROP_NAME, this.programUUID);
		Mockito.doReturn(new ArrayList<>()).when(this.presetService)
			.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
				presetDTO.getToolSection());

		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void validate_ThrowsException_IfFileTypeIsNotSupported() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(this.type);
		presetDTO.setName(this.name);
		presetDTO.setToolId(this.toolId);
		presetDTO.setProgramUUID(this.programUUID);
		final FilePresetConfigurationDTO filePresetConfigurationDTO = new FilePresetConfigurationDTO();
		filePresetConfigurationDTO.setOutputType("txt");
		presetDTO.setFileConfiguration(filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(this.barcodeSetting);

		presetDTO.setSelectedFields(this.selectedField);

		this.selectedField.forEach(list -> {
			list.forEach(id -> Mockito.doReturn(new VariableDetails()).when(this.variableService)
				.getVariableById(CROP_NAME, presetDTO.getProgramUUID(), String.valueOf(id)));
		});

		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting = new LabelPrintingPresetDTO.BarcodeSetting(false, false, null);
		presetDTO.setBarcodeSetting(barcodeSetting);

		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(CROP_NAME, this.programUUID);
		Mockito.doReturn(new ArrayList<>()).when(this.presetService)
			.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
				presetDTO.getToolSection());

		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}

	@Test
	public void validate_Ok() {
		final LabelPrintingPresetDTO presetDTO = new LabelPrintingPresetDTO();
		presetDTO.setToolSection(this.toolSection);
		presetDTO.setType(this.type);
		presetDTO.setName(this.name);
		presetDTO.setToolId(this.toolId);
		presetDTO.setProgramUUID(this.programUUID);
		final FilePresetConfigurationDTO filePresetConfigurationDTO = new FilePresetConfigurationDTO();
		filePresetConfigurationDTO.setOutputType("csv");
		presetDTO.setFileConfiguration(filePresetConfigurationDTO);
		presetDTO.setBarcodeSetting(this.barcodeSetting);

		presetDTO.setSelectedFields(this.selectedField);

		this.selectedField.forEach(list -> {
			list.forEach(id -> Mockito.doReturn(new VariableDetails()).when(this.variableService)
				.getVariableById(CROP_NAME, presetDTO.getProgramUUID(), String.valueOf(id)));
		});

		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting = new LabelPrintingPresetDTO.BarcodeSetting(false, false, null);
		presetDTO.setBarcodeSetting(barcodeSetting);

		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(CROP_NAME, this.programUUID);
		Mockito.doReturn(new ArrayList<>()).when(this.presetService)
			.getProgramPresetFromProgramAndToolByName(presetDTO.getName(), presetDTO.getProgramUUID(), presetDTO.getToolId(),
				presetDTO.getToolSection());

		this.presetDTOValidator.validate(CROP_NAME, null, presetDTO);
	}
}
