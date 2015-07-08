package org.ibp.api.domain.ontology.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.Strings;
import org.ibp.api.domain.ontology.ScaleSummary;
import org.ibp.api.domain.ontology.TermSummary;
import org.ibp.api.java.ontology.ModelService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

public class ScaleSummarySerializer extends JsonSerializer<ScaleSummary>{

	@Autowired
	private ModelService modelService;

	@Override
	public void serialize(ScaleSummary scaleSummary, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

		jsonGenerator.writeStartObject();

		// Added common term field
		if(!isNullOrEmpty(scaleSummary.getId())){
			jsonGenerator.writeStringField("id", scaleSummary.getId());
		}
		jsonGenerator.writeStringField("name", scaleSummary.getName());

		jsonGenerator.writeStringField("description", scaleSummary.getDescription() == null ? "" : scaleSummary.getDescription());

		if(scaleSummary.getDataType() != null){

			// If dataType is not null then added data type related details
			jsonGenerator.writeObjectFieldStart("dataType");
				jsonGenerator.writeStringField("id", scaleSummary.getDataType().getId());
				jsonGenerator.writeStringField("name", scaleSummary.getDataType().getName());
				jsonGenerator.writeBooleanField("systemDataType", scaleSummary.getDataType().isSystemDataType());
			jsonGenerator.writeEndObject();

			// Adding valid values
			jsonGenerator.writeObjectFieldStart("validValues");

			// If numeric data type then adding min and max in valid values
			if(modelService.isNumericDataType(scaleSummary.getDataType().getId())){
				if(!isNullOrEmpty(scaleSummary.getValidValues().getMin())){
					BigDecimal min = new BigDecimal(scaleSummary.getValidValues().getMin());
					jsonGenerator.writeNumberField("min", min);
				}

				if(!isNullOrEmpty(scaleSummary.getValidValues().getMax())){
					BigDecimal max = new BigDecimal(scaleSummary.getValidValues().getMax());
					jsonGenerator.writeNumberField("max", max);
				}
			} else if(modelService.isCategoricalDataType(scaleSummary.getDataType().getId())){
				// If categorical data type then adding categories in valid values
				if(!scaleSummary.getValidValues().getCategories().isEmpty()){
					jsonGenerator.writeArrayFieldStart("categories");

					for (TermSummary category : scaleSummary.getValidValues().getCategories()){
						jsonGenerator.writeStartObject();
							jsonGenerator.writeStringField("id", category.getId());
							jsonGenerator.writeStringField("name", category.getName());
							jsonGenerator.writeStringField("description", category.getDescription());
						jsonGenerator.writeEndObject();
					}

					jsonGenerator.writeEndArray();
				}
			}

			jsonGenerator.writeEndObject();
		}

		// Adding metadata related to scale
		jsonGenerator.writeObjectFieldStart("metadata");
			jsonGenerator.writeStringField("dateCreated", scaleSummary.getMetadata().getDateCreated());
			jsonGenerator.writeStringField("lastModified", scaleSummary.getMetadata().getDateLastModified());
		jsonGenerator.writeEndObject();

		jsonGenerator.writeEndObject();
	}

	protected boolean isNullOrEmpty(Object value) {
		return value instanceof String && Strings.isNullOrEmpty(((String) value).trim()) || value == null || value instanceof Collection
				&& ((Collection) value).isEmpty() || value instanceof Map && ((Map) value).isEmpty();
	}
}
