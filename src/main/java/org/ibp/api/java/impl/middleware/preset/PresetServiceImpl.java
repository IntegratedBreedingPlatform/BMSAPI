package org.ibp.api.java.impl.middleware.preset;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.domain.labelprinting.PresetDTO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.generationcp.middleware.preset.PresetMapper;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ForbiddenException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.preset.PresetService;
import org.ibp.api.java.program.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class PresetServiceImpl implements PresetService {

	@Autowired
	private org.generationcp.middleware.manager.api.PresetService presetService;

	@Autowired
	private PresetDTOValidator presetDTOValidator;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private ProgramService programService;

	private BindingResult errors;

	@Autowired
	private PresetMapper presetMapper;

	@Override
	public PresetDTO savePreset(final String crop, final PresetDTO presetDTO) {
		this.presetDTOValidator.validate(crop, null, presetDTO);
		this.validateUserIsAProgramMember(crop, this.securityService.getCurrentlyLoggedInUser().getName(), presetDTO.getProgramUUID());
		ProgramPreset programPreset = presetMapper.map(presetDTO);
		programPreset = this.presetService.saveProgramPreset(programPreset);
		presetDTO.setId(programPreset.getProgramPresetId());
		return presetDTO;
	}

	@Override
	public List<PresetDTO> getPresets(final String programUUID, final Integer toolId, final String toolSection) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), PresetDTO.class.getName());
		if (StringUtils.isEmpty(programUUID)) {
			this.errors.reject("preset.program.required", "");
		}
		if (StringUtils.isEmpty(toolSection)) {
			this.errors.reject("preset.tool.section.required", "");
		}
		if (toolId == null) {
			this.errors.reject("preset.tool.id.required", "");
		}
		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		final List<ProgramPreset> programPresets = this.presetService.getProgramPresetFromProgramAndTool(programUUID, toolId, toolSection);
		final List<PresetDTO> presetDTOs = new ArrayList<>();
		programPresets.forEach(programPreset -> presetDTOs.add(presetMapper.map(programPreset)));
		return presetDTOs;
	}

	@Override
	public void deletePreset(final String crop, final Integer presetId) {
		this.presetDTOValidator.validateDeletable(presetId);
		final ProgramPreset programPreset = this.presetService.getProgramPresetById(presetId);
		this.validateUserIsAProgramMember(crop, this.securityService.getCurrentlyLoggedInUser().getName(), programPreset.getProgramUuid());
		this.presetService.deleteProgramPreset(presetId);
	}

	@Override
	public void updatePreset(final String crop, final Integer presetId, final PresetDTO presetDTO) {
		this.presetDTOValidator.validate(crop, presetId, presetDTO);
		this.validateUserIsAProgramMember(crop, this.securityService.getCurrentlyLoggedInUser().getName(), presetDTO.getProgramUUID());
		final ProgramPreset updateProgramPreset = this.presetService.getProgramPresetById(presetId);
		final ProgramPreset programPreset = presetMapper.map(presetDTO, updateProgramPreset);
		this.presetService.updateProgramPreset(updateProgramPreset);
	}

	private void validateUserIsAProgramMember(final String crop, final String username, final String programUUID) {
		final ProgramDTO program = this.programService.getByUUIDAndCrop(crop, programUUID);
		if (!program.getMembers().contains(username)) {
			this.errors = new MapBindingResult(new HashMap<String, String>(), PresetDTO.class.getName());
			this.errors.reject("preset.user.not.a.program.member", "");
			throw new ForbiddenException(this.errors.getAllErrors().get(0));
		}
	}
}
