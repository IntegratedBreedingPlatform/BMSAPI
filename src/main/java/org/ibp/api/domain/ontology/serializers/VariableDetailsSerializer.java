package org.ibp.api.domain.ontology.serializers;

import java.io.IOException;
import java.math.BigDecimal;

import org.ibp.api.Util;
import org.ibp.api.domain.ontology.TermSummary;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableType;
import org.ibp.api.java.ontology.ModelService;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class VariableDetailsSerializer extends JsonSerializer<VariableDetails>{

	@Autowired
	private ModelService modelService;

	@Override
	public void serialize(VariableDetails variableDetails, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
			throws IOException {

		jsonGenerator.writeStartObject();

		if (!Util.isNullOrEmpty(variableDetails.getId())) {
			jsonGenerator.writeStringField("id", variableDetails.getId());
		}
		jsonGenerator.writeStringField("name", variableDetails.getName());

		jsonGenerator.writeStringField("alias", variableDetails.getAlias() == null ? "" : variableDetails.getAlias());

		jsonGenerator.writeStringField("description", variableDetails.getDescription() == null ? "" : variableDetails.getDescription());

		if (!Util.isNullOrEmpty(variableDetails.getProperty())) {
			jsonGenerator.writeObjectFieldStart("property");
			jsonGenerator.writeStringField("id", variableDetails.getProperty().getId());
			jsonGenerator.writeStringField("name", variableDetails.getProperty().getName());
			jsonGenerator.writeStringField("description", variableDetails.getProperty().getDescription());
			jsonGenerator.writeStringField("cropOntologyId", variableDetails.getProperty().getCropOntologyId() != null ? variableDetails.getProperty().getCropOntologyId() : null);

			jsonGenerator.writeArrayFieldStart("classes");
			if (!Util.isNullOrEmpty(variableDetails.getProperty().getClasses().isEmpty())) {
				for(String propertyClass : variableDetails.getProperty().getClasses()){
					jsonGenerator.writeString(propertyClass);
				}
			}
			jsonGenerator.writeEndArray();

			// Adding metadata related to property
			jsonGenerator.writeObjectFieldStart("metadata");
			jsonGenerator.writeStringField("dateCreated", variableDetails.getProperty().getMetadata().getDateCreated());
			jsonGenerator.writeStringField("lastModified", variableDetails.getProperty().getMetadata().getDateLastModified());
			jsonGenerator.writeArrayFieldStart("editableFields");
			if (!Util.isNullOrEmpty(variableDetails.getProperty().getMetadata().getEditableFields())) {
				for(String field : variableDetails.getProperty().getMetadata().getEditableFields()){
					jsonGenerator.writeString(field);
				}
			}
			jsonGenerator.writeEndArray();
			jsonGenerator.writeBooleanField("deletable", variableDetails.getProperty().getMetadata().isDeletable());

			jsonGenerator.writeObjectFieldStart("usage");
			jsonGenerator.writeNumberField("observations",
					!Util.isNullOrEmpty(variableDetails.getProperty().getMetadata().getUsage().getObservations()) ?
							variableDetails.getProperty().getMetadata().getUsage().getObservations() :
							0);
			jsonGenerator.writeNumberField("studies",
					!Util.isNullOrEmpty(variableDetails.getProperty().getMetadata().getUsage().getStudies()) ?
							variableDetails.getProperty().getMetadata().getUsage().getStudies() :
							0);

			if (!Util.isNullOrEmpty(variableDetails.getProperty().getMetadata().getUsage().getVariables())) {
				jsonGenerator.writeArrayFieldStart("variables");
				for(TermSummary variable : variableDetails.getProperty().getMetadata().getUsage().getVariables()){
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

		if (!Util.isNullOrEmpty(variableDetails.getMethod())) {
			jsonGenerator.writeObjectFieldStart("method");
			jsonGenerator.writeStringField("id", variableDetails.getMethod().getId());
			jsonGenerator.writeStringField("name", variableDetails.getMethod().getName());
			jsonGenerator.writeStringField("description", variableDetails.getMethod().getDescription());


			// Adding metadata related to method
			jsonGenerator.writeObjectFieldStart("metadata");
			jsonGenerator.writeStringField("dateCreated", variableDetails.getMethod().getMetadata().getDateCreated());
			jsonGenerator.writeStringField("lastModified", variableDetails.getMethod().getMetadata().getDateLastModified());
			jsonGenerator.writeArrayFieldStart("editableFields");
			if (!Util.isNullOrEmpty(variableDetails.getMethod().getMetadata().getEditableFields())) {
				for(String field : variableDetails.getMethod().getMetadata().getEditableFields()){
					jsonGenerator.writeString(field);
				}
			}
			jsonGenerator.writeEndArray();
			jsonGenerator.writeBooleanField("deletable", variableDetails.getMethod().getMetadata().isDeletable());

			jsonGenerator.writeObjectFieldStart("usage");
			jsonGenerator.writeNumberField("observations",
					!Util.isNullOrEmpty(variableDetails.getMethod().getMetadata().getUsage().getObservations()) ?
							variableDetails.getMethod().getMetadata().getUsage().getObservations() :
							0);
			jsonGenerator.writeNumberField("studies",
					!Util.isNullOrEmpty(variableDetails.getMethod().getMetadata().getUsage().getStudies()) ?
							variableDetails.getMethod().getMetadata().getUsage().getStudies() :
							0);

			if (!Util.isNullOrEmpty(variableDetails.getMethod().getMetadata().getUsage().getVariables())) {
				jsonGenerator.writeArrayFieldStart("variables");
				for(TermSummary variable : variableDetails.getMethod().getMetadata().getUsage().getVariables()){
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

		if (!Util.isNullOrEmpty(variableDetails.getScale())) {
			jsonGenerator.writeObjectFieldStart("scale");
			jsonGenerator.writeStringField("id", variableDetails.getScale().getId());
			jsonGenerator.writeStringField("name", variableDetails.getScale().getName());
			jsonGenerator.writeStringField("description", variableDetails.getScale().getDescription());

			if(variableDetails.getScale().getDataType() != null){
				jsonGenerator.writeObjectFieldStart("dataType");
				jsonGenerator.writeStringField("id", variableDetails.getScale().getDataType().getId());
				jsonGenerator.writeStringField("name", variableDetails.getScale().getDataType().getName());
				jsonGenerator.writeBooleanField("systemDataType", variableDetails.getScale().getDataType().isSystemDataType());
				jsonGenerator.writeEndObject();
			}

			// Adding valid values
			jsonGenerator.writeObjectFieldStart("validValues");

			// If numeric data type then adding min and max in valid values
			if(modelService.isNumericDataType(variableDetails.getScale().getDataType().getId())){
				if (!Util.isNullOrEmpty(variableDetails.getScale().getValidValues().getMin())) {
					BigDecimal min = new BigDecimal(variableDetails.getScale().getValidValues().getMin());
					jsonGenerator.writeNumberField("min", min);
				}

				if (!Util.isNullOrEmpty(variableDetails.getScale().getValidValues().getMax())) {
					BigDecimal max = new BigDecimal(variableDetails.getScale().getValidValues().getMax());
					jsonGenerator.writeNumberField("max", max);
				}
			} else if(modelService.isCategoricalDataType(variableDetails.getScale().getDataType().getId())){
				// If categorical data type then adding categories in valid values
				if(!variableDetails.getScale().getValidValues().getCategories().isEmpty()){
					jsonGenerator.writeArrayFieldStart("categories");

					for (TermSummary category : variableDetails.getScale().getValidValues().getCategories()){
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

			// Adding metadata related to scale
			jsonGenerator.writeObjectFieldStart("metadata");
			jsonGenerator.writeStringField("dateCreated", variableDetails.getScale().getMetadata().getDateCreated());
			jsonGenerator.writeStringField("lastModified", variableDetails.getScale().getMetadata().getDateLastModified());
			jsonGenerator.writeArrayFieldStart("editableFields");
			if (!Util.isNullOrEmpty(variableDetails.getScale().getMetadata().getEditableFields())) {
				for(String field : variableDetails.getScale().getMetadata().getEditableFields()){
					jsonGenerator.writeString(field);
				}
			}
			jsonGenerator.writeEndArray();
			jsonGenerator.writeBooleanField("deletable", variableDetails.getScale().getMetadata().isDeletable());

			jsonGenerator.writeObjectFieldStart("usage");
			jsonGenerator.writeNumberField("observations",
					!Util.isNullOrEmpty(variableDetails.getScale().getMetadata().getUsage().getObservations()) ?
							variableDetails.getScale().getMetadata().getUsage().getObservations() :
							0);
			jsonGenerator.writeNumberField("studies",
					!Util.isNullOrEmpty(variableDetails.getScale().getMetadata().getUsage().getStudies()) ?
							variableDetails.getScale().getMetadata().getUsage().getStudies() :
							0);

			if (!Util.isNullOrEmpty(variableDetails.getScale().getMetadata().getUsage().getVariables())) {
				jsonGenerator.writeArrayFieldStart("variables");
				for(TermSummary variable : variableDetails.getScale().getMetadata().getUsage().getVariables()){
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

		if(!variableDetails.getVariableTypes().isEmpty()){
			jsonGenerator.writeArrayFieldStart("variableTypes");

			for(VariableType variableType : variableDetails.getVariableTypes()){
				jsonGenerator.writeStartObject();
				jsonGenerator.writeStringField("id", variableType.getId());
				jsonGenerator.writeStringField("name", variableType.getName());
				jsonGenerator.writeStringField("description", variableType.getDescription());
				jsonGenerator.writeEndObject();
			}

			jsonGenerator.writeEndArray();
		}

		jsonGenerator.writeBooleanField("favourite", variableDetails.isFavourite());

		// Adding metadata related to variables
		jsonGenerator.writeObjectFieldStart("metadata");
		jsonGenerator.writeArrayFieldStart("editableFields");
		if (!Util.isNullOrEmpty(variableDetails.getMetadata().getEditableFields())) {
			for (String field : variableDetails.getMetadata().getEditableFields()) {
				jsonGenerator.writeString(field);
			}
		}
		jsonGenerator.writeEndArray();
		jsonGenerator.writeBooleanField("deletable", variableDetails.getMetadata().isDeletable());

		jsonGenerator.writeStringField("dateCreated", variableDetails.getMetadata().getDateCreated());
		jsonGenerator.writeStringField("lastModified", variableDetails.getMetadata().getDateLastModified());

		jsonGenerator.writeObjectFieldStart("usage");
		jsonGenerator.writeNumberField("observations", !Util.isNullOrEmpty(variableDetails.getMetadata().getUsage().getObservations()) ?
				variableDetails.getMetadata().getUsage().getObservations() :
				0);
		jsonGenerator.writeNumberField("studies", !Util.isNullOrEmpty(variableDetails.getMetadata().getUsage().getStudies()) ?
				variableDetails.getMetadata().getUsage().getStudies() :
				0);
		jsonGenerator.writeNumberField("datasets", !Util.isNullOrEmpty(variableDetails.getMetadata().getUsage().getDatasets()) ?
				variableDetails.getMetadata().getUsage().getDatasets() :
				0);
		jsonGenerator.writeNumberField("germplasm", !Util.isNullOrEmpty(variableDetails.getMetadata().getUsage().getGermplasm()) ?
			variableDetails.getMetadata().getUsage().getGermplasm() :
			0);
		jsonGenerator.writeNumberField("breedingMethods", !Util.isNullOrEmpty(variableDetails.getMetadata().getUsage().getBreedingMethods()) ?
			variableDetails.getMetadata().getUsage().getBreedingMethods() :
			0);
		jsonGenerator.writeEndObject();
		jsonGenerator.writeEndObject();

		jsonGenerator.writeObjectFieldStart("expectedRange");
		if (!Util.isNullOrEmpty(variableDetails.getExpectedRange().getMin())) {
				BigDecimal min = new BigDecimal(variableDetails.getExpectedRange().getMin());
				jsonGenerator.writeNumberField("min", min);
			}
		if (!Util.isNullOrEmpty(variableDetails.getExpectedRange().getMax())) {
				BigDecimal max = new BigDecimal(variableDetails.getExpectedRange().getMax());
				jsonGenerator.writeNumberField("max", max);
			}

		jsonGenerator.writeEndObject();

		jsonGenerator.writeObjectField("formula", variableDetails.getFormula());
		jsonGenerator.writeBooleanField("allowsFormula", variableDetails.isAllowsFormula());

		jsonGenerator.writeEndObject();
	}

}
