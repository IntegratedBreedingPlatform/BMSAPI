package org.ibp.api.rest.labelprinting;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.Season;
import org.generationcp.middleware.service.api.dataset.ObservationUnitData;
import org.ibp.api.domain.common.LabelPrintingStaticField;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.FieldType;
import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public abstract class ObservationLabelPrintingHelper {

	static String PLOT = "PLOT";

	//Variable ids of PI_NAME_ID and COOPERATOR_ID
	static List<Integer> PAIR_ID_VARIABLES = Arrays.asList(TermId.PI_ID.getId(), TermId.COOPERATOOR_ID.getId());

	public static List<Field> transform(final List<MeasurementVariable> measurementVariables) {
		final List<Field> fields = new LinkedList<>();
		for (final MeasurementVariable measurementVariable : measurementVariables) {
			final Field field = new Field(measurementVariable);
			//Requirement to show PLOT OBS_UNIT_ID label when variable = OBS_UNIT_ID in Plot Dataset
			//Which is in fact the only dataset that cointains this variable.
			if (field.getId().equals(TermId.OBS_UNIT_ID.getId())) {
				field.setName(PLOT.concat(StringUtils.SPACE).concat(field.getName()));
			}
			if (field.getId().equals(TermId.SEASON_VAR.getId())) {
				field.setName("Season");
			}
			field.setFieldType(FieldType.VARIABLE);
			fields.add(field);
		}
		return fields;
	}

	public static List<Field> transformNameTypesToFields(final List<GermplasmNameTypeDTO> germplasmNameTypeDTOs) {
		final List<Field> fields = new LinkedList<>();
		for (final GermplasmNameTypeDTO germplasmNameTypeDTO : germplasmNameTypeDTOs) {
			final Field field = new Field(germplasmNameTypeDTO.getId(), germplasmNameTypeDTO.getCode(), FieldType.NAME);
			fields.add(field);
		}
		return fields;
	}

	public static List<Field> buildTransactionDetailsFields(final ResourceBundleMessageSource messageSource){

		final Locale locale = LocaleContextHolder.getLocale();

		final String transactionIdPropValue = messageSource.getMessage("label.printing.field.transaction.id", null, locale);
		final String transactionStatusPropValue = messageSource.getMessage("label.printing.field.transaction.status", null, locale);
		final String transactionTypePropValue = messageSource.getMessage("label.printing.field.transaction.type", null, locale);
		final String transactionCreationDatePropValue = messageSource.getMessage("label.printing.field.transaction.creation.date", null, locale);
		final String transactionNotesPropValue = messageSource.getMessage("label.printing.field.transaction.notes", null, locale);
		final String transactionUsernamePropValue = messageSource.getMessage("label.printing.field.transaction.username", null, locale);

		return ImmutableList.<Field>builder()
			.add(new Field(LabelPrintingStaticField.TRN_ID.getFieldId(), transactionIdPropValue, FieldType.STATIC))
			.add(new Field(LabelPrintingStaticField.STATUS.getFieldId(), transactionStatusPropValue, FieldType.STATIC))
			.add(new Field(LabelPrintingStaticField.TYPE.getFieldId(), transactionTypePropValue, FieldType.STATIC))
			.add(new Field(LabelPrintingStaticField.CREATED.getFieldId(), transactionCreationDatePropValue, FieldType.STATIC))
			.add(new Field(LabelPrintingStaticField.TRN_NOTES.getFieldId(), transactionNotesPropValue, FieldType.STATIC))
			.add(new Field(LabelPrintingStaticField.USERNAME.getFieldId(), transactionUsernamePropValue, FieldType.STATIC)).build();
	}

	public static List<Field> buildLotDetailsFields(final ResourceBundleMessageSource messageSource){

		final Locale locale = LocaleContextHolder.getLocale();

		final String lotIDPropValue = messageSource.getMessage("label.printing.field.lot.id", null, locale);
		final String lotUIDPropValue = messageSource.getMessage("label.printing.field.lot.uid", null, locale);
		final String lotStockIdPropValue = messageSource.getMessage("label.printing.field.lot.stock.id", null, locale);
		final String lotAvailableBalancePropValue = messageSource.getMessage("label.printing.field.lot.available.balance", null, locale);
		final String lotUnitsPropValue = messageSource.getMessage("label.printing.field.lot.units", null, locale);
		final String lotStorageLocationAbbrPropValue = messageSource.getMessage("label.printing.field.lot.storage.location.abbr", null, locale);
		final String lotStorageLocationPropValue = messageSource.getMessage("label.printing.field.lot.storage.location", null, locale);
		final String lotNotesPropValue = messageSource.getMessage("label.printing.field.lot.notes", null, locale);

		return ImmutableList.<Field>builder()
			.add(new Field(LabelPrintingStaticField.LOT_ID.getFieldId(), lotIDPropValue, FieldType.STATIC))
			.add(new Field(LabelPrintingStaticField.LOT_UID.getFieldId(), lotUIDPropValue, FieldType.STATIC))
			.add(new Field(LabelPrintingStaticField.STOCK_ID.getFieldId(), lotStockIdPropValue, FieldType.STATIC))
			.add(new Field(LabelPrintingStaticField.AVAILABLE_BALANCE.getFieldId(), lotAvailableBalancePropValue, FieldType.STATIC))
			.add(new Field(LabelPrintingStaticField.UNITS.getFieldId(), lotUnitsPropValue, FieldType.STATIC))
			.add(new Field(LabelPrintingStaticField.STORAGE_LOCATION_ABBR.getFieldId(), lotStorageLocationAbbrPropValue, FieldType.STATIC))
			.add(new Field(LabelPrintingStaticField.STORAGE_LOCATION.getFieldId(), lotStorageLocationPropValue, FieldType.STATIC))
			.add(new Field(LabelPrintingStaticField.LOT_NOTES.getFieldId(), lotNotesPropValue, FieldType.STATIC)).build();
	}

	public static String getSeason(final String seasonStr) {
		final String value;
		if (seasonStr != null && Integer.parseInt(seasonStr.trim()) == TermId.SEASON_DRY.getId()) {
			value = Season.DRY.getLabel().toUpperCase();
		} else if (seasonStr != null && Integer.parseInt(seasonStr.trim()) == TermId.SEASON_WET.getId()) {
			value = Season.WET.getLabel().toUpperCase();
		} else {
			value = Season.GENERAL.getLabel().toUpperCase();
		}
		return value;
	}

	public static void removePairIdVariables(final List<LabelType> labelTypes) {
		for (final LabelType labelType : labelTypes) {
			final Iterator<Field> fieldIterator = labelType.getFields().iterator();
			while (fieldIterator.hasNext()) {
				if (PAIR_ID_VARIABLES.contains(fieldIterator.next().getId())) {
					fieldIterator.remove();
				}
			}
		}
	}

	public static String getDefaultFileName(final StudyDetails studyDetails, final DatasetDTO datasetDTO) {
		final String fileName = "Labels-for-".concat(studyDetails.getStudyName()).concat("-").concat(datasetDTO.getName());
		return FileUtils.cleanFileName(fileName);
	}

	public static Optional<ObservationUnitData> getObservationUnitData(final Map<String, ObservationUnitData> variableMap, final Field field) {
		return variableMap.get(field.getName()) != null ?
			Optional.of(variableMap.get(field.getName())) : variableMap.values().stream() //
			.filter(observationUnitData -> //
				observationUnitData.getVariableId() != null && observationUnitData.getVariableId().equals(field.getId())).findAny();
	}
}
