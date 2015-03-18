package org.generationcp.bms.ontology.services;


import org.generationcp.bms.ontology.dto.*;
import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Scale;
import org.modelmapper.*;
import org.modelmapper.spi.MappingContext;

import java.util.ArrayList;

public class OntologyMapper {

    private static volatile ModelMapper SINGLETON = new ModelMapper();

    private OntologyMapper(){  }

    /**
     * Eager Initialization of ModelMapper Instance
     * Used when Simple Class to Class Mapping is Required without Custom Mapping
     * @return ModelMapper Instance
     */
    public static ModelMapper getInstance(){
        return SINGLETON;
    }

    /**
     * Custom Mapping for Middleware Method Class to MethodSummary
     * Definition to Description Mapping
     */
    private static PropertyMap<Method, MethodSummary> methodMap = new PropertyMap<Method, MethodSummary>() {
        @Override
        protected void configure() {
            map().setId(source.getId());
            map().setName(source.getName());
            map().setDescription(source.getDefinition());
        }
    };

    private static PropertyMap<Method, MethodResponse> methodResponseMap = new PropertyMap<Method, MethodResponse>() {
        @Override
        protected void configure() {
            map().setId(source.getId());
            map().setName(source.getName());
            map().setDescription(source.getDefinition());
            map().setEditableFields(new ArrayList<String>());
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
    private static PropertyMap<Property, PropertySummary> propertyMap = new PropertyMap<Property, PropertySummary>() {

        @Override
        protected void configure() {
            map().setId(source.getId());
            map().setName(source.getName());
            map().setDescription(source.getDefinition());
            map().setCropOntologyId(source.getCropOntologyId());
            map().setClasses(source.getClassNames());
        }
    };

    private static PropertyMap<Property, PropertyResponse> propertyResponseMap = new PropertyMap<Property, PropertyResponse>() {
        @Override
        protected void configure() {
            map().setId(source.getId());
            map().setName(source.getName());
            map().setDescription(source.getDefinition());
            map().setCropOntologyId(source.getCropOntologyId());
            map().setClasses(source.getClassNames());
            map().setEditableFields(new ArrayList<String>());
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

    /**
     * Custom Mapping for Middleware Scale Class to ScaleSummary
     * Definition to Description Mapping
     */
    private static PropertyMap<Scale, ScaleSummary> scaleMap = new PropertyMap<Scale, ScaleSummary>() {
        @Override
        protected void configure() {
            map().setId(source.getId());
            map().setName(source.getName());
            map().setDescription(source.getDefinition());
            map().setMinValue(source.getMinValue());
            map().setMaxValue(source.getMaxValue());
            map().setCategories(source.getCategories());
        }
    };

    private static PropertyMap<Scale, ScaleResponse> scaleResponseMap = new PropertyMap<Scale, ScaleResponse>() {
        @Override
        protected void configure() {
            map().setId(source.getId());
            map().setName(source.getName());
            map().setDescription(source.getDefinition());
            map().setMinValue(source.getMinValue());
            map().setMaxValue(source.getMaxValue());
            map().setCategories(source.getCategories());
            map().setEditableFields(new ArrayList<String>());
            map().setDeletable(false);
        }
    };

    /**
     * Customise Mapped property 'scaleMap' is Initialize in Mapper and Returned
     * @return ModelMapper Instance
     */
    public static ModelMapper scaleMapper(){
        ModelMapper scaleMapper = new ModelMapper();
        scaleMapper.addMappings(scaleMap);
        scaleMapper.addMappings(scaleResponseMap);
        scaleMapper.createTypeMap(DataType.class, IdName.class).setConverter(new Converter<DataType, IdName>() {
            @Override
            public IdName convert(MappingContext<DataType, IdName> mappingContext) {
                return new IdName();
            }
        });
        return scaleMapper;
    }

}
