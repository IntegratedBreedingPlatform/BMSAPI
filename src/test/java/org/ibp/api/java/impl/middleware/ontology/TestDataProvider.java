
package org.ibp.api.java.impl.middleware.ontology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.OntologyVariableSummary;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.oms.VariableType;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.ibp.api.domain.ontology.MetadataSummary;
import org.ibp.api.domain.ontology.MethodSummary;
import org.ibp.api.domain.ontology.PropertySummary;
import org.ibp.api.domain.ontology.ScaleSummary;
import org.ibp.api.domain.ontology.VariableSummary;

public class TestDataProvider {

	private static final Integer variableId = 15;
	private static final String variableName = "Variable Name";
	private static final String variableDescription = "Variable Description";
	private static final String variableAlias = "VA";
	private static final boolean variableIsFavourite = false;
	private static final String programUUID = "abcd";
	private static final Integer variableObservations = 0;
	private static final Integer variableStudies = 0;
	private static final String variableExpectedMin = "12";
	private static final String variableExpectedMax = "16";

	private static final Integer methodId = 10;
	private static final String methodName = "Method Name";
	private static final String methodDescription = "Method Description";

	private static final Integer propertyId = 11;
	private static final String propertyName = "Property Name";
	private static final String propertyDescription = "Property Description";
	private static final String cropOntologyId = "CO:1234567890";

	private static final Integer scaleId = 12;
	private static final String scaleName = "Scale Name";
	private static final String scaleDescription = "Scale Description";
	private static final Integer scaleVocabularyId = 1030;

	private static final String scaleMinValue = "10.01";
	private static final String scaleMaxValue = "20.02";
	private static final String category1 = "Scale Category1";
	private static final String category2 = "Scale Category2";

	private static final String className1 = "Agronomic";
	private static final String className2 = "Biotic Stress";
	private static final String className3 = "Study condition";

	public static final List<Term> mwTermList = new ArrayList<>(Arrays.asList(new Term(1, TestDataProvider.className1, ""), new Term(2,
			TestDataProvider.className2, ""), new Term(3, TestDataProvider.className3, "")));

	public static final TermSummary mwMethodSummary = new TermSummary(TestDataProvider.methodId, TestDataProvider.methodName,
			TestDataProvider.methodDescription);
	public static final TermSummary mwPropertySummary = new TermSummary(TestDataProvider.propertyId, TestDataProvider.propertyName,
			TestDataProvider.propertyDescription);

	public static final org.ibp.api.domain.ontology.DataType numericalDataType = new org.ibp.api.domain.ontology.DataType(
			DataType.NUMERIC_VARIABLE.getId(), DataType.NUMERIC_VARIABLE.getName());
	public static final org.ibp.api.domain.ontology.DataType categoricalDataType = new org.ibp.api.domain.ontology.DataType(
			DataType.CATEGORICAL_VARIABLE.getId(), DataType.CATEGORICAL_VARIABLE.getName());

	public static final org.ibp.api.domain.ontology.VariableType traitVariable = new org.ibp.api.domain.ontology.VariableType(1,
			"Trait Variable", "Variable for trait study");

	public static Term getMethodTerm() {
		return new Term(TestDataProvider.methodId, TestDataProvider.methodName, TestDataProvider.methodDescription, CvId.METHODS.getId(),
				null);
	}

	public static Term getPropertyTerm() {
		return new Term(TestDataProvider.propertyId, TestDataProvider.propertyName, TestDataProvider.propertyDescription,
				CvId.PROPERTIES.getId(), null);
	}

	public static Term getScaleTerm() {
		return new Term(TestDataProvider.scaleId, TestDataProvider.scaleName, TestDataProvider.scaleDescription, CvId.SCALES.getId(), null);
	}

	public static Term getVariableTerm() {
		return new Term(TestDataProvider.variableId, TestDataProvider.variableName, TestDataProvider.variableDescription,
				CvId.VARIABLES.getId(), false);
	}

	public static Date getDateCreated() {
		Calendar dateCreated = Calendar.getInstance();
		dateCreated.set(2015, Calendar.JANUARY, 1);
		return dateCreated.getTime();
	}

	public static Date getDateModified() {
		Calendar dateLastModified = Calendar.getInstance();
		dateLastModified.set(2015, Calendar.JANUARY, 2);
		return dateLastModified.getTime();
	}

	public static Method getTestMethod() {
		Method method = new Method();
		method.setId(TestDataProvider.methodId);
		method.setName(TestDataProvider.methodName);
		method.setDefinition(TestDataProvider.methodDescription);
		method.setDateCreated(TestDataProvider.getDateCreated());
		method.setDateLastModified(TestDataProvider.getDateModified());
		return method;
	}

