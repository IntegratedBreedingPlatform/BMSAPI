package org.generationcp.bms.ontology.services;


import org.generationcp.bms.ontology.dto.*;
import org.generationcp.bms.ontology.dto.MethodRequest;
import org.generationcp.bms.ontology.dto.PropertyRequest;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import java.util.List;

public interface OntologyModelService {

    public List<MethodSummary> getAllMethods() throws MiddlewareQueryException;
    public MethodResponse getMethod(Integer id) throws MiddlewareQueryException;
    public GenericResponse addMethod(MethodRequest method) throws MiddlewareQueryException;
    public void updateMethod(Integer id, MethodRequest request) throws MiddlewareQueryException, MiddlewareException;
    public void deleteMethod(Integer id) throws MiddlewareQueryException;

    public List<PropertySummary> getAllProperties() throws MiddlewareQueryException;
    public PropertyResponse getProperty(Integer id) throws MiddlewareQueryException;
    public GenericResponse addProperty(PropertyRequest request) throws MiddlewareQueryException;
    public List<PropertySummary> getAllPropertiesByClass(String propertyClass) throws MiddlewareQueryException;
    public List<PropertySummary> getAllPropertiesByFilter(String filter) throws MiddlewareQueryException;
    public List<PropertySummary> getAllPropertiesByClasses(List<String> classes) throws MiddlewareQueryException;

    public List<DataTypeSummary> getAllDataTypes() throws MiddlewareQueryException;

    public List<String> getAllClasses() throws MiddlewareQueryException;
}
