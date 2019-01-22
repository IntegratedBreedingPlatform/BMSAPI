package org.ibp.api.rest.labelprinting;

import java.util.Map;

public interface LabelPrintingStrategy {

	void validateInputData(final LabelsNeededSummaryInput labelsNeededSummaryInput);

	LabelsNeededSummary getSummaryOfLabelsNeeded (final LabelsNeededSummaryInput labelsNeededSummaryInput);

	LabelsNeededSummaryResponse transformLabelsNeededSummary(final LabelsNeededSummary labelsNeededSummary);

	Map<String, String> getOriginResourceMetadata (final LabelsNeededSummaryInput labelsNeededSummaryInput);

}
