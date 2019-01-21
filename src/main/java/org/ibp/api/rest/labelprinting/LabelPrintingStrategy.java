package org.ibp.api.rest.labelprinting;

public interface LabelPrintingStrategy {

	void validateInputData(final LabelsNeededSummaryInput labelsNeededSummaryInput);

	LabelsNeededSummary getSummaryOfLabelsNeeded (final LabelsNeededSummaryInput labelsNeededSummaryInput);

	LabelsNeededSummaryResponse transformLabelsNeededSummary(final LabelsNeededSummary labelsNeededSummary);

}
