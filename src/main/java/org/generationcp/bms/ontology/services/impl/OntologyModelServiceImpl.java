package org.generationcp.bms.ontology.services.impl;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import org.generationcp.bms.ontology.dto.*;
import org.generationcp.bms.ontology.services.OntologyMapper;
import org.generationcp.bms.ontology.services.OntologyModelService;
import org.generationcp.middleware.domain.oms.*;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.generationcp.middleware.util.Util;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.generationcp.middleware.domain.oms.DataType.CATEGORICAL_VARIABLE;
import static org.generationcp.middleware.domain.oms.DataType.NUMERIC_VARIABLE;

@Service
public class OntologyModelServiceImpl implements OntologyModelService {

    @Autowired
    private OntologyManagerService ontologyManagerService;

    private String FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED = "description";

    @Override
    public List<MethodSummary> getAllMethods() throws MiddlewareQueryException {
        List<Method> methodList = ontologyManagerService.getAllMethods();
        List<MethodSummary> methods = new ArrayList<>();

        ModelMapper mapper = OntologyMapper.methodMapper();

        for (Method method : methodList){
            MethodSummary methodSummary = mapper.map(method, MethodSummary.class);
            methods.add(methodSummary);
        }
        return methods;
    }

    @Override
    public MethodResponse getMethod(Integer id) throws MiddlewareQueryException {
        Method method = ontologyManagerService.getMethod(id);
        if(method == null){
            return null;
        }
        boolean deletable = true;
        if(ontologyManagerService.isTermReferred(id)){
            deletable = false;
        }
        ModelMapper mapper = OntologyMapper.methodMapper();
        MethodResponse response = mapper.map(method, MethodResponse.class);
        if(!deletable){
            response.setEditableFields(new ArrayList<>(Collections.singletonList(FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED)));
        }else {
            response.setEditableFields(new ArrayList<>(Arrays.asList("name", "description")));
        }
        response.setDeletable(deletable);
        return response;
    }

    @Override
    public GenericResponse addMethod(MethodRequest request) throws MiddlewareQueryException {
        Method method = new Method();
        method.setName(request.getName());
        method.setDefinition(request.getDescription());
        ontologyManagerService.addMethod(method);
        return new GenericResponse(method.getId());
    }

    @Override
    public void updateMethod(Integer id, MethodRequest request) throws MiddlewareQueryException, MiddlewareException {
        Method method = new Method();
        method.setId(request.getId());
        method.setName(request.getName());
        method.setDefinition(request.getDescription());
        ontologyManagerService.updateMethod(method);
    }

    @Override
    public void deleteMethod(Integer id) throws MiddlewareQueryException {
        ontologyManagerService.deleteMethod(id);
    }

    @Override
    public List<PropertySummary> getAllProperties() throws MiddlewareQueryException {
        List<Property> propertyList = ontologyManagerService.getAllProperties();
        List<PropertySummary> properties = new ArrayList<>();

        ModelMapper mapper = OntologyMapper.propertyMapper();

        for (Property property : propertyList){
            PropertySummary propertyDTO = mapper.map(property, PropertySummary.class);
            properties.add(propertyDTO);
        }
        return properties;
    }

    @Override
    public PropertyResponse getProperty(Integer id) throws MiddlewareQueryException, MiddlewareException {
        Property property = ontologyManagerService.getProperty(id);
        if (property == null) {
            return null;
        }
        boolean deletable = true;
        if(ontologyManagerService.isTermReferred(id)) {
            deletable = false;
        }
        ModelMapper mapper = OntologyMapper.propertyMapper();
        PropertyResponse response = mapper.map(property, PropertyResponse.class);
        if(!deletable) {
            response.setEditableFields(new ArrayList<>(Arrays.asList("description", "classes", "cropOntologyId")));
        }else {
            response.setEditableFields(new ArrayList<>(Arrays.asList("name", "description", "classes", "cropOntologyId")));
        }
        response.setDeletable(deletable);
        return response;
    }

