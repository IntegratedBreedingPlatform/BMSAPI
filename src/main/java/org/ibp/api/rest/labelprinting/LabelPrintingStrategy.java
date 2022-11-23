package org.ibp.api.rest.labelprinting;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.domain.LabelsInfoInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummary;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummaryResponse;
import org.ibp.api.rest.labelprinting.domain.OriginResourceMetadata;
import org.ibp.api.rest.labelprinting.domain.SortableFieldDto;
import org.generationcp.middleware.domain.labelprinting.LabelPrintingPresetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class LabelPrintingStrategy {

	private static final int FILENAME_MAX_LENGTH = 100;

	@Autowired
	private PedigreeService pedigreeService;

	@Autowired
	private CrossExpansionProperties crossExpansionProperties;

	@Autowired
	ResourceBundleMessageSource messageSource;

	/**
	 * Validate LabelsInfoInput
	 * Throws an exception when a validation problem is found
	 *
	 * @param labelsInfoInput
	 */
	abstract void validateLabelsInfoInputData(final LabelsInfoInput labelsInfoInput, final String programUUID);

	/**
	 * Given a labelInfoInput, it retrieves a summarized info of the labels needed in a more readable object
	 *
	 * @param labelsInfoInput
	 * @return LabelsNeededSummary
	 */
	abstract LabelsNeededSummary getSummaryOfLabelsNeeded(final LabelsInfoInput labelsInfoInput);

	/**
	 * Given LabelsNeededSummary, it transforms the object in a response that allows to build a more flexible response
	 * to the frontend
	 *
	 * @param labelsNeededSummary
	 * @return LabelsNeededSummaryResponse
	 */
	abstract LabelsNeededSummaryResponse transformLabelsNeededSummary(final LabelsNeededSummary labelsNeededSummary);

	/**
	 * Given labelsInfoInput, it will get the metadata of the resource which can be a dataset, a study, fieldmap, inventory,
	 * depending on the LabelPrintingType that is being implemented.
	 * Built to provide more flexibility to the frontend
	 *
	 * @param labelsInfoInput
	 * @return OriginResourceMetadata
	 */
	abstract OriginResourceMetadata getOriginResourceMetadata(final LabelsInfoInput labelsInfoInput, String programUUID);

	/**
	 * Given labelInfoInput, it will get the available LabelTypes (fields organized in lists), so that the user can
	 * select from them the ones he would like to include in her labels
	 *
	 * @param labelsInfoInput
	 * @param programUUID
	 * @return List<LabelType>
	 */
	abstract List<LabelType> getAvailableLabelTypes(final LabelsInfoInput labelsInfoInput, final String programUUID);

	/**
	 * Given labelsGeneratorInput (user selection for labels creation), it will get all the data from the database to be able
	 * to build the labels.
	 * It will also include data for the barcode generation
	 *
	 * @param labelsGeneratorInput
	 * @param programUUID
	 * @return LabelsData
	 */
	abstract LabelsData getLabelsData(final LabelsGeneratorInput labelsGeneratorInput, final String programUUID);

	/**
	 * List all the supported file types for a particular strategy.
	 * Once all datatypes are implemented for all the strategies, this function can be removed
	 *
	 * @return
	 */
	abstract List<FileType> getSupportedFileTypes();

	/**
	 * List of Sortable field labels for a particular strategy.
	 *
	 * @return Sortable
	 */
	abstract List<SortableFieldDto> getSortableFields();

	/**
	 * Validate LabelsGeneratorInput
	 * Throws an exception when a validation problem is found
	 *
	 * @param labelsGeneratorInput
	 * @param programUUID
	 */
	void validateLabelsGeneratorInputData(final LabelsGeneratorInput labelsGeneratorInput, final String programUUID) {
		this.validateLabelsInfoInputData(labelsGeneratorInput, programUUID);

		final Set<Integer> availableKeys = this.getAvailableLabelTypes(labelsGeneratorInput, programUUID)
			.stream().flatMap(labelType -> labelType.getFields().stream())
			.map(Field::getId)
			.collect(Collectors.toSet());

		final Set<Integer> requestedFields = new HashSet<>();
		int totalRequestedFields = 0;

		for (final List<Integer> list : labelsGeneratorInput.getFields()) {
			for (final Integer key : list) {
				requestedFields.add(key);
				totalRequestedFields++;
			}
		}
		final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		if (requestedFields.isEmpty()) {
			// Error, at least one requested field is needed
			errors.reject("label.fields.selection.empty");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		if (!availableKeys.containsAll(requestedFields)) {
			// Error, some of the requested fields are not available to use
			errors.reject("label.fields.invalid");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		if (totalRequestedFields != requestedFields.size()) {
			// Error, duplicated requested field
			errors.reject("label.fields.duplicated");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		if (labelsGeneratorInput.isBarcodeRequired() && !labelsGeneratorInput.isAutomaticBarcode()) {
			// Validate that at least one is selected
			if (labelsGeneratorInput.getBarcodeFields().isEmpty()) {
				errors.reject("barcode.fields.empty");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
			// Validate that selected are availableFields
			if (!availableKeys.containsAll(labelsGeneratorInput.getBarcodeFields())) {
				// Error, some of the requested fields are not available to use
				errors.reject("barcode.fields.invalid");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}

		// Validation for the file name
		final String fileName = labelsGeneratorInput.getFileName();

		if (StringUtils.isEmpty(fileName)) {
			errors.reject("common.error.filename.required");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (!FileUtils.isFilenameValid(fileName)) {
			errors.reject("common.error.invalid.filename.windows");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (fileName.length() > FILENAME_MAX_LENGTH) {
			errors.reject("common.error.invalid.filename.size", new String[] {String.valueOf(FILENAME_MAX_LENGTH)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	Set<Field> getAllAvailableFields(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		final Set<Field> availableFields = new HashSet<>();
		this.getAvailableLabelTypes(labelsInfoInput, programUUID).forEach(labelType -> availableFields.addAll(labelType.getFields()));
		return availableFields;
	}

	String getPedigree(final String gid, final Map<String, String> gidPedigreeMap) {
		final String pedigree;
		if (gidPedigreeMap.containsKey(gid)) {
			pedigree = gidPedigreeMap.get(gid);
		} else {
			pedigree = this.pedigreeService.getCrossExpansion(Integer.valueOf(gid), this.crossExpansionProperties);
			gidPedigreeMap.put(gid, pedigree);
		}
		return pedigree;
	}

	void validateBarcode(final LabelsGeneratorInput labelsGeneratorInput, final LabelsData labelsData) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		if (!labelsGeneratorInput.isAutomaticBarcode()) {
			for (final Map<Integer, String> data : labelsData.getData()) {
				final List<Integer> barcodeIds =
					labelsGeneratorInput.getBarcodeFields().stream().filter(labelId -> StringUtils.isEmpty(data.get(labelId))).collect(
						Collectors.toList());
				if (!barcodeIds.isEmpty()) {
					errors.reject("label.fields.barcodes.selected.empty.value", "");
					throw new ApiRequestValidationException(errors.getAllErrors());
				}
			}
		}
	}

	void populateAttributesLabelType(final String programUUID, final List<LabelType> labelTypes,
		final List<Integer> recordIds, final List<Variable> attributeVariables) {
		// Attributes labels
		final String attributesPropValue = this.messageSource.getMessage(
			"label.printing.attributes.details", null, LocaleContextHolder.getLocale());
		final LabelType attributesType = new LabelType(attributesPropValue, attributesPropValue);
		attributesType.setFields(new ArrayList<>());
		labelTypes.add(attributesType);

		if (!recordIds.isEmpty()) {
			attributesType.getFields().addAll(attributeVariables.stream()
				.map(attributeVariable -> new Field(
					toKey(attributeVariable.getId()),
					StringUtils.isNotBlank(attributeVariable.getAlias()) ? attributeVariable.getAlias() : attributeVariable.getName()))
				.collect(Collectors.toList()));
		}
	}

	/**
	 * Given labelInfoInput, it will get a LabelPrintingPresetDTO depending on the LabelPrintingType
	 *
	 * @param labelsInfoInput
	 * @param programUUID
	 * @return LabelPrintingPresetDTO
	 */
	abstract LabelPrintingPresetDTO getDefaultSetting(final LabelsInfoInput labelsInfoInput, final String programUUID);
}
