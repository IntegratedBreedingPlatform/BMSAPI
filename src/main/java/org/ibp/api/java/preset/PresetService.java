package org.ibp.api.java.preset;

import java.util.List;
import org.ibp.api.rest.preset.domain.PresetDTO;

/**
 * Created by clarysabel on 2/19/19.
 */
public interface PresetService {

	PresetDTO savePreset(String crop, PresetDTO presetDTO);

	List<PresetDTO> getPresets (String programUUID, Integer toolId, String toolSection);

	void deletePreset(String crop, Integer presetId);

	void updatePreset(String crop, Integer presetId, PresetDTO presetDTO);

}
