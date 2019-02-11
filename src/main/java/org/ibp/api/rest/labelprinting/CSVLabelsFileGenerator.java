package org.ibp.api.rest.labelprinting;

import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by clarysabel on 2/7/19.
 */
@Component
public class CSVLabelsFileGenerator implements LabelsFileGenerator {

	@Override
	public File generate(final LabelsGeneratorInput labelsGeneratorInput, final LabelsData labelsData) {
		return null;
	}
}
