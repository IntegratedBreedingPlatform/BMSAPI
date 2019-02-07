package org.ibp.api.rest.labelprinting;

import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;

import java.util.List;
/**
 * Created by clarysabel on 2/7/19.
 */
@Component
public class CSVLabelsFileGenerator implements LabelsFileGenerator {

	@Override
	public File generate(final LabelsGeneratorInput labelsGeneratorInput, final List<Map<String, String>> labelsData) {
		return null;
	}
}
