package org.generationcp.bms.ontology.services;


import org.generationcp.bms.ontology.dto.outgoing.MethodSummary;
import org.generationcp.bms.ontology.dto.outgoing.MethodResponse;
import org.generationcp.bms.ontology.dto.outgoing.PropertyResponse;
import org.generationcp.bms.ontology.dto.outgoing.PropertySummary;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.domain.oms.Property;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import java.util.Arrays;
import java.util.ArrayList;

public class OntologyMapper {

    private static volatile ModelMapper SINGLETON = new ModelMapper();

    private OntologyMapper(){  }

    /**
     * Eager Initialization of ModelMapper Instance
     * Used when Simple Class to Class Mapping is Required without Custom Mapping
     * @return ModelMapper Instance
     */
    public static final ModelMapper getInstance(){
        return SINGLETON;
    }

    /**
     * Custom Mapping for Middleware Method Class to MethodSummary
     * Definition to Description Mapping
     */
    public static PropertyMap<Method, MethodSummary> methodMap = new PropertyMap<Method, MethodSummary>() {
        @Override
        protected void configure() {
            map().setId(source.getId());
            map().setName(source.getName());
            map().setDescription(source.getDefinition());
        }
    };

    public static PropertyMap<Method, MethodResponse> methodResponseMap = new PropertyMap<Method, MethodResponse>() {
        @Override
        protected void configure() {
            map().setId(source.getId());
            map().setName(source.getName());
            map().setDescription(source.getDefinition());
            map().setEditableFields(new ArrayList<>(Arrays.asList("description")));
            map().setDeletable(false);
        }
    };

    /**
     * Customise Mapped method 'methodMap' is Initialize in Mapper and Returned
     * @return ModelMapper Instance
     */
    public static ModelMapper methodMapper(){
        ModelMapper mapper = new ModelMapper();
        mapper.addMappings(methodMap);
        mapper.addMappings(methodResponseMap);
        return mapper;
    }


    /**
     * Custom Mapping for Middleware Property Class to PropertySummary
     * Definition to Description Mapping
     */
    public static PropertyMap<Property, PropertySummary> propertyMap = new PropertyMap<Property, PropertySummary>() {

        @Override
        protected void configure() {
            map().setId(source.getId());
            map().setName(source.getName());
            map().setDescription(source.getDefinition());
            map().setCropOntologyId(source.getCropOntologyId());
            map().setClasses(source.getClassNames());
        }
    };

    public static PropertyMap<Property, PropertyResponse> propertyResponseMap = new PropertyMap<Property, PropertyResponse>() {
        @Override
        protected void configure() {
            map().setId(source.getId());
            map().setName(source.getName());
            map().setDescription(source.getDefinition());
            map().setCropOntologyId(source.getCropOntologyId());
            map().setClasses(source.getClassNames());
            map().setEditableFields(new ArrayList<>(Arrays.asList("description")));
            map().setDeletable(false);
        }
    };

    /**
     * Customise Mapped property 'propertyMap' is Initialize in Mapper and Returned
     * @return ModelMapper Instance
     */
    public static ModelMapper propertyMapper(){
        ModelMapper mapper = new ModelMapper();
        mapper.addMappings(propertyMap);
        mapper.addMappings(propertyResponseMap);
        return mapper;
    }
}
