package org.ibp.api.rest.labelprinting;

import com.google.common.collect.Maps;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummary;
import org.ibp.api.rest.labelprinting.domain.LabelsInfoInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummaryResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class LabelPrintingStrategy {

	abstract void validateLabelsInfoInputData(final LabelsInfoInput labelsInfoInput);

	//This function could end being generic for all the strategies, so re-review for next implementation and make
	//this interface an abstract class if needed.
	abstract void validateLabelsGeneratorInputData(final LabelsGeneratorInput labelsGeneratorInput);

	abstract LabelsNeededSummary getSummaryOfLabelsNeeded (final LabelsInfoInput labelsInfoInput);

	abstract LabelsNeededSummaryResponse transformLabelsNeededSummary(final LabelsNeededSummary labelsNeededSummary);

	abstract Map<String, String> getOriginResourceMetadata (final LabelsInfoInput labelsInfoInput);

	abstract List<LabelType> getAvailableLabelTypes(final LabelsInfoInput labelsInfoInput);

	abstract LabelsData getLabelsData (final LabelsGeneratorInput labelsGeneratorInput);

	abstract List<FileType> getSupportedFileTypes();

	Set<Field> getAllAvailableFields(final LabelsInfoInput labelsInfoInput) {
		final Set<Field> availableFields = new HashSet<>();
		this.getAvailableLabelTypes(labelsInfoInput).forEach(labelType -> availableFields.addAll(labelType.getFields()));
		return availableFields;
	}

}
