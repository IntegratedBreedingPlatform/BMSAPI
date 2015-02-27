package org.generationcp.bms.ontology.services.impl;

import org.generationcp.bms.ontology.dto.outgoing.*;
import org.generationcp.bms.ontology.services.OntologyMapper;
import org.generationcp.bms.ontology.dto.incoming.AddMethodRequest;
import org.generationcp.bms.ontology.services.IOntologyModelService;
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
public class OntologyModelService implements IOntologyModelService {

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
    public GenericAddResponse addMethod(AddMethodRequest request) throws MiddlewareQueryException {
        Method method = ontologyService.addMethod(request.getName(), request.getDescription());
        return new GenericAddResponse(method.getId());
    }

    @Override
    public void updateMethod(Integer id, AddMethodRequest request) throws MiddlewareQueryException, MiddlewareException {
        Method method = new Method(new Term(id, request.getName(), request.getDescription()));
        ontologyService.updateMethod(method);
    }

    @Override
    public void deleteMethod(Integer id) throws MiddlewareQueryException {
        ontologyService.deleteMethod(id);
    }

    @Override
    public List<PropertySummary> getAllProperties() throws MiddlewareQueryException {
        List<Property> propertyList = ontologyService.getAllProperties();
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
        Property property = ontologyService.getProperty(id);
        if (property == null) return null;
        ModelMapper mapper = OntologyMapper.propertyMapper();
        return mapper.map(property, PropertyResponse.class);
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
}
