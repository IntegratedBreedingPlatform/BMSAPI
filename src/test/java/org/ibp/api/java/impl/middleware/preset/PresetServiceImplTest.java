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
		this.programUUID = RandomStringUtils.randomAlphabetic(10);
		this.presetId = RandomUtils.nextInt();

		this.workbenchUser = new WorkbenchUser();
		this.workbenchUser.setName("username");

		Mockito.doReturn(this.workbenchUser).when(this.securityService).getCurrentlyLoggedInUser();
	}

	@Test(expected = ForbiddenException.class)
	public void savePreset_ThrowsException_WhenUserDoesNotBelongToProgram() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setProgramUUID(this.programUUID);
		presetDTO.setToolId(23);
		Mockito.doNothing().when(this.presetDTOValidator).validate(CROP_NAME, null, presetDTO);
		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(CROP_NAME, this.programUUID);
		this.presetService.savePreset(this.CROP_NAME, presetDTO);
	}

	@Test(expected = MiddlewareQueryException.class)
	public void savePreset_ThrowsException_WhenThereIsAMiddlewareException() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setProgramUUID(this.programUUID);
		presetDTO.setToolId(23);
		Mockito.doNothing().when(this.presetDTOValidator).validate(this.CROP_NAME, null, presetDTO);
		final ProgramDTO programSummary = new ProgramDTO();
		programSummary.setMembers(Sets.newHashSet(this.workbenchUser.getName()));
		Mockito.doReturn(programSummary).when(this.programService).getByUUIDAndCrop(this.CROP_NAME, this.programUUID);
		final ProgramPreset programPreset = new ProgramPreset();
		Mockito.doReturn(programPreset).when(this.presetMapper).map(presetDTO);
		Mockito.doThrow(MiddlewareQueryException.class).when(this.middlewarePresetService).saveProgramPreset(programPreset);
		this.presetService.savePreset(this.CROP_NAME, presetDTO);
	}

	@Test
	public void savePreset_Ok() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setProgramUUID(this.programUUID);
		presetDTO.setToolId(23);
		Mockito.doNothing().when(this.presetDTOValidator).validate(this.CROP_NAME, null, presetDTO);
		final ProgramDTO programSummary = new ProgramDTO();
		programSummary.setMembers(Sets.newHashSet(this.workbenchUser.getName()));
		Mockito.doReturn(programSummary).when(this.programService).getByUUIDAndCrop(this.CROP_NAME, this.programUUID);
		final ProgramPreset programPreset = new ProgramPreset();
		Mockito.doReturn(programPreset).when(this.presetMapper).map(presetDTO);
		Mockito.doReturn(programPreset).when(this.middlewarePresetService).saveProgramPreset(programPreset);
		this.presetService.savePreset(this.CROP_NAME, presetDTO);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void getPresets_ThrowsException_IfProgramUUIDIsEmpty() {
		this.presetService.getPresets(null, 23, ToolSection.DATASET_LABEL_PRINTING_PRESET.name());
	}

	@Test(expected = ApiRequestValidationException.class)
	public void getPresets_ThrowsException_IfToolIdIsEmpty() {
		this.presetService.getPresets(this.programUUID, null, ToolSection.DATASET_LABEL_PRINTING_PRESET.name());
	}

	@Test(expected = ApiRequestValidationException.class)
	public void getPresets_ThrowsException_IfToolSectionIsEmpty() {
		this.presetService.getPresets(this.programUUID, 23, null);
	}

	@Test
	public void delete_Ok() {
		final ProgramPreset programPreset = new ProgramPreset();
		programPreset.setProgramUuid(this.programUUID);
		final ProgramDTO programSummary = new ProgramDTO();
		programSummary.setMembers(Sets.newHashSet(this.workbenchUser.getName()));
		Mockito.doNothing().when(this.presetDTOValidator).validateDeletable(this.presetId);
		Mockito.doReturn(programPreset).when(this.middlewarePresetService).getProgramPresetById(this.presetId);
		Mockito.doReturn(programSummary).when(this.programService).getByUUIDAndCrop(this.CROP_NAME, this.programUUID);
		Mockito.doNothing().when(this.middlewarePresetService).deleteProgramPreset(this.presetId);
		this.presetService.deletePreset(this.CROP_NAME, this.presetId);
	}

	@Test(expected = ForbiddenException.class)
	public void deletePreset_ThrowsException_WhenUserDoesNotBelongToProgram() {
		final ProgramPreset programPreset = new ProgramPreset();
		programPreset.setProgramUuid(this.programUUID);
		Mockito.doNothing().when(this.presetDTOValidator).validateDeletable(this.presetId);
		Mockito.doReturn(programPreset).when(this.middlewarePresetService).getProgramPresetById(this.presetId);
		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(this.CROP_NAME, this.programUUID);
		this.presetService.deletePreset(this.CROP_NAME, this.presetId);
	}

	@Test
	public void updatePreset_Ok() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setProgramUUID(this.programUUID);
		presetDTO.setToolId(23);
		final ProgramPreset programPreset = new ProgramPreset();
		programPreset.setProgramUuid(this.programUUID);
		final ProgramDTO programSummary = new ProgramDTO();
		programSummary.setMembers(Sets.newHashSet(this.workbenchUser.getName()));
		Mockito.doNothing().when(this.presetDTOValidator).validate(this.CROP_NAME, this.presetId, presetDTO);
		Mockito.doReturn(programPreset).when(this.middlewarePresetService).getProgramPresetById(this.presetId);
		Mockito.doReturn(programSummary).when(this.programService).getByUUIDAndCrop(this.CROP_NAME, this.programUUID);
		Mockito.doNothing().when(this.middlewarePresetService).deleteProgramPreset(this.presetId);
		this.presetService.updatePreset(this.CROP_NAME, this.presetId, presetDTO);
	}


	@Test(expected = ForbiddenException.class)
	public void updatePreset_ThrowsException_WhenUserDoesNotBelongToProgram() {
		final PresetDTO presetDTO = new PresetDTO();
		presetDTO.setProgramUUID(this.programUUID);
		presetDTO.setToolId(23);
		Mockito.doNothing().when(this.presetDTOValidator).validate(this.CROP_NAME, this.presetId, presetDTO);
		final ProgramDTO programSummary = new ProgramDTO();
		Mockito.doReturn(programSummary).when(this.programService).getByUUIDAndCrop(this.CROP_NAME, this.programUUID);
		this.presetService.updatePreset(this.CROP_NAME, this.presetId, presetDTO);
	}
}
