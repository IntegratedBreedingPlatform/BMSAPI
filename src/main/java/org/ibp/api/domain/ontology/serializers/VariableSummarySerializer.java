package org.ibp.api.domain.ontology.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.Strings;
import org.ibp.api.domain.ontology.VariableSummary;
import org.ibp.api.domain.ontology.VariableType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

public class VariableSummarySerializer extends JsonSerializer<VariableSummary>{

	@Override
	public void serialize(VariableSummary variableSummary, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
			throws IOException {

		jsonGenerator.writeStartObject();

		if(!isNullOrEmpty(variableSummary.getId())){
			jsonGenerator.writeStringField("id", variableSummary.getId());
		}
		jsonGenerator.writeStringField("name", variableSummary.getName());

		jsonGenerator.writeStringField("alias", variableSummary.getAlias() == null ? "" : variableSummary.getAlias());

		jsonGenerator.writeStringField("description", variableSummary.getDescription() == null ? "" : variableSummary.getDescription());

		if(!isNullOrEmpty(variableSummary.getPropertySummary())){
			jsonGenerator.writeObjectFieldStart("propertySummary");
			jsonGenerator.writeStringField("id", variableSummary.getPropertySummary().getId());
			jsonGenerator.writeStringField("name", variableSummary.getPropertySummary().getName());
			jsonGenerator.writeEndObject();
		}

		if(!isNullOrEmpty(variableSummary.getMethodSummary())){
			jsonGenerator.writeObjectFieldStart("methodSummary");
			jsonGenerator.writeStringField("id", variableSummary.getMethodSummary().getId());
			jsonGenerator.writeStringField("name", variableSummary.getMethodSummary().getName());
			jsonGenerator.writeEndObject();
		}

		if(!isNullOrEmpty(variableSummary.getScaleSummary())){
			jsonGenerator.writeObjectFieldStart("scaleSummary");
			jsonGenerator.writeStringField("id", variableSummary.getScaleSummary().getId());
			jsonGenerator.writeStringField("name", variableSummary.getScaleSummary().getName());

			if(variableSummary.getScaleSummary().getDataType() != null){
				jsonGenerator.writeObjectFieldStart("dataType");
				jsonGenerator.writeStringField("id", variableSummary.getScaleSummary().getDataType().getId());
				jsonGenerator.writeStringField("name", variableSummary.getScaleSummary().getDataType().getName());
				jsonGenerator.writeEndObject();
			}

			jsonGenerator.writeEndObject();
		}

		if(!variableSummary.getVariableTypes().isEmpty()){
			jsonGenerator.writeArrayFieldStart("variableTypes");

			for(VariableType variableType : variableSummary.getVariableTypes()){
				jsonGenerator.writeStartObject();
				jsonGenerator.writeNumberField("id", variableType.getId());
				jsonGenerator.writeStringField("name", variableType.getName());
				jsonGenerator.writeStringField("description", variableType.getDescription());
				jsonGenerator.writeEndObject();
			}

			jsonGenerator.writeEndArray();
		}

		jsonGenerator.writeBooleanField("favourite", variableSummary.isFavourite());

		jsonGenerator.writeObjectFieldStart("metadata");
			jsonGenerator.writeStringField("dateCreated", variableSummary.getMetadata().getDateCreated());
			jsonGenerator.writeStringField("lastModified", variableSummary.getMetadata().getDateLastModified());
		jsonGenerator.writeEndObject();

		jsonGenerator.writeObjectFieldStart("expectedRange");
			if(!isNullOrEmpty(variableSummary.getExpectedRange().getMin())){
				BigDecimal min = new BigDecimal(variableSummary.getExpectedRange().getMin());
				jsonGenerator.writeNumberField("min", min);
			}
			if(!isNullOrEmpty(variableSummary.getExpectedRange().getMax())){
				BigDecimal max = new BigDecimal(variableSummary.getExpectedRange().getMax());
				jsonGenerator.writeNumberField("max", max);
			}

		jsonGenerator.writeEndObject();

		jsonGenerator.writeEndObject();
	}

	protected boolean isNullOrEmpty(Object value) {
		return value instanceof String && Strings.isNullOrEmpty(((String) value).trim()) || value == null || value instanceof Collection
				&& ((Collection) value).isEmpty() || value instanceof Map && ((Map) value).isEmpty();
	}
}
