package org.ibp.api.java.impl.middleware.preset;

import org.ibp.api.rest.preset.domain.LabelPrintingPresetDTO;
import org.ibp.api.rest.preset.domain.PresetDTO;
import org.springframework.stereotype.Component;

/**
 * Created by clarysabel on 2/19/19.
 */
@Component
public class PresetDTOValidator {

	public void validate(final PresetDTO presetDTO) {

		//Validate toolSection

		//Validate name

		//Validate type

		//Validate Program

		//any possible validation between toolSection and PresetType?

		//Cast according type and call specific validations
	}

	public void validateLabelPrintingPreset(final LabelPrintingPresetDTO labelPrintingPresetDTO) {

	}

}