    @Override
    public GenericResponse addProperty(PropertyRequest request) throws MiddlewareQueryException, MiddlewareException {
        Property property = new Property();
        property.setName(request.getName());
        property.setDefinition(request.getDescription());
        property.setCropOntologyId(request.getCropOntologyId());

        List<Term> traitClasses = ontologyManagerService.getAllTraitClass();

        for(String c : request.getClasses()){
            for(Term tc : traitClasses ) {
                if(tc.getName().equals(c)) {
                    property.addClass(tc);
                }
            }
        }

        ontologyManagerService.addProperty(property);

        return new GenericResponse(property.getId());
    }

    @Override
    public List<PropertySummary> getAllPropertiesByClass(String propertyClass) throws MiddlewareQueryException {
        List<Property> propertyList = ontologyManagerService.getAllPropertiesWithClass(propertyClass);
        List<PropertySummary> properties = new ArrayList<>();

        ModelMapper mapper = OntologyMapper.propertyMapper();

        for (Property property : propertyList){
            PropertySummary propertyDTO = mapper.map(property, PropertySummary.class);
            properties.add(propertyDTO);
        }
        return properties;
    }

    @Override
    public boolean deleteProperty(Integer id) throws MiddlewareQueryException, MiddlewareException {
        boolean isReferred = ontologyManagerService.isTermReferred(id);
        if(isReferred) {
            return false;
        }
        ontologyManagerService.deleteProperty(id);
        return true;
    }

    @Override
    public void updateProperty(Integer id, PropertyRequest request) throws MiddlewareQueryException, MiddlewareException {
        Property property = new Property();
        property.setId(id);
        property.setName(request.getName());
        property.setDefinition(request.getDescription());
        property.setCropOntologyId(request.getCropOntologyId());

        List<Term> traitClasses = ontologyManagerService.getAllTraitClass();

        for(String c : request.getClasses()){
            for(Term tc : traitClasses ) {
                if(tc.getName().equals(c)) {
                    property.addClass(tc);
                }
            }
        }

        ontologyManagerService.updateProperty(property);
    }

    @Override
    public List<IdName> getAllDataTypes() throws MiddlewareQueryException {
        return Util.convertAll(Arrays.asList(DataType.values()), new Function<DataType, IdName>(){
            @Override
            public IdName apply(DataType dataType) {
                return new IdName(dataType.getId(), dataType.getName());
            }
        });
    }

    @Override
    public List<String> getAllClasses() throws MiddlewareQueryException {
        List<Term> classes = ontologyManagerService.getAllTraitClass();
        List<String> classList = new ArrayList<>();

        for (Term term : classes){
            classList.add(term.getName());
        }
        return classList;
    }

    @Override
    public List<ScaleSummary> getAllScales() throws MiddlewareQueryException {
        List<Scale> scales = ontologyManagerService.getAllScales();
        List<ScaleSummary> scaleSummaries = new ArrayList<>();

        ModelMapper mapper = OntologyMapper.scaleMapper();

        for (Scale scale: scales){
            ScaleSummary scaleSummary = mapper.map(scale, ScaleSummary.class);
            scaleSummaries.add(scaleSummary);
        }
        return scaleSummaries;
    }

    @Override
    public ScaleResponse getScaleById(Integer id) throws MiddlewareQueryException {
        Scale scale = ontologyManagerService.getScaleById(id);
        if(scale == null){
            return null;
        }
        boolean deletable = true;
        if(ontologyManagerService.isTermReferred(id)){
            deletable = false;
        }
        ModelMapper mapper = OntologyMapper.scaleMapper();
        ScaleResponse response = mapper.map(scale, ScaleResponse.class);
        if(!deletable){
            response.setEditableFields(new ArrayList<>(Collections.singletonList(FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED)));
        }else {
            response.setEditableFields(new ArrayList<>(Arrays.asList("name", "description", "validValues")));
        }
        response.setDeletable(deletable);
        return response;
    }

    @Override
    public GenericResponse addScale(ScaleRequest request) throws MiddlewareQueryException, MiddlewareException {
        Scale scale = new Scale();
        scale.setName(request.getName());
        scale.setDefinition(request.getDescription());

        scale.setDataType(DataType.getById(request.getDataTypeId()));

        if(Objects.equals(request.getDataTypeId(), CATEGORICAL_VARIABLE.getId())){
            for(NameDescription description : request.getValidValues().getCategories()){
                scale.addCategory(description.getName(), description.getDescription());
            }
        }
        if(Objects.equals(request.getDataTypeId(), NUMERIC_VARIABLE.getId())){
            scale.setMinValue(request.getValidValues().getMin());
            scale.setMaxValue(request.getValidValues().getMax());
        }

        ontologyManagerService.addScale(scale);
        return new GenericResponse(scale.getId());
    }

