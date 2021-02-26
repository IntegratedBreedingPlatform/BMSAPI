package org.ibp.api.java.impl.middleware.preset;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ForbiddenException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.preset.PresetService;
import org.ibp.api.java.program.ProgramService;
import org.ibp.api.rest.preset.domain.PresetDTO;
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
	private PresetMapper presetMapper;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private ProgramService programService;

	private BindingResult errors;

	@Override
	public PresetDTO savePreset(final String crop, final PresetDTO presetDTO) {
		presetDTOValidator.validate(crop, presetDTO);
		this.validateUserIsAProgramMember(crop, securityService.getCurrentlyLoggedInUser().getName(), presetDTO.getProgramUUID());
		ProgramPreset programPreset = presetMapper.map(presetDTO);
		programPreset = presetService.saveProgramPreset(programPreset);
		presetDTO.setId(programPreset.getProgramPresetId());
		return presetDTO;
	}

	@Override
	public List<PresetDTO> getPresets(final String programUUID, final Integer toolId, final String toolSection) {
		errors = new MapBindingResult(new HashMap<String, String>(), PresetDTO.class.getName());
		if (StringUtils.isEmpty(programUUID)) {
			errors.reject("preset.program.required", "");
		}
		if (StringUtils.isEmpty(toolSection)) {
			errors.reject("preset.tool.section.required", "");
		}
		if (toolId == null) {
			errors.reject("preset.tool.id.required", "");
		}
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		final List<ProgramPreset> programPresets = presetService.getProgramPresetFromProgramAndTool(programUUID, toolId, toolSection);
		final List<PresetDTO> presetDTOs = new ArrayList<>();
		programPresets.forEach(programPreset -> presetDTOs.add(presetMapper.map(programPreset)));
		return presetDTOs;
	}

	@Override
	public void deletePreset(final String crop, final Integer presetId) {
		final ProgramPreset programPreset = presetService.getProgramPresetById(presetId);
		if (programPreset == null) {
			errors = new MapBindingResult(new HashMap<String, String>(), PresetDTO.class.getName());
			errors.reject("preset.not.found", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
		this.validateUserIsAProgramMember(crop, securityService.getCurrentlyLoggedInUser().getName(), programPreset.getProgramUuid());
		presetService.deleteProgramPreset(presetId);
	}

	private void validateUserIsAProgramMember(final String crop, final String username, final String programUUID){
		final ProgramDTO program = programService.getByUUIDAndCrop(crop, programUUID);
		if (!program.getMembers().contains(username)) {
			errors = new MapBindingResult(new HashMap<String, String>(), PresetDTO.class.getName());
			errors.reject("preset.user.not.a.program.member", "");
			throw new ForbiddenException(errors.getAllErrors().get(0));
		}
	}
}
