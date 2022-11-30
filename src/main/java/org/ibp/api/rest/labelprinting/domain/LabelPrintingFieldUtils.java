package org.ibp.api.rest.labelprinting.domain;

import org.ibp.api.rest.labelprinting.LabelPrintingStrategy;

public abstract class LabelPrintingFieldUtils {

	public static final String UNDERSCORE = "_";
	/**
	 * Given field, it will get a String combing FieldType.name + '_' + field.Id
	 *
	 * @param field
	 * @return String
	 */
	public static String transformToCombinedKey(final Field field) {
		return field.getFieldType().getName() + LabelPrintingFieldUtils.UNDERSCORE + field.getId();
	}

	/**
	 * Given combinedKey( FieldType.name + '_' + field.Id ), it will get the Id of a Field
	 *
	 * @param Integer
	 * @return String
	 */
	public static Integer getFieldIdFromCombinedKey(final String combinedKey) {
		final String[] keys = combinedKey.split(LabelPrintingFieldUtils.UNDERSCORE);
		return Integer.valueOf(keys[1]);
	}

	/**
	 * Given combinedKey( FieldType.name + '_' + field.Id ), it will get the FieldType (VARIABLE/STATIC/NAME)
	 *
	 * @param String
	 * @return String
	 */
	public static String getFieldTypeNameFromCombinedKey(final String combinedKey) {
		final String[] keys = combinedKey.split(LabelPrintingFieldUtils.UNDERSCORE);
		return keys[0];
	}
}


