package org.generationcp.bms.ontology.services.impl;

import org.generationcp.bms.ontology.dto.*;
import org.generationcp.bms.ontology.services.OntologyMapper;
import org.generationcp.bms.ontology.dto.MethodRequest;
import org.generationcp.bms.ontology.services.OntologyModelService;
import org.generationcp.bms.ontology.dto.PropertyRequest;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

@Service
public class OntologyModelServiceImpl implements OntologyModelService {

    @Autowired
    private OntologyService ontologyService;

    @Override
    public List<MethodSummary> getAllMethods() throws MiddlewareQueryException {

        List<Method> methodList = ontologyService.getAllMethods();
        if(methodList == null){
            return Collections.emptyList();
        }
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
        Method method = ontologyService.getMethod(id);
        if(method == null){
            return null;
        }
        boolean deletable = true;
        if(ontologyService.isTermReferred(id)){
            deletable = false;
        }
        ModelMapper mapper = OntologyMapper.methodMapper();
        MethodResponse response = mapper.map(method, MethodResponse.class);
        if(deletable){
            response.setEditableFields(new ArrayList<>(Arrays.asList("description")));
        }
        response.setDeletable(deletable);
        return response;
    }

    @Override
    public GenericResponse addMethod(MethodRequest request) throws MiddlewareQueryException {
        Method method = ontologyService.addMethod(request.getName(), request.getDescription());
        return new GenericResponse(method.getId());
    }

    @Override
    public boolean updateMethod(Integer id, MethodRequest request) throws MiddlewareQueryException, MiddlewareException {
        if(ontologyService.isTermReferred(id)){
            return false;
        }
        Method method = new Method(new Term(id, request.getName(), request.getDescription()));
        ontologyService.updateMethod(method);
        return true;
    }

    @Override
    public boolean deleteMethod(Integer id) throws MiddlewareQueryException {
        boolean isReferred = ontologyService.isTermReferred(id);
        if(isReferred){
            return false;
        }
        ontologyService.deleteMethod(id);
        return true;
    }

    @Override
    public List<PropertySummary> getAllProperties() throws MiddlewareQueryException {
        List<Property> propertyList = ontologyService.getAllPropertiesWithClassAndCropOntology();
        List<PropertySummary> properties = new ArrayList<>();

        ModelMapper mapper = OntologyMapper.propertyMapper();

        for (Property property : propertyList){
            PropertySummary propertyDTO = mapper.map(property, PropertySummary.class);
            properties.add(propertyDTO);
        }
        return properties;
    }

    @Override
    public PropertyResponse getProperty(Integer id) throws MiddlewareQueryException {
        Property property = ontologyService.getPropertyById(id);
        if (property == null) {
        	return null;
        }
        boolean deletable = true;
        if(ontologyService.isTermReferred(id)) {
        	deletable = false;
        }
        ModelMapper mapper = OntologyMapper.propertyMapper();
        PropertyResponse response = mapper.map(property, PropertyResponse.class);
        if(deletable) {
        	response.setEditableFields(new ArrayList<>(Arrays.asList("description")));
        }
        response.setDeletable(deletable);
        return response;
    }

    @Override
    public GenericResponse addProperty(PropertyRequest request) throws MiddlewareQueryException, MiddlewareException {
        return new GenericResponse(ontologyService.addProperty(request.getName(), request.getDescription(), request.getCropOntologyId(), request.getClasses()).getId());
    }

    @Override
    public List<PropertySummary> getAllPropertiesByClass(String propertyClass) throws MiddlewareQueryException {
        List<Property> propertyList = ontologyService.getAllPropertiesWithClass(propertyClass);
        if(propertyList.isEmpty()) {
        	return Collections.emptyList();
        }
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
        boolean isReferred = ontologyService.isTermReferred(id);
        if(isReferred) {
        	return false;
        }
        ontologyService.deleteProperty(id);
        return true;
    }

    @Override
    public boolean updateProperty(Integer id, PropertyRequest request) throws MiddlewareQueryException, MiddlewareException {
        if(ontologyService.isTermReferred(id)) {
        	return false;
        }
        ontologyService.updateProperty(id, request.getName(), request.getDescription(), request.getCropOntologyId(), request.getClasses());
        return true;
    }

    @Override
    public List<DataTypeSummary> getAllDataTypes() throws MiddlewareQueryException {
        List<Term> termList = ontologyService.getAllDataTypes();
        List<DataTypeSummary> dataTypeSummaries = new ArrayList<>();

        ModelMapper mapper = OntologyMapper.getInstance();

        for(Term term : termList){
            DataTypeSummary summary = mapper.map(term, DataTypeSummary.class);
            dataTypeSummaries.add(summary);
        }
        return dataTypeSummaries;
    }

    @Override
    public List<String> getAllClasses() throws MiddlewareQueryException {
        List<Term> classes = ontologyService.getAllTraitClass();
        List<String> classList = new ArrayList<>();

        for (Term term : classes){
            classList.add(term.getName());
        }
        return classList;
    }
}