	public static List<Method> getTestMethodList(Integer elements) {
		List<Method> methods = new ArrayList<>();
		for (Integer count = 0; count < elements; count++) {
			Integer methodId = 100 + count;
			Method method = new Method();
			method.setId(methodId);
			method.setName(TestDataProvider.methodName + methodId);
			method.setDefinition(TestDataProvider.methodDescription);
			method.setDateCreated(TestDataProvider.getDateCreated());
			method.setDateLastModified(TestDataProvider.getDateModified());
			methods.add(method);
		}
		return methods;
	}

	public static Property getTestProperty() {
		Property property = new Property();
		property.setId(TestDataProvider.propertyId);
		property.setName(TestDataProvider.propertyName);
		property.setDefinition(TestDataProvider.propertyDescription);
		property.setCropOntologyId(TestDataProvider.cropOntologyId);
		property.addClass(TestDataProvider.className1);
		property.setDateCreated(TestDataProvider.getDateCreated());
		property.setDateLastModified(TestDataProvider.getDateModified());
		return property;
	}

	public static List<Property> getTestProperties(Integer elements) {
		List<Property> properties = new ArrayList<>();

		for (Integer count = 0; count < elements; count++) {
			Property property = new Property();
			property.setId(TestDataProvider.propertyId);
			property.setName(TestDataProvider.propertyName);
			property.setDefinition(TestDataProvider.propertyDescription);
			property.setCropOntologyId(TestDataProvider.cropOntologyId);
			property.addClass(TestDataProvider.className1);
			property.setDateCreated(TestDataProvider.getDateCreated());
			property.setDateLastModified(TestDataProvider.getDateModified());
			properties.add(property);
		}
		return properties;
	}

	public static Scale getTestScale() {
		Scale scale = new Scale();
		scale.setId(TestDataProvider.scaleId);
		scale.setName(TestDataProvider.scaleName);
		scale.setVocabularyId(TestDataProvider.scaleVocabularyId);
		scale.setDefinition(TestDataProvider.scaleDescription);
		scale.setDataType(DataType.NUMERIC_VARIABLE);
		scale.setMinValue(TestDataProvider.scaleMinValue);
		scale.setMaxValue(TestDataProvider.scaleMaxValue);
		scale.addCategory(TestDataProvider.category1, TestDataProvider.category2);
		scale.setDateCreated(TestDataProvider.getDateCreated());
		scale.setDateLastModified(TestDataProvider.getDateModified());
		return scale;
	}

	public static List<Scale> getTestScales(Integer elements) {
		List<Scale> scaleList = new ArrayList<>();

		for (Integer count = 0; count < elements; count++) {
			Scale scale = new Scale();
			scale.setId(TestDataProvider.scaleId);
			scale.setName(TestDataProvider.scaleName);
			scale.setDefinition(TestDataProvider.scaleDescription);
			scale.setDataType(DataType.NUMERIC_VARIABLE);
			scale.setMinValue(TestDataProvider.scaleMinValue);
			scale.setMaxValue(TestDataProvider.scaleMaxValue);
			scale.addCategory(TestDataProvider.category1, TestDataProvider.category2);
			scale.setDateCreated(TestDataProvider.getDateCreated());
			scale.setDateLastModified(TestDataProvider.getDateModified());
			scaleList.add(scale);
		}
		return scaleList;
	}

	public static Variable getTestVariable() {
		Variable variable = new Variable();
		variable.setId(TestDataProvider.variableId);
		variable.setName(TestDataProvider.variableName);
		variable.setDefinition(TestDataProvider.variableDescription);
		variable.setObservations(TestDataProvider.variableObservations);
		variable.setProperty(TestDataProvider.getTestProperty());
		variable.setMethod(TestDataProvider.getTestMethod());
		variable.setScale(TestDataProvider.getTestScale());
		variable.setMinValue(TestDataProvider.variableExpectedMin);
		variable.setMaxValue(TestDataProvider.variableExpectedMax);
		variable.setAlias(TestDataProvider.variableAlias);
		variable.setIsFavorite(TestDataProvider.variableIsFavourite);
		variable.setDateCreated(TestDataProvider.getDateCreated());
		variable.setStudies(TestDataProvider.variableStudies);
		variable.addVariableType(VariableType.getById(1));
		variable.setDateCreated(TestDataProvider.getDateCreated());
		variable.setDateLastModified(TestDataProvider.getDateModified());

		return variable;
	}

