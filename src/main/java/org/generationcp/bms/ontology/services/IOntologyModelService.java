package org.generationcp.bms.ontology.services;


import org.generationcp.bms.ontology.dto.outgoing.*;
import org.generationcp.bms.ontology.dto.incoming.AddMethodRequest;
import org.generationcp.bms.ontology.dto.incoming.AddPropertyRequest;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import java.util.List;

public interface IOntologyModelService {

    public List<MethodSummary> getAllMethods() throws MiddlewareQueryException;
    public MethodResponse getMethod(Integer id) throws MiddlewareQueryException;
    public GenericAddResponse addMethod(AddMethodRequest method) throws MiddlewareQueryException;
    public void updateMethod(Integer id, AddMethodRequest request) throws MiddlewareQueryException, MiddlewareException;
    public void deleteMethod(Integer id) throws MiddlewareQueryException;

    public List<PropertySummary> getAllProperties() throws MiddlewareQueryException;
    public PropertyResponse getProperty(Integer id) throws MiddlewareQueryException;
    public GenericAddResponse addProperty(AddPropertyRequest request) throws MiddlewareQueryException;
    public List<PropertySummary> getAllPropertiesByClass(String propertyClass) throws MiddlewareQueryException;
    public List<PropertySummary> getAllPropertiesByFilter(String filter) throws MiddlewareQueryException;
    public List<PropertySummary> getAllPropertiesByClasses(List<String> classes) throws MiddlewareQueryException;

    public List<DataTypeSummary> getAllDataTypes() throws MiddlewareQueryException;

    public List<String> getAllClasses() throws MiddlewareQueryException;
}
