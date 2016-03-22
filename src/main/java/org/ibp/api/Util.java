package org.ibp.api;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.Strings;
import org.ibp.api.domain.ontology.TermSummary;

public final class Util {

	private Util(){}

	public static boolean isNullOrEmpty(Object value) {

		boolean isNull = (value == null);
		boolean isStringEmpty = value instanceof String && Strings.isNullOrEmpty(((String) value).trim());
		boolean isCollectionEmpty = value instanceof Collection && ((Collection) value).isEmpty();
		boolean isMapEmpty = value instanceof Map && ((Map) value).isEmpty();

		return isNull || isStringEmpty || isCollectionEmpty || isMapEmpty;
	}

	public static void serializeTermSummary(JsonGenerator jsonGenerator, TermSummary termSummary) throws IOException {
		jsonGenerator.writeStartObject();
		jsonGenerator.writeStringField("id", termSummary.getId());
		jsonGenerator.writeStringField("name", termSummary.getName());
		jsonGenerator.writeStringField("description", termSummary.getDescription());
		jsonGenerator.writeEndObject();
	}
}
