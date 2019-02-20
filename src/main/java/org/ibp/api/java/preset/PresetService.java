package org.ibp.api.java.preset;

import java.util.List;
import org.ibp.api.rest.preset.PresetDTO;

/**
 * Created by clarysabel on 2/19/19.
 */
public interface PresetService {

	void savePreset(PresetDTO presetDTO);

	List<PresetDTO> getPresets (String programUUID, Integer toolId, String toolSection);

}
