package org.generationcp.bms.ontology.services;


import org.generationcp.bms.ontology.dto.*;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import java.util.List;

public interface OntologyModelService {

    /**
     * get all methods using middleware ontology service
     * @return list of methods
     * @throws MiddlewareQueryException
     */
    public List<MethodSummary> getAllMethods() throws MiddlewareQueryException;

    /**
     * get method by method id
     * @param id the method id
     * @return method
     * @throws MiddlewareQueryException
     */
    public MethodResponse getMethod(Integer id) throws MiddlewareQueryException;

    /**
     * add method using given input data
     * @param method method to add
     * @return newly created method id
     * @throws MiddlewareQueryException
     */
    public GenericResponse addMethod(MethodRequest method) throws MiddlewareQueryException;

    /**
     * update method data using given method id
     * @param id method to be updated
     * @param request method data to be updated
     * @throws MiddlewareQueryException, MiddlewareException
     */
    public void updateMethod(Integer id, MethodRequest request) throws MiddlewareQueryException, MiddlewareException;

    /**
     * delete method using given id
     * @param id method to be deleted
     * @throws MiddlewareQueryException
     */
    public void deleteMethod(Integer id) throws MiddlewareQueryException;


    /**
     * get all properties
     * @return list of properties
     * @throws MiddlewareQueryException
     */
    public List<PropertySummary> getAllProperties() throws MiddlewareQueryException;

    /**
     * get property by given property id
     * @param id property id
     * @return property that have given id
     * @throws MiddlewareQueryException
     */
    public PropertyResponse getProperty(Integer id) throws MiddlewareQueryException;

    /**
     * add property using given input data
     * @param request property data to be added
     * @return newly created property id
     * @throws MiddlewareQueryException
     */
    public GenericResponse addProperty(PropertyRequest request) throws MiddlewareQueryException;

    /**
     * get all properties containing class name
     * @param propertyClass class name to be search in property
     * @return list of properties
     * @throws MiddlewareQueryException
     */
    public List<PropertySummary> getAllPropertiesByClass(String propertyClass) throws MiddlewareQueryException;

    /**
     * get all data types
     * @return list of data types
     * @throws MiddlewareQueryException
     */
    public List<DataTypeSummary> getAllDataTypes() throws MiddlewareQueryException;

    /**
     * get all classes
     * @return list of classes
     * @throws MiddlewareQueryException
     */
    public List<String> getAllClasses() throws MiddlewareQueryException;
}
