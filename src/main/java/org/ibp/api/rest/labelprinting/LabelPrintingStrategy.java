package org.ibp.api.rest.labelprinting;

import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummary;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummaryInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummaryResponse;

import java.util.List;
import java.util.Map;

public interface LabelPrintingStrategy {

	void validateInputData(final LabelsNeededSummaryInput labelsNeededSummaryInput);

	LabelsNeededSummary getSummaryOfLabelsNeeded (final LabelsNeededSummaryInput labelsNeededSummaryInput);

	LabelsNeededSummaryResponse transformLabelsNeededSummary(final LabelsNeededSummary labelsNeededSummary);

	Map<String, String> getOriginResourceMetadata (final LabelsNeededSummaryInput labelsNeededSummaryInput);

	List<LabelType> getAvailableLabelFields(final LabelsNeededSummaryInput labelsNeededSummaryInput);

}
