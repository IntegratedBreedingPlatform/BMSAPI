package org.ibp.api.java.impl.middleware.ontology;

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
import org.ibp.api.domain.ontology.*;
import org.ibp.api.java.impl.middleware.common.CommonUtil;

import java.util.*;

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
    private static final String scaleDescription = "cale Description";
    private static final Integer scaleVocabularyId = 1030;

    private static final String scaleMinValue = "10";
    private static final String scaleMaxValue = "20";
    private static final String category1 = "Scale Category1";
    private static final String category2 = "Scale Category2";

    private static final String className1 = "Agronomic";
    private static final String className2 = "Biotic Stress";
    private static final String className3 = "Study condition";

    public static final List<Term> mwTermList = new ArrayList<>(Arrays.asList(new Term(1, className1, ""), new Term(2, className2, ""), new Term(3, className3, "")));

    public static final TermSummary mwMethodSummary = new TermSummary(methodId, methodName, methodDescription);
    public static final TermSummary mwPropertySummary = new TermSummary(propertyId, propertyName, propertyDescription);
    public static final TermSummary mwScaleSummary = new TermSummary(scaleId, scaleName, scaleDescription);

    public static final org.ibp.api.domain.ontology.DataType numericalDataType = new org.ibp.api.domain.ontology.DataType(DataType.NUMERIC_VARIABLE.getId(), DataType.NUMERIC_VARIABLE.getName());
    public static final org.ibp.api.domain.ontology.DataType categoricalDataType = new org.ibp.api.domain.ontology.DataType(DataType.CATEGORICAL_VARIABLE.getId(), DataType.CATEGORICAL_VARIABLE.getName());

    public static final org.ibp.api.domain.ontology.VariableType traitVariable = new org.ibp.api.domain.ontology.VariableType(1, "Trait Variable", "Variable for trait study");

    public static Term getMethodTerm(){
        return new Term(methodId, methodName, methodDescription, CvId.METHODS.getId(), null);
    }

    public static Term getPropertyTerm(){
        return new Term(propertyId, propertyName, propertyDescription, CvId.PROPERTIES.getId(), null);
    }

    public static Term getScaleTerm(){
        return new Term(scaleId, scaleName, scaleDescription, CvId.SCALES.getId(), null);
    }

    public static Term getVariableTerm(){
        return new Term(variableId, variableName, variableDescription, CvId.VARIABLES.getId(), false);
    }

    public static Date getDateCreated(){
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
        method.setId(methodId);
        method.setName(methodName);
        method.setDefinition(methodDescription);
        method.setDateCreated(getDateCreated());
        method.setDateLastModified(getDateModified());
        return method;
    }

    public static List<Method> getTestMethodList(Integer elements){
        List<Method> methods = new ArrayList<>();
        for(Integer count = 0; count < elements; count ++){
            Integer methodId = 100 + count;
            Method method = new Method();
            method.setId(methodId);
            method.setName(methodName + methodId);
            method.setDefinition(methodDescription);
            method.setDateCreated(getDateCreated());
            method.setDateLastModified(getDateModified());
            methods.add(method);
        }
        return methods;
    }

    public static Property getTestProperty(){
        Property property = new Property();
        property.setId(propertyId);
        property.setName(propertyName);
        property.setDefinition(propertyDescription);
        property.setCropOntologyId(cropOntologyId);
        property.addClass(className1);
        property.setDateCreated(getDateCreated());
        property.setDateLastModified(getDateModified());
        return property;
    }

    public static List<Property> getTestProperties(Integer elements){
        List<Property> properties = new ArrayList<>();

        for(Integer count = 0; count < elements; count++){
            Property property = new Property();
            property.setId(propertyId);
            property.setName(propertyName);
            property.setDefinition(propertyDescription);
            property.setCropOntologyId(cropOntologyId);
            property.addClass(className1);
            property.setDateCreated(getDateCreated());
            property.setDateLastModified(getDateModified());
            properties.add(property);
        }
        return properties;
    }

    public static Scale getTestScale(){
        Scale scale = new Scale();
        scale.setId(scaleId);
        scale.setName(scaleName);
        scale.setVocabularyId(scaleVocabularyId);
        scale.setDefinition(scaleDescription);
        scale.setDataType(DataType.NUMERIC_VARIABLE);
        scale.setMinValue(scaleMinValue);
        scale.setMaxValue(scaleMaxValue);
        scale.addCategory(category1, category2);
        scale.setDateCreated(getDateCreated());
        scale.setDateLastModified(getDateModified());
        return scale;
    }

    public static List<Scale> getTestScales(Integer elements){
        List<Scale> scaleList = new ArrayList<>();

        for(Integer count = 0; count < elements; count++){
            Scale scale = new Scale();
            scale.setId(scaleId);
            scale.setName(scaleName);
            scale.setDefinition(scaleDescription);
            scale.setMinValue(scaleMinValue);
            scale.setMaxValue(scaleMaxValue);
            scale.addCategory(category1, category2);
            scale.setDateCreated(getDateCreated());
            scale.setDateLastModified(getDateModified());
            scaleList.add(scale);
        }
        return scaleList;
    }

    public static Variable getTestVariable(){
        Variable variable = new Variable();
        variable.setId(variableId);
        variable.setName(variableName);
        variable.setDefinition(variableDescription);
        variable.setObservations(variableObservations);
        variable.setProperty(getTestProperty());
        variable.setMethod(getTestMethod());
        variable.setScale(getTestScale());
        variable.setMinValue(variableExpectedMin);
        variable.setMaxValue(variableExpectedMax);
        variable.setAlias(variableAlias);
        variable.setIsFavorite(variableIsFavourite);
        variable.setDateCreated(getDateCreated());
        variable.setStudies(variableStudies);
        variable.addVariableType(VariableType.getById(1));
        variable.setDateCreated(getDateCreated());
        variable.setDateLastModified(getDateModified());

        return variable;
    }

    public static List<OntologyVariableSummary> getTestVariables(Integer elements){
        List<OntologyVariableSummary> variableList = new ArrayList<>();

        for(Integer count = 0; count < elements; count++){
            OntologyVariableSummary variable = new OntologyVariableSummary(variableId + count, variableName + count, variableDescription + count);
            variable.setMinValue(variableExpectedMin);
            variable.setMaxValue(variableExpectedMax);
            variable.setAlias(variableAlias);
            variable.setIsFavorite(variableIsFavourite);
            variable.setDateCreated(getDateCreated());
            variable.setPropertySummary(mwPropertySummary);
            variable.setMethodSummary(mwMethodSummary);
            variable.setScaleSummary(mwScaleSummary);
            variable.setDateCreated(getDateCreated());
            variable.setDateLastModified(getDateModified());
            variable.setDataType(DataType.NUMERIC_VARIABLE);
            variableList.add(variable);
        }
        return variableList;
    }

    public static MetadataSummary getTestMetadataSummary() {
        MetadataSummary metadataSummary = new MetadataSummary();
        metadataSummary.setDateCreated(getDateCreated());
        metadataSummary.setDateLastModified(getDateModified());
        return metadataSummary;
    }

    public static MethodSummary getTestMethodSummary(){
        MethodSummary methodSummary = new MethodSummary();
        methodSummary.setId(String.valueOf(methodId));
        methodSummary.setName(methodName);
        methodSummary.setDescription(methodDescription);
        methodSummary.setMetadata(getTestMetadataSummary());
        return methodSummary;
    }

    public static PropertySummary getTestPropertySummary(){
        PropertySummary propertySummary = new PropertySummary();
        propertySummary.setId(String.valueOf(propertyId));
        propertySummary.setName(propertyName);
        propertySummary.setDescription(propertyDescription);
        propertySummary.setClasses(new HashSet<>(Collections.singletonList(className1)));
        propertySummary.setCropOntologyId(cropOntologyId);
        propertySummary.setMetadata(getTestMetadataSummary());
        return propertySummary;
    }

    public static ScaleSummary getTestScaleSummary() {
        ScaleSummary scaleSummary = new ScaleSummary();
        scaleSummary.setId(String.valueOf(scaleId));
        scaleSummary.setName(scaleName);
        scaleSummary.setDescription(scaleDescription);
        scaleSummary.setDataType(numericalDataType);
        scaleSummary.setMinValue(CommonUtil.tryParseSafe(scaleMinValue));
        scaleSummary.setMaxValue(CommonUtil.tryParseSafe(scaleMaxValue));
        scaleSummary.setMetadata(getTestMetadataSummary());
        return scaleSummary;
    }

    public static VariableSummary getTestVariableSummary(){
        VariableSummary variableSummary = new VariableSummary();
        variableSummary.setProgramUuid(programUUID);
        variableSummary.setId(String.valueOf(variableId));
        variableSummary.setName(variableName);
        variableSummary.setDescription(variableDescription);
        variableSummary.setAlias(variableAlias);
        variableSummary.setVariableTypes(new HashSet<>(Collections.singletonList(traitVariable)));
        variableSummary.setPropertySummary(new org.ibp.api.domain.ontology.TermSummary());
        variableSummary.getPropertySummary().setId(String.valueOf(propertyId));
        variableSummary.getPropertySummary().setName(propertyName);
        variableSummary.getPropertySummary().setDescription(propertyDescription);
        variableSummary.setMethodSummary(new org.ibp.api.domain.ontology.TermSummary());
        variableSummary.getMethodSummary().setId(String.valueOf(methodId));
        variableSummary.getMethodSummary().setName(methodName);
        variableSummary.getMethodSummary().setDescription(methodDescription);
        variableSummary.setScaleSummary(new org.ibp.api.domain.ontology.TermSummary());
        variableSummary.getScaleSummary().setId(String.valueOf(scaleId));
        variableSummary.getScaleSummary().setName(scaleName);
        variableSummary.getScaleSummary().setDescription(scaleDescription);
        variableSummary.setDataType(numericalDataType);
        variableSummary.setExpectedMin(variableExpectedMin);
        variableSummary.setExpectedMax(variableExpectedMax);
        variableSummary.setFavourite(variableIsFavourite);
        return variableSummary;
    }
}
