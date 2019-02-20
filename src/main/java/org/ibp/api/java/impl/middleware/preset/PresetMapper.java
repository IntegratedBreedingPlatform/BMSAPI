package org.ibp.api.java.impl.middleware.preset;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.ibp.api.rest.preset.PresetDTO;
import org.springframework.stereotype.Component;

/**
 * Created by clarysabel on 2/19/19.
 */
@Component
public class PresetMapper {

	private ObjectMapper mapper;

	public PresetMapper() {
		mapper = new ObjectMapper();
		mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
	}

	ProgramPreset map (final PresetDTO presetDTO) {
		final ProgramPreset programPreset = new ProgramPreset();
		programPreset.setName(presetDTO.getName());
		programPreset.setToolId(presetDTO.getToolId());
		programPreset.setToolSection(presetDTO.getToolSection());
		programPreset.setProgramUuid(presetDTO.getProgramUUID());
		try {
			programPreset.setConfiguration(mapper.writerWithView(PresetDTO.View.Configuration.class).writeValueAsString(presetDTO));
		} catch (Exception e) {

		}
		return programPreset;
	}

	PresetDTO map (final ProgramPreset programPreset) {
		final PresetDTO presetDTO;
		try {
			presetDTO = mapper.readValue(programPreset.getConfiguration(), PresetDTO.class);
			presetDTO.setToolId(programPreset.getToolId());
			presetDTO.setProgramUUID(programPreset.getProgramUuid());
			presetDTO.setToolSection(programPreset.getToolSection());
			presetDTO.setName(programPreset.getName());
			presetDTO.setId(programPreset.getProgramPresetId());
			return presetDTO;
		} catch (Exception e) {
			return null;
		}
	}

}
