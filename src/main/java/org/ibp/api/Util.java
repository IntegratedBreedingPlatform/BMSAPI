package org.ibp.api;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.rest.dataset.ObservationUnitData;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Util {

	public static boolean isNullOrEmpty(final Object value) {
		return value instanceof String && Strings.isNullOrEmpty(((String) value).trim()) || value == null || value instanceof Collection
				&& ((Collection) value).isEmpty() || value instanceof Map && ((Map) value).isEmpty();
	}

	public static boolean isPositiveInteger(final String str) {
		if (StringUtils.isEmpty(str)) {
			return false;
		}
		return str.chars().allMatch(Character::isDigit);
	}

	public static <T> String buildErrorMessageFromList(final List<T> elements, final Integer elementsToShow) {
		final StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(elements.stream().limit(elementsToShow).map(Object::toString).collect(Collectors.joining(", ")));

		if (elements.size() > elementsToShow) {
			stringBuilder.append(" and ").append(elements.size() - elementsToShow).append(" more");
		}

		return stringBuilder.toString();
	}

	public static <T> long countNullElements(final List<T> list) {
		return list.stream().filter(Objects::isNull).count();
	}

	public static long countNullOrEmptyStrings(final List<String> list) {
		return list.stream().filter(s -> StringUtils.isEmpty(s)).count();
	}

	public static <T> boolean areAllUnique(final List<T> list) {
		return list.stream().allMatch(new HashSet<>()::add);
	}

	public static ObservationUnitData getObservationUnitData(final Map<String, ObservationUnitData> variables, final MeasurementVariable column) {
		final String key = variables.containsKey(column.getName()) ? column.getName() : column.getAlias();
		return variables.get(key);
	}
}