	public static List<OntologyVariableSummary> getTestVariables(Integer elements) {
		List<OntologyVariableSummary> variableList = new ArrayList<>();

		for (Integer count = 0; count < elements; count++) {
			OntologyVariableSummary variable =
					new OntologyVariableSummary(TestDataProvider.variableId + count, TestDataProvider.variableName + count,
							TestDataProvider.variableDescription + count);
			variable.setMinValue(TestDataProvider.variableExpectedMin);
			variable.setMaxValue(TestDataProvider.variableExpectedMax);
			variable.setAlias(TestDataProvider.variableAlias);
			variable.setIsFavorite(TestDataProvider.variableIsFavourite);
			variable.setDateCreated(TestDataProvider.getDateCreated());
			variable.setPropertySummary(TestDataProvider.mwPropertySummary);
			variable.setMethodSummary(TestDataProvider.mwMethodSummary);
			variable.setScaleSummary(TestDataProvider.getTestScale());
			variable.setDateCreated(TestDataProvider.getDateCreated());
			variable.setDateLastModified(TestDataProvider.getDateModified());
			variableList.add(variable);
		}
		return variableList;
	}

	public static MetadataSummary getTestMetadataSummary() {
		MetadataSummary metadataSummary = new MetadataSummary();
		metadataSummary.setDateCreated(TestDataProvider.getDateCreated());
		metadataSummary.setDateLastModified(TestDataProvider.getDateModified());
		return metadataSummary;
	}

	public static MethodSummary getTestMethodSummary() {
		MethodSummary methodSummary = new MethodSummary();
		methodSummary.setId(String.valueOf(TestDataProvider.methodId));
		methodSummary.setName(TestDataProvider.methodName);
		methodSummary.setDescription(TestDataProvider.methodDescription);
		methodSummary.setMetadata(TestDataProvider.getTestMetadataSummary());
		return methodSummary;
	}

	public static PropertySummary getTestPropertySummary() {
		PropertySummary propertySummary = new PropertySummary();
		propertySummary.setId(String.valueOf(TestDataProvider.propertyId));
		propertySummary.setName(TestDataProvider.propertyName);
		propertySummary.setDescription(TestDataProvider.propertyDescription);
		propertySummary.setClasses(new HashSet<>(Collections.singletonList(TestDataProvider.className1)));
		propertySummary.setCropOntologyId(TestDataProvider.cropOntologyId);
		propertySummary.setMetadata(TestDataProvider.getTestMetadataSummary());
		return propertySummary;
	}

	public static ScaleSummary getTestScaleSummary() {
		ScaleSummary scaleSummary = new ScaleSummary();
		scaleSummary.setId(String.valueOf(TestDataProvider.scaleId));
		scaleSummary.setName(TestDataProvider.scaleName);
		scaleSummary.setDescription(TestDataProvider.scaleDescription);
		scaleSummary.setDataType(TestDataProvider.numericalDataType);
		scaleSummary.setMin(TestDataProvider.scaleMinValue);
		scaleSummary.setMax(TestDataProvider.scaleMaxValue);
		scaleSummary.setMetadata(TestDataProvider.getTestMetadataSummary());
		return scaleSummary;
	}

	public static VariableSummary getTestVariableSummary() {
		VariableSummary variableSummary = new VariableSummary();
		variableSummary.setProgramUuid(TestDataProvider.programUUID);
		variableSummary.setId(String.valueOf(TestDataProvider.variableId));
		variableSummary.setName(TestDataProvider.variableName);
		variableSummary.setDescription(TestDataProvider.variableDescription);
		variableSummary.setAlias(TestDataProvider.variableAlias);
		variableSummary.setVariableTypes(new HashSet<>(Collections.singletonList(TestDataProvider.traitVariable)));
		variableSummary.setPropertySummary(new org.ibp.api.domain.ontology.TermSummary());
		variableSummary.getPropertySummary().setId(String.valueOf(TestDataProvider.propertyId));
		variableSummary.getPropertySummary().setName(TestDataProvider.propertyName);
		variableSummary.getPropertySummary().setDescription(TestDataProvider.propertyDescription);
		variableSummary.setMethodSummary(new org.ibp.api.domain.ontology.TermSummary());
		variableSummary.getMethodSummary().setId(String.valueOf(TestDataProvider.methodId));
		variableSummary.getMethodSummary().setName(TestDataProvider.methodName);
		variableSummary.getMethodSummary().setDescription(TestDataProvider.methodDescription);
		variableSummary.getScaleSummary().setId(String.valueOf(TestDataProvider.scaleId));
		variableSummary.getScaleSummary().setName(TestDataProvider.scaleName);
		variableSummary.getScaleSummary().setDescription(TestDataProvider.scaleDescription);
		variableSummary.getScaleSummary().setDataType(TestDataProvider.numericalDataType);
		variableSummary.setExpectedMin(TestDataProvider.variableExpectedMin);
		variableSummary.setExpectedMax(TestDataProvider.variableExpectedMax);
		variableSummary.setFavourite(TestDataProvider.variableIsFavourite);
		return variableSummary;
	}
}
