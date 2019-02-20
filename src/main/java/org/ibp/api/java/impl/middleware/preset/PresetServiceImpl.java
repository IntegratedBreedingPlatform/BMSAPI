package org.ibp.api.java.impl.middleware.preset;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.ibp.api.java.preset.PresetService;
import org.ibp.api.rest.preset.PresetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PresetServiceImpl implements PresetService {

	@Autowired
	private org.generationcp.middleware.manager.api.PresetService presetService;

	@Autowired
	private PresetDTOValidator presetDTOValidator;

	@Autowired
	private PresetMapper presetMapper;

	@Override
	public void savePreset(final PresetDTO presetDTO) {
		presetDTO.setToolId(23);
		presetDTOValidator.validate(presetDTO);
		presetService.saveOrUpdateProgramPreset(presetMapper.map(presetDTO));
	}

	@Override
	public List<PresetDTO> getPresets(final String programUUID, final Integer toolId, final String toolSection) {
		final List<ProgramPreset> programPresets = presetService.getProgramPresetFromProgramAndTool(programUUID, toolId, toolSection);
		final List<PresetDTO> presetDTOs = new ArrayList<>();
		programPresets.forEach(programPreset -> presetDTOs.add(presetMapper.map(programPreset)));
		return presetDTOs;
	}
}
