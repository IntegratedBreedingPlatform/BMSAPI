package org.ibp.api.rest.labelprinting;

import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummary;
import org.ibp.api.rest.labelprinting.domain.LabelsInfoInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummaryResponse;
import org.ibp.api.rest.labelprinting.domain.OriginResourceMetadata;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class LabelPrintingStrategy {

	/**
	 * Validate LabelsInfoInput
	 * Throws an exception when a validation problem is found
	 *
	 * @param labelsInfoInput
	 */
	abstract void validateLabelsInfoInputData(final LabelsInfoInput labelsInfoInput);

	//This function could end being generic for all the strategies, so re-review for next implementation and make
	//this interface an abstract class if needed.

	/**
	 * Validate LabelsGeneratorInput
	 * Throws an exception when a validation problem is found
	 * @param labelsGeneratorInput
	 */
	abstract void validateLabelsGeneratorInputData(final LabelsGeneratorInput labelsGeneratorInput);

	/**
	 * Given a labelInfoInput, it retrieves a summarized info of the labels needed in a more readable object
	 * @param labelsInfoInput
	 * @return LabelsNeededSummary
	 */
	abstract LabelsNeededSummary getSummaryOfLabelsNeeded (final LabelsInfoInput labelsInfoInput);

	/**
	 * Given LabelsNeededSummary, it transforms the object in a response that allows to build a more flexible response
	 * to the frontend
	 * @param labelsNeededSummary
	 * @return LabelsNeededSummaryResponse
	 */
	abstract LabelsNeededSummaryResponse transformLabelsNeededSummary(final LabelsNeededSummary labelsNeededSummary);

	/**
	 * Given labelsInfoInput, it will get the metadata of the resource which can be a dataset, a study, fieldmap, inventory,
	 * depending on the LabelPrintingType that is being implemented.
	 * Built to provide more flexibility to the frontend
	 * @param labelsInfoInput
	 * @return OriginResourceMetadata
	 */
	abstract OriginResourceMetadata getOriginResourceMetadata (final LabelsInfoInput labelsInfoInput);

	/**
	 * Given labelInfoInput, it will get the available LabelTypes (fields organized in lists), so that the user can
	 * select from them the ones he would like to include in her labels
	 * @param labelsInfoInput
	 * @return List<LabelType>
	 */
	abstract List<LabelType> getAvailableLabelTypes(final LabelsInfoInput labelsInfoInput);

	/**
	 * Given labelsGeneratorInput (user selection for labels creation), it will get all the data from the database to be able
	 * to build the labels.
	 * It will also include data for the barcode generation
	 * @param labelsGeneratorInput
	 * @return LabelsData
	 */
	abstract LabelsData getLabelsData (final LabelsGeneratorInput labelsGeneratorInput);

	/**
	 * List all the supported file types for a particular strategy.
	 * Once all datatypes are implemented for all the strategies, this function can be removed
	 * @return
	 */
	abstract List<FileType> getSupportedFileTypes();

	Set<Field> getAllAvailableFields(final LabelsInfoInput labelsInfoInput) {
		final Set<Field> availableFields = new HashSet<>();
		this.getAvailableLabelTypes(labelsInfoInput).forEach(labelType -> availableFields.addAll(labelType.getFields()));
		return availableFields;
	}

}