    @Override
    public void updateScale(ScaleRequest request) throws MiddlewareQueryException, MiddlewareException {
        Scale scale = new Scale(new Term(request.getId(), request.getName(), request.getDescription()));

        scale.setDataType(DataType.getById(request.getDataTypeId()));

        ValidValues validValues = Objects.equals(request.getValidValues(), null) ? new ValidValues() : request.getValidValues();

        if(Objects.equals(request.getDataTypeId(), CATEGORICAL_VARIABLE.getId())){
            for(NameDescription description : validValues.getCategories()){
                scale.addCategory(description.getName(), description.getDescription());
            }
        }
        if(Objects.equals(request.getDataTypeId(), NUMERIC_VARIABLE.getId())){
            scale.setMinValue(validValues.getMin());
            scale.setMaxValue(validValues.getMax());
        }

        ontologyManagerService.updateScale(scale);
    }

    @Override
    public void deleteScale(Integer id) throws MiddlewareQueryException, MiddlewareException {
        ontologyManagerService.deleteScale(id);
    }

    @Override
    public List<VariableTypeResponse> getAllVariableTypes() {

        return Util.convertAll(Arrays.asList(VariableType.values()), new Function<VariableType, VariableTypeResponse>(){
            @Override
            public VariableTypeResponse apply(VariableType variableType) {
                return new VariableTypeResponse(variableType.getId(), variableType.getName(), variableType.getDescription());
            }
        });
    }

    @Override
    public List<VariableSummary> getAllVariablesByFilter(Integer programId, Integer propertyId, Boolean favourite) throws MiddlewareQueryException {
        List<OntologyVariableSummary> variableSummaries = ontologyManagerService.getWithFilter(programId, favourite, null, propertyId, null);
        List<VariableSummary> variableSummaryList = new ArrayList<>();

        ModelMapper mapper = OntologyMapper.variableMapper();

        for (OntologyVariableSummary variable : variableSummaries){
            VariableSummary variableSummary = mapper.map(variable, VariableSummary.class);
            variableSummaryList.add(variableSummary);
        }
        return variableSummaryList;
    }

    @Override
    public VariableResponse getVariableById(Integer programId, Integer variableId) throws MiddlewareQueryException, MiddlewareException {
        OntologyVariable ontologyVariable = ontologyManagerService.getVariable(programId, variableId);
        if(ontologyVariable == null){
            return null;
        }
        boolean deletable = true;
        if(ontologyManagerService.isTermReferred(variableId)){
            deletable = false;
        }
        ModelMapper mapper = OntologyMapper.variableResponseMapper();
        VariableResponse response = mapper.map(ontologyVariable, VariableResponse.class);
        if(!deletable){
            response.setEditableFields(new ArrayList<>(Collections.singletonList(FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED)));
        }else {
            response.setEditableFields(new ArrayList<>(Arrays.asList("name", "description", "alias", "cropOntologyId", "variableTypeIds", "propertySummary", "methodSummary", "scale", "expectedRange")));
        }
        response.setDeletable(deletable);
        return response;
    }

    @Override
    public GenericResponse addVariable(VariableRequest request) throws MiddlewareQueryException, MiddlewareException {
        OntologyVariableInfo variableInfo = new OntologyVariableInfo();
        variableInfo.setName(request.getName());
        variableInfo.setDescription(request.getDescription());
        variableInfo.setMethodId(request.getMethodId());
        variableInfo.setPropertyId(request.getPropertyId());
        variableInfo.setScaleId(request.getScaleId());

        if(!Strings.isNullOrEmpty(request.getExpectedRange().getMin()) && !Strings.isNullOrEmpty(request.getExpectedRange().getMax())){
            variableInfo.setMinValue(request.getExpectedRange().getMin());
            variableInfo.setMaxValue(request.getExpectedRange().getMax());
        }

        for(Integer i : request.getVariableTypeIds()){
            variableInfo.addVariableType(VariableType.getById(i));
        }
        ontologyManagerService.addVariable(variableInfo);
        return new GenericResponse(variableInfo.getId());
    }
}
