package org.ibp.api.rest.labelprinting;

import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;

import java.util.List;
import java.io.File;
import java.util.Map;

/**
 * Created by clarysabel on 2/7/19.
 */
public interface LabelsFileGenerator {

	File generate (final LabelsGeneratorInput labelsGeneratorInput, final List<Map<String, String>> labelsData);

}
