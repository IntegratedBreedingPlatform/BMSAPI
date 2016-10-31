package org.ibp.api.domain.ontology.serializers;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import org.ibp.api.domain.ontology.Category;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.domain.ontology.TermSummary;
import org.ibp.api.java.ontology.ModelService;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.Strings;

public class ScaleDetailsSerializer extends JsonSerializer<ScaleDetails>{

	@Autowired
	private ModelService modelService;

	@Override
	public void serialize(ScaleDetails scaleDetails, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

		jsonGenerator.writeStartObject();

		// Added common term field
		if(!isNullOrEmpty(scaleDetails.getId())){
			jsonGenerator.writeStringField("id", scaleDetails.getId());
		}
		jsonGenerator.writeStringField("name", scaleDetails.getName());

		jsonGenerator.writeStringField("description", scaleDetails.getDescription() == null ? "" : scaleDetails.getDescription());

		if(scaleDetails.getDataType() != null){

			// If dataType is not null then added data type related details
			jsonGenerator.writeObjectFieldStart("dataType");
				jsonGenerator.writeStringField("id", scaleDetails.getDataType().getId());
				jsonGenerator.writeStringField("name", scaleDetails.getDataType().getName());
				jsonGenerator.writeBooleanField("systemDataType", scaleDetails.getDataType().isSystemDataType());
			jsonGenerator.writeEndObject();

			// Adding valid values
			jsonGenerator.writeObjectFieldStart("validValues");

			// If numeric data type then adding min and max in valid values
			if(modelService.isNumericDataType(scaleDetails.getDataType().getId())){
				if(!isNullOrEmpty(scaleDetails.getValidValues().getMin())){
					BigDecimal min = new BigDecimal(scaleDetails.getValidValues().getMin());
					jsonGenerator.writeNumberField("min", min);
				}

				if(!isNullOrEmpty(scaleDetails.getValidValues().getMax())){
					BigDecimal max = new BigDecimal(scaleDetails.getValidValues().getMax());
					jsonGenerator.writeNumberField("max", max);
				}
			} else if(modelService.isCategoricalDataType(scaleDetails.getDataType().getId())){
				// If categorical data type then adding categories in valid values
				if(!scaleDetails.getValidValues().getCategories().isEmpty()){
					jsonGenerator.writeArrayFieldStart("categories");

					for (Category category : scaleDetails.getValidValues().getCategories()){
						jsonGenerator.writeStartObject();
							jsonGenerator.writeStringField("id", category.getId());
							jsonGenerator.writeStringField("name", category.getName());
							jsonGenerator.writeStringField("description", category.getDescription());
						    jsonGenerator.writeBooleanField("editable", category.isEditable());
						jsonGenerator.writeEndObject();
					}

					jsonGenerator.writeEndArray();
				}
			}

			jsonGenerator.writeEndObject();
		}

		// Adding metadata related to scale
		jsonGenerator.writeObjectFieldStart("metadata");
			jsonGenerator.writeStringField("dateCreated", scaleDetails.getMetadata().getDateCreated());
			jsonGenerator.writeStringField("lastModified", scaleDetails.getMetadata().getDateLastModified());
			jsonGenerator.writeArrayFieldStart("editableFields");
			if(!isNullOrEmpty(scaleDetails.getMetadata().getEditableFields())){
				for(String field : scaleDetails.getMetadata().getEditableFields()){
					jsonGenerator.writeString(field);
				}
			}
			jsonGenerator.writeEndArray();
			jsonGenerator.writeBooleanField("deletable", scaleDetails.getMetadata().isDeletable());
			jsonGenerator.writeBooleanField("editable", scaleDetails.getMetadata().isEditable());

			jsonGenerator.writeObjectFieldStart("usage");
				jsonGenerator.writeNumberField("observations", !isNullOrEmpty(scaleDetails.getMetadata().getUsage().getObservations()) ? scaleDetails.getMetadata().getUsage().getObservations() : 0);
				jsonGenerator.writeNumberField("studies", !isNullOrEmpty(scaleDetails.getMetadata().getUsage().getObservations()) ? scaleDetails.getMetadata().getUsage().getObservations() : 0);

				if(!isNullOrEmpty(scaleDetails.getMetadata().getUsage().getVariables())){
					jsonGenerator.writeArrayFieldStart("variables");
					for(TermSummary variable : scaleDetails.getMetadata().getUsage().getVariables()){
						jsonGenerator.writeStartObject();
						jsonGenerator.writeStringField("id", variable.getId());
						jsonGenerator.writeStringField("name", variable.getName());
						jsonGenerator.writeStringField("description", variable.getDescription());
						jsonGenerator.writeEndObject();
					}
					jsonGenerator.writeEndArray();
				}
			jsonGenerator.writeEndObject();


		jsonGenerator.writeEndObject();

		jsonGenerator.writeEndObject();
	}

	protected boolean isNullOrEmpty(Object value) {
		return value instanceof String && Strings.isNullOrEmpty(((String) value).trim()) || value == null || value instanceof Collection
				&& ((Collection) value).isEmpty() || value instanceof Map && ((Map) value).isEmpty();
	}
}
