
package org.ibp.api.java.impl.middleware.ontology;

import org.apache.commons.lang.math.RandomUtils;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.ibp.api.domain.ontology.MetadataDetails;
import org.ibp.api.domain.ontology.MethodDetails;
import org.ibp.api.domain.ontology.PropertyDetails;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.domain.ontology.VariableDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class TestDataProvider {

	private static final Integer VARIABLE_ID = 15;
	private static final String VARIABLE_NAME = "Variable_Name";
	private static final String VARIABLE_DESCRIPTION = "Variable Description";
	private static final String VARIABLE_ALIAS = "VA";
	private static final boolean VARIABLE_IS_FAVOURITE = false;
	private static final String PROGRAM_UUID = "abcd";
	private static final Integer VARIABLE_OBSERVATIONS = 0;
	private static final Integer VARIABLE_STUDIES = 0;
	private static final String VARIABLE_EXPECTED_MIN = "12";
	private static final String VARIABLE_EXPECTED_MAX = "16";

	private static final Integer METHOD_ID = 10;
	private static final String METHOD_NAME = "Method Name";
	private static final String METHOD_DESCRIPTION = "Method Description";

	private static final Integer PROPERTY_ID = 11;
	private static final String PROPERTY_NAME = "Property Name";
	private static final String PROPERTY_DESCRIPTION = "Property Description";
	private static final String CROP_ONTOLOGY_ID = "CO:1234567890";

	private static final Integer SCALE_ID = 12;
	private static final String SCALE_NAME = "Scale Name";
	private static final String SCALE_DESCRIPTION = "Scale Description";
	private static final Integer SCALE_VOCABULARY_ID = 1030;

	private static final String SCALE_MIN_VALUE = "10.01";
	private static final String SCALE_MAX_VALUE = "20.02";
	private static final List<TermSummary> SCALE_CATEGORICAL_VALUES = new ArrayList<>(Arrays.asList(new TermSummary(120, "1", "One"), new TermSummary(121, "2", "Two")));

	private static final String CLASS_NAME_1 = "Agronomic";
	private static final String CLASS_NAME_2 = "Biotic Stress";
	private static final String CLASS_NAME_3 = "Study condition";

	public static final List<Term> MW_TERM_LIST = new ArrayList<>(Arrays.asList(new Term(1, TestDataProvider.CLASS_NAME_1, ""), new Term(2, TestDataProvider.CLASS_NAME_2, ""), new Term(3, TestDataProvider.CLASS_NAME_3, "")));

	public static final org.ibp.api.domain.ontology.DataType NUMERICAL_DATA_TYPE = new org.ibp.api.domain.ontology.DataType(String.valueOf(DataType.NUMERIC_VARIABLE.getId()), DataType.NUMERIC_VARIABLE.getName(), false);
	public static final org.ibp.api.domain.ontology.DataType CATEGORICAL_DATA_TYPE = new org.ibp.api.domain.ontology.DataType(String.valueOf(DataType.CATEGORICAL_VARIABLE.getId()), DataType.CATEGORICAL_VARIABLE.getName(), false);

	public static final org.ibp.api.domain.ontology.VariableType TRAIT_VARIABLE = new org.ibp.api.domain.ontology.VariableType(VariableType.TRAIT.getId().toString(),
            VariableType.TRAIT.getName(), VariableType.TRAIT.getDescription());

    public static final org.ibp.api.domain.ontology.VariableType ANALYSIS_VARIABLE = new org.ibp.api.domain.ontology.VariableType(VariableType.ANALYSIS.getId().toString(),
            VariableType.ANALYSIS.getName(), VariableType.ANALYSIS.getDescription());

    public static final org.ibp.api.domain.ontology.VariableType STUDY_CONDITION_VARIABLE = new org.ibp.api.domain.ontology.VariableType(VariableType.ENVIRONMENT_CONDITION
		.getId().toString(),
            VariableType.ENVIRONMENT_CONDITION.getName(), VariableType.ENVIRONMENT_CONDITION.getDescription());

	public static Term getMethodTerm() {
		return new Term(TestDataProvider.METHOD_ID, TestDataProvider.METHOD_NAME, TestDataProvider.METHOD_DESCRIPTION, CvId.METHODS.getId(),null);
	}

	public static Term getPropertyTerm() {
		return new Term(TestDataProvider.PROPERTY_ID, TestDataProvider.PROPERTY_NAME, TestDataProvider.PROPERTY_DESCRIPTION, CvId.PROPERTIES.getId(), null);
	}

	public static Term getScaleTerm() {
		return new Term(TestDataProvider.SCALE_ID, TestDataProvider.SCALE_NAME, TestDataProvider.SCALE_DESCRIPTION, CvId.SCALES.getId(), null);
	}

	public static Term getVariableTerm() {
		return new Term(TestDataProvider.VARIABLE_ID, TestDataProvider.VARIABLE_NAME, TestDataProvider.VARIABLE_DESCRIPTION, CvId.VARIABLES.getId(), false);
	}

	public static Date getDateCreated() {
		final Calendar dateCreated = Calendar.getInstance();
		dateCreated.set(2015, Calendar.JANUARY, 1);
		return dateCreated.getTime();
	}

	public static Date getDateModified() {
		final Calendar dateLastModified = Calendar.getInstance();
		dateLastModified.set(2015, Calendar.JANUARY, 2);
		return dateLastModified.getTime();
	}

	public static Method getTestMethod() {
		final Method method = new Method();
		method.setId(TestDataProvider.METHOD_ID);
		method.setName(TestDataProvider.METHOD_NAME);
		method.setDefinition(TestDataProvider.METHOD_DESCRIPTION);
		method.setDateCreated(TestDataProvider.getDateCreated());
		method.setDateLastModified(TestDataProvider.getDateModified());
		return method;
	}

	public static List<Method> getTestMethodList(final Integer elements) {
		final List<Method> methods = new ArrayList<>();
		for (Integer count = 0; count < elements; count++) {
			final Integer methodId = 100 + count;
			final Method method = new Method();
			method.setId(methodId);
			method.setName(TestDataProvider.METHOD_NAME + methodId);
			method.setDefinition(TestDataProvider.METHOD_DESCRIPTION);
			method.setDateCreated(TestDataProvider.getDateCreated());
			method.setDateLastModified(TestDataProvider.getDateModified());
			methods.add(method);
		}
		return methods;
	}

	public static Property getTestProperty() {
		final Property property = new Property();
		property.setId(TestDataProvider.PROPERTY_ID);
		property.setName(TestDataProvider.PROPERTY_NAME);
		property.setDefinition(TestDataProvider.PROPERTY_DESCRIPTION);
		property.setCropOntologyId(TestDataProvider.CROP_ONTOLOGY_ID);
		property.addClass(TestDataProvider.CLASS_NAME_1);
		property.setDateCreated(TestDataProvider.getDateCreated());
		property.setDateLastModified(TestDataProvider.getDateModified());
		return property;
	}

	public static List<Property> getTestProperties(final Integer elements) {
		final List<Property> properties = new ArrayList<>();

		for (Integer count = 0; count < elements; count++) {
			final Property property = new Property();
			property.setId(TestDataProvider.PROPERTY_ID);
			property.setName(TestDataProvider.PROPERTY_NAME);
			property.setDefinition(TestDataProvider.PROPERTY_DESCRIPTION);
			property.setCropOntologyId(TestDataProvider.CROP_ONTOLOGY_ID);
			property.addClass(TestDataProvider.CLASS_NAME_1);
			property.setDateCreated(TestDataProvider.getDateCreated());
			property.setDateLastModified(TestDataProvider.getDateModified());
			properties.add(property);
		}
		return properties;
	}

	public static Scale getTestScale() {
		final Scale scale = new Scale();
		scale.setId(TestDataProvider.SCALE_ID);
		scale.setName(TestDataProvider.SCALE_NAME);
		scale.setVocabularyId(TestDataProvider.SCALE_VOCABULARY_ID);
		scale.setDefinition(TestDataProvider.SCALE_DESCRIPTION);
		scale.setDataType(DataType.NUMERIC_VARIABLE);
		scale.setMinValue(TestDataProvider.SCALE_MIN_VALUE);
		scale.setMaxValue(TestDataProvider.SCALE_MAX_VALUE);
		scale.addCategory(SCALE_CATEGORICAL_VALUES.get(0));
		scale.setDateCreated(TestDataProvider.getDateCreated());
		scale.setDateLastModified(TestDataProvider.getDateModified());
		return scale;
	}

	public static List<Scale> getTestScales(final Integer elements) {
		final List<Scale> scaleList = new ArrayList<>();

		for (Integer count = 0; count < elements; count++) {
			final Scale scale = new Scale();
			scale.setId(TestDataProvider.SCALE_ID);
			scale.setName(TestDataProvider.SCALE_NAME);
			scale.setDefinition(TestDataProvider.SCALE_DESCRIPTION);
			scale.setDataType(DataType.NUMERIC_VARIABLE);
			scale.setMinValue(TestDataProvider.SCALE_MIN_VALUE);
			scale.setMaxValue(TestDataProvider.SCALE_MAX_VALUE);
			scale.addCategory(SCALE_CATEGORICAL_VALUES.get(0));
			scale.setDateCreated(TestDataProvider.getDateCreated());
			scale.setDateLastModified(TestDataProvider.getDateModified());
			scaleList.add(scale);
		}
		return scaleList;
	}

	public static Variable getTestVariable() {
		final Variable variable = new Variable();
		variable.setId(TestDataProvider.VARIABLE_ID);
		variable.setName(TestDataProvider.VARIABLE_NAME);
		variable.setDefinition(TestDataProvider.VARIABLE_DESCRIPTION);
		variable.setObservations(TestDataProvider.VARIABLE_OBSERVATIONS);
		variable.setProperty(TestDataProvider.getTestProperty());
		variable.setMethod(TestDataProvider.getTestMethod());
		variable.setScale(TestDataProvider.getTestScale());
		variable.setMinValue(TestDataProvider.VARIABLE_EXPECTED_MIN);
		variable.setMaxValue(TestDataProvider.VARIABLE_EXPECTED_MAX);
		variable.setAlias(TestDataProvider.VARIABLE_ALIAS);
		variable.setIsFavorite(TestDataProvider.VARIABLE_IS_FAVOURITE);
		variable.setDateCreated(TestDataProvider.getDateCreated());
		variable.setStudies(TestDataProvider.VARIABLE_STUDIES);
		variable.addVariableType(VariableType.TRAIT);
		variable.setDateCreated(TestDataProvider.getDateCreated());
		variable.setDateLastModified(TestDataProvider.getDateModified());
		variable.setFormula(getTestFormula());
		variable.setAllowsFormula(true);
	  	variable.setHasUsage(false);
		variable.setIsSystem(false);
		return variable;
	}

	public static Variable getTestVariable(final VariableType variableType) {
		final Variable variable = new Variable();
		variable.setId(TestDataProvider.VARIABLE_ID);
		variable.setName(TestDataProvider.VARIABLE_NAME);
		variable.setDefinition(TestDataProvider.VARIABLE_DESCRIPTION);
		variable.setObservations(TestDataProvider.VARIABLE_OBSERVATIONS);
		variable.setProperty(TestDataProvider.getTestProperty());
		variable.setMethod(TestDataProvider.getTestMethod());
		variable.setScale(TestDataProvider.getTestScale());
		variable.setMinValue(TestDataProvider.VARIABLE_EXPECTED_MIN);
		variable.setMaxValue(TestDataProvider.VARIABLE_EXPECTED_MAX);
		variable.setAlias(TestDataProvider.VARIABLE_ALIAS);
		variable.setIsFavorite(TestDataProvider.VARIABLE_IS_FAVOURITE);
		variable.setDateCreated(TestDataProvider.getDateCreated());
		variable.setStudies(TestDataProvider.VARIABLE_STUDIES);
		variable.addVariableType(variableType);
		variable.setDateCreated(TestDataProvider.getDateCreated());
		variable.setDateLastModified(TestDataProvider.getDateModified());
		variable.setFormula(variableType.equals(VariableType.TRAIT)?getTestFormula(): null);
		variable.setAllowsFormula(true);
		variable.setHasUsage(false);

		return variable;
	}
	public static FormulaDto getTestFormula() {
		final FormulaDto formulaDto = new FormulaDto();
		final int inputId = RandomUtils.nextInt();
		formulaDto.setFormulaId(RandomUtils.nextInt());
		final FormulaVariable target = new FormulaVariable();
		target.setId(RandomUtils.nextInt());
		formulaDto.setTarget(target);
		formulaDto.setDefinition("{{" + inputId + "}}");

		final List<FormulaVariable> inputs = new ArrayList<>();
		final FormulaVariable input = new FormulaVariable();
		input.setTargetTermId(RandomUtils.nextInt());
		input.setName("input name");
		input.setId(inputId);
		inputs.add(input);
		formulaDto.setInputs(inputs);

		return formulaDto;
	}

	public static List<Variable> getTestVariables(final Integer elements) {
		final List<Variable> variableList = new ArrayList<>();

		for (Integer count = 0; count < elements; count++) {
			final Variable variable = new Variable(new Term(TestDataProvider.VARIABLE_ID + count, TestDataProvider.VARIABLE_NAME + count, TestDataProvider.VARIABLE_DESCRIPTION + count));
			variable.setMinValue(TestDataProvider.VARIABLE_EXPECTED_MIN);
			variable.setMaxValue(TestDataProvider.VARIABLE_EXPECTED_MAX);
			variable.setAlias(TestDataProvider.VARIABLE_ALIAS + "_" + String.valueOf(count));
			variable.setIsFavorite(TestDataProvider.VARIABLE_IS_FAVOURITE);
			variable.setDateCreated(TestDataProvider.getDateCreated());
			variable.setProperty(TestDataProvider.getTestProperty());
			variable.setMethod(TestDataProvider.getTestMethod());
			variable.setScale(TestDataProvider.getTestScale());
			variable.setDateCreated(TestDataProvider.getDateCreated());
			variable.setDateLastModified(TestDataProvider.getDateModified());
			variable.addVariableType(VariableType.ENVIRONMENT_DETAIL);
			variableList.add(variable);
		}
		return variableList;
	}

	public static MetadataDetails getTestMetadataDetails() {
		final MetadataDetails metadataDetails = new MetadataDetails();
		metadataDetails.setDateCreated(TestDataProvider.getDateCreated());
		metadataDetails.setDateLastModified(TestDataProvider.getDateModified());
		return metadataDetails;
	}

	public static MethodDetails getTestMethodDetails() {
		final MethodDetails method = new MethodDetails();
		method.setId(String.valueOf(TestDataProvider.METHOD_ID));
		method.setName(TestDataProvider.METHOD_NAME);
		method.setDescription(TestDataProvider.METHOD_DESCRIPTION);
		method.setMetadata(TestDataProvider.getTestMetadataDetails());
		return method;
	}

	public static PropertyDetails getTestPropertyDetails() {
		final PropertyDetails propertyDetails = new PropertyDetails();
		propertyDetails.setId(String.valueOf(TestDataProvider.PROPERTY_ID));
		propertyDetails.setName(TestDataProvider.PROPERTY_NAME);
		propertyDetails.setDescription(TestDataProvider.PROPERTY_DESCRIPTION);
		propertyDetails.setClasses(new HashSet<>(Collections.singletonList(TestDataProvider.CLASS_NAME_1)));
		propertyDetails.setCropOntologyId(TestDataProvider.CROP_ONTOLOGY_ID);
		propertyDetails.setMetadata(TestDataProvider.getTestMetadataDetails());
		return propertyDetails;
	}

	public static ScaleDetails getTestScaleDetails() {
		final ScaleDetails scaleDetails = new ScaleDetails();
		scaleDetails.setId(String.valueOf(TestDataProvider.SCALE_ID));
		scaleDetails.setName(TestDataProvider.SCALE_NAME);
		scaleDetails.setDescription(TestDataProvider.SCALE_DESCRIPTION);
		scaleDetails.setDataType(TestDataProvider.NUMERICAL_DATA_TYPE);
		scaleDetails.setMaxValue(TestDataProvider.SCALE_MIN_VALUE);
		scaleDetails.setMaxValue(TestDataProvider.SCALE_MAX_VALUE);
		scaleDetails.setMetadata(TestDataProvider.getTestMetadataDetails());
		return scaleDetails;
	}

	public static VariableDetails getTestVariableDetails() {
		final VariableDetails variableDetails = new VariableDetails();
		variableDetails.setProgramUuid(TestDataProvider.PROGRAM_UUID);
		variableDetails.setId(String.valueOf(TestDataProvider.VARIABLE_ID));
		variableDetails.setName(TestDataProvider.VARIABLE_NAME);
		variableDetails.setDescription(TestDataProvider.VARIABLE_DESCRIPTION);
		variableDetails.setAlias(TestDataProvider.VARIABLE_ALIAS);
		variableDetails.setVariableTypes(new HashSet<>(Collections.singletonList(TRAIT_VARIABLE)));
		variableDetails.setProperty(TestDataProvider.getTestPropertyDetails());
		variableDetails.setMethod(TestDataProvider.getTestMethodDetails());
		variableDetails.setScale(TestDataProvider.getTestScaleDetails());
		variableDetails.setExpectedMin(TestDataProvider.VARIABLE_EXPECTED_MIN);
		variableDetails.setExpectedMax(TestDataProvider.VARIABLE_EXPECTED_MAX);
		variableDetails.setFavourite(TestDataProvider.VARIABLE_IS_FAVOURITE);
		return variableDetails;
	}

	public static VariableDetails getTestVariableDetails(final org.ibp.api.domain.ontology.VariableType variableType) {
		final VariableDetails variableDetails = new VariableDetails();
		variableDetails.setProgramUuid(TestDataProvider.PROGRAM_UUID);
		variableDetails.setId(String.valueOf(TestDataProvider.VARIABLE_ID));
		variableDetails.setName(TestDataProvider.VARIABLE_NAME);
		variableDetails.setDescription(TestDataProvider.VARIABLE_DESCRIPTION);
		variableDetails.setAlias(TestDataProvider.VARIABLE_ALIAS);
		variableDetails.setVariableTypes(new HashSet<>(Collections.singletonList(variableType)));
		variableDetails.setProperty(TestDataProvider.getTestPropertyDetails());
		variableDetails.setMethod(TestDataProvider.getTestMethodDetails());
		variableDetails.setScale(TestDataProvider.getTestScaleDetails());
		variableDetails.setExpectedMin(TestDataProvider.VARIABLE_EXPECTED_MIN);
		variableDetails.setExpectedMax(TestDataProvider.VARIABLE_EXPECTED_MAX);
		variableDetails.setFavourite(TestDataProvider.VARIABLE_IS_FAVOURITE);
		return variableDetails;
	}

	public static VariableFilter getVariableFilterForVariableValidator(){
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addMethodId(METHOD_ID);
		variableFilter.addPropertyId(PROPERTY_ID);
		variableFilter.addScaleId(SCALE_ID);
		return variableFilter;
	}

	public static List<org.ibp.api.domain.ontology.VariableType> getVariableTypes(){
		final List<org.ibp.api.domain.ontology.VariableType> variableTypes = new ArrayList<>();

		org.ibp.api.domain.ontology.VariableType variableType = new org.ibp.api.domain.ontology.VariableType("1", "Variable Type 1", "Variable Type Description 1");
		variableTypes.add(variableType);

		variableType = new org.ibp.api.domain.ontology.VariableType("2", "Variable Type 2", "Variable Type Description 2");
		variableTypes.add(variableType);
		return variableTypes;
	}
}

