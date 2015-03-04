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

import java.util.List;
import java.util.ArrayList;

@Service
public class OntologyModelServiceImpl implements OntologyModelService {

    @Autowired
    private OntologyService ontologyService;

    @Override
    public List<MethodSummary> getAllMethods() throws MiddlewareQueryException {

        List<Method> methodList = ontologyService.getAllMethods();
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
        if(method == null) return null;
        ModelMapper mapper = OntologyMapper.methodMapper();
        return mapper.map(method, MethodResponse.class);
    }

    @Override
    public GenericResponse addMethod(MethodRequest request) throws MiddlewareQueryException {
        Method method = ontologyService.addMethod(request.getName(), request.getDescription());
        return new GenericResponse(method.getId());
    }

    @Override
    public void updateMethod(Integer id, MethodRequest request) throws MiddlewareQueryException, MiddlewareException {
        Method method = new Method(new Term(id, request.getName(), request.getDescription()));
        ontologyService.updateMethod(method);
    }

    @Override
    public void deleteMethod(Integer id) throws MiddlewareQueryException {
        ontologyService.deleteMethod(id);
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
        if (property == null) return null;
        ModelMapper mapper = OntologyMapper.propertyMapper();
        return mapper.map(property, PropertyResponse.class);
    }

    @Override
    public GenericResponse addProperty(PropertyRequest request) throws MiddlewareQueryException {
        return new GenericResponse(ontologyService.addProperty(request.getName(), request.getDescription(), request.getCropOntologyId(), request.getClasses()).getId());
    }

    @Override
    public List<PropertySummary> getAllPropertiesByClass(String propertyClass) throws MiddlewareQueryException {
        List<Property> propertyList = ontologyService.getAllPropertiesWithClass(propertyClass);
        if(propertyList.isEmpty()) return null;
        List<PropertySummary> properties = new ArrayList<>();

        ModelMapper mapper = OntologyMapper.propertyMapper();

        for (Property property : propertyList){
            PropertySummary propertyDTO = mapper.map(property, PropertySummary.class);
            properties.add(propertyDTO);
        }
        return properties;
    }

    @Override
    public List<PropertySummary> getAllPropertiesByFilter(String filter) throws MiddlewareQueryException {
        List<Property> propertyList = ontologyService.searchProperties(filter);
        if(propertyList.isEmpty()) return null;
        List<PropertySummary> properties = new ArrayList<>();

        ModelMapper mapper = OntologyMapper.propertyMapper();

        for (Property property : propertyList){
            PropertySummary propertyDTO = mapper.map(property, PropertySummary.class);
            properties.add(propertyDTO);
        }
        return properties;
    }

    @Override
    public List<PropertySummary> getAllPropertiesByClasses(List<String> classes) throws MiddlewareQueryException {
        List<Property> propertyList = ontologyService.getAllPropertiesWithClasses(classes);
        if(propertyList.isEmpty()) return null;
        List<PropertySummary> properties = new ArrayList<>();

        ModelMapper mapper = OntologyMapper.propertyMapper();

        for (Property property : propertyList){
            PropertySummary propertyDTO = mapper.map(property, PropertySummary.class);
            properties.add(propertyDTO);
        }
        return properties;
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
