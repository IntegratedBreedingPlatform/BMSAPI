package org.ibp.api.java.impl.middleware.preset;

import com.google.common.collect.Sets;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ForbiddenException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.program.ProgramService;
import org.ibp.api.rest.preset.domain.PresetDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * Created by clarysabel on 2/26/19.
 */
public class PresetServiceImplTest extends ApiUnitTestBase {

	private static String CROP_NAME = "maize";

	private WorkbenchUser workbenchUser;

	private String programUUID;

	private Integer presetId;

	@Mock
	private SecurityService securityService;

	@Mock
	private ProgramService programService;

	@Mock
	private PresetDTOValidator presetDTOValidator;

	@Mock
	private PresetMapper presetMapper;

	@Mock
	private org.generationcp.middleware.manager.api.PresetService middlewarePresetService;

	@InjectMocks
	private PresetServiceImpl presetService;

	@Before
	public void init() {
		programUUID = RandomStringUtils.randomAlphabetic(10);
		presetId = RandomUtils.nextInt();

		workbenchUser = new WorkbenchUser();
		workbenchUser.setName("username");

		Mockito.doReturn(workbenchUser).when(securityService).getCurrentlyLoggedInUser();
	}


	@Test(expected = ForbiddenException.class)
	public void savePreset_ThrowsException_WhenUserDoesNotBelongToProgram () {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setProgramUUID(programUUID);
		presetDTO.setToolId(23);
		Mockito.doNothing().when(presetDTOValidator).validate(CROP_NAME, presetDTO);
		Mockito.doReturn(new ProgramDTO()).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		presetService.savePreset(CROP_NAME, presetDTO);
	}

	@Test(expected = MiddlewareQueryException.class)
	public void savePreset_ThrowsException_WhenThereIsAMiddlewareException () {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setProgramUUID(programUUID);
		presetDTO.setToolId(23);
		Mockito.doNothing().when(presetDTOValidator).validate(CROP_NAME, presetDTO);
		final ProgramDTO programSummary = new ProgramDTO();
		programSummary.setMembers(Sets.newHashSet(workbenchUser.getName()));
		Mockito.doReturn(programSummary).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		final ProgramPreset programPreset = new ProgramPreset();
		Mockito.doReturn(programPreset).when(presetMapper).map(presetDTO);
		Mockito.doThrow(MiddlewareQueryException.class).when(middlewarePresetService).saveProgramPreset(programPreset);
		presetService.savePreset(CROP_NAME, presetDTO);
	}

	@Test
	public void savePreset_Ok () {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setProgramUUID(programUUID);
		presetDTO.setToolId(23);
		Mockito.doNothing().when(presetDTOValidator).validate(CROP_NAME, presetDTO);
		final ProgramDTO programSummary = new ProgramDTO();
		programSummary.setMembers(Sets.newHashSet(workbenchUser.getName()));
		Mockito.doReturn(programSummary).when(programService).getByUUIDAndCrop(CROP_NAME, programUUID);
		final ProgramPreset programPreset = new ProgramPreset();
		Mockito.doReturn(programPreset).when(presetMapper).map(presetDTO);
		Mockito.doReturn(programPreset).when(middlewarePresetService).saveProgramPreset(programPreset);
		presetService.savePreset(CROP_NAME, presetDTO);
	}

	@Test (expected = ApiRequestValidationException.class)
	public void getPresets_ThrowsException_IfProgramUUIDIsEmpty() {
		presetService.getPresets(null, 23, ToolSection.DATASET_LABEL_PRINTING_PRESET.name());
	}

	@Test (expected = ApiRequestValidationException.class)
	public void getPresets_ThrowsException_IfToolIdIsEmpty() {
		presetService.getPresets(programUUID, null, ToolSection.DATASET_LABEL_PRINTING_PRESET.name());
	}

	@Test (expected = ApiRequestValidationException.class)
	public void getPresets_ThrowsException_IfToolSectionIsEmpty() {
		presetService.getPresets(programUUID, 23, null);
	}

	@Test (expected = ResourceNotFoundException.class)
	public void deletePreset_ThrowsException_IfPresetDoesNotExist() {
		Mockito.doReturn(null).when(middlewarePresetService).getProgramPresetById(presetId);
		presetService.deletePreset(CROP_NAME, presetId);
	}
}
