package org.ibp.api.domain.ontology.serializers;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.ibp.api.Util;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.domain.ontology.TermSummary;
import org.ibp.api.java.ontology.ModelService;
import org.springframework.beans.factory.annotation.Autowired;

public class ScaleDetailsSerializer extends JsonSerializer<ScaleDetails>{

	@Autowired
	private ModelService modelService;

	@Override
	public void serialize(ScaleDetails scaleDetails, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

		jsonGenerator.writeStartObject();

		// Added common term field
		if(!Util.isNullOrEmpty(scaleDetails.getId())){
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
				if(!Util.isNullOrEmpty(scaleDetails.getValidValues().getMin())){
					BigDecimal min = new BigDecimal(scaleDetails.getValidValues().getMin());
					jsonGenerator.writeNumberField("min", min);
				}

				if(!Util.isNullOrEmpty(scaleDetails.getValidValues().getMax())){
					BigDecimal max = new BigDecimal(scaleDetails.getValidValues().getMax());
					jsonGenerator.writeNumberField("max", max);
				}
			} else if(modelService.isCategoricalDataType(scaleDetails.getDataType().getId())){
				// If categorical data type then adding categories in valid values
				if(!scaleDetails.getValidValues().getCategories().isEmpty()){
					jsonGenerator.writeArrayFieldStart("categories");

					for (TermSummary category : scaleDetails.getValidValues().getCategories()){
						Util.serializeTermSummary(jsonGenerator, category);
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
			if(!Util.isNullOrEmpty(scaleDetails.getMetadata().getEditableFields())){
				for(String field : scaleDetails.getMetadata().getEditableFields()){
					jsonGenerator.writeString(field);
				}
			}
			jsonGenerator.writeEndArray();
			jsonGenerator.writeBooleanField("deletable", scaleDetails.getMetadata().isDeletable());

			jsonGenerator.writeObjectFieldStart("usage");
				jsonGenerator.writeNumberField("observations", !Util.isNullOrEmpty(scaleDetails.getMetadata().getUsage().getObservations()) ? scaleDetails.getMetadata().getUsage().getObservations() : 0);
				jsonGenerator.writeNumberField("studies", !Util.isNullOrEmpty(scaleDetails.getMetadata().getUsage().getObservations()) ? scaleDetails.getMetadata().getUsage().getObservations() : 0);

				if(!Util.isNullOrEmpty(scaleDetails.getMetadata().getUsage().getVariables())){
					jsonGenerator.writeArrayFieldStart("variables");
					for(TermSummary variable : scaleDetails.getMetadata().getUsage().getVariables()){
						Util.serializeTermSummary(jsonGenerator, variable);
					}
					jsonGenerator.writeEndArray();
				}
			jsonGenerator.writeEndObject();

		jsonGenerator.writeEndObject();

		jsonGenerator.writeEndObject();
	}

}
