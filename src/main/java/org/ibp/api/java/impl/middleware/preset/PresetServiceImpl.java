package org.ibp.api.java.impl.middleware.preset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.preset.PresetService;
import org.ibp.api.rest.preset.domain.PresetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

@Service
public class PresetServiceImpl implements PresetService {

	@Autowired
	private org.generationcp.middleware.manager.api.PresetService presetService;

	@Autowired
	private PresetDTOValidator presetDTOValidator;

	@Autowired
	private PresetMapper presetMapper;

	private BindingResult errors;

	@Override
	public PresetDTO savePreset(final PresetDTO presetDTO) {
		presetDTO.setToolId(23);
		presetDTOValidator.validate(presetDTO);
		final ProgramPreset programPreset = presetService.saveProgramPreset(presetMapper.map(presetDTO));
		presetDTO.setId(programPreset.getProgramPresetId());
		return presetDTO;
	}

	@Override
	public List<PresetDTO> getPresets(final String programUUID, final Integer toolId, final String toolSection) {
		final List<ProgramPreset> programPresets = presetService.getProgramPresetFromProgramAndTool(programUUID, toolId, toolSection);
		final List<PresetDTO> presetDTOs = new ArrayList<>();
		programPresets.forEach(programPreset -> presetDTOs.add(presetMapper.map(programPreset)));
		return presetDTOs;
	}

	@Override
	public void deletePreset(final Integer presetId) {
		final ProgramPreset programPreset = presetService.getProgramPresetById(presetId);
		if (programPreset == null) {
			errors = new MapBindingResult(new HashMap<String, String>(), PresetDTO.class.getName());
			errors.reject("preset.not.found", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
		presetService.deleteProgramPreset(presetId);
	}
}
