package org.generationcp.bms.ontology.services;


import org.generationcp.bms.ontology.dto.*;
import org.generationcp.middleware.domain.oms.*;
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

    /**
     * Custom Mapping for Middleware OntologyVariableSummary Class to VariableSummary
     * Definition to Description Mapping
     */
    private static PropertyMap<OntologyVariableSummary, VariableSummary> variableMap = new PropertyMap<OntologyVariableSummary, VariableSummary>() {
        @Override
        protected void configure() {
            map().setId(source.getId());
            map().setName(source.getName());
            map().setDescription(source.getDescription());
            map().setFavourite(source.getIsFavorite());
            map().setAlias("");
            map().setVariableTypeIds(source.getVariableTypes());
            map().setCreatedDate(source.getDateCreated());
            map().setModifiedData(source.getDateLastModified());
            map().setObservations(source.getObservations());
            map().setExpectedMin(source.getMinValue());
            map().setExpectedMax(source.getMaxValue());
        }
    };

    /**
     * Customise Mapped property 'variableMap' is Initialize in Mapper and Returned
     * @return ModelMapper Instance
     */
    public static ModelMapper variableMapper(){
        ModelMapper variableMapper = new ModelMapper();
        variableMapper.addMappings(variableMap);
        variableMapper.createTypeMap(TermSummary.class, IdName.class).setConverter(new Converter<TermSummary, IdName>() {
            @Override
            public IdName convert(MappingContext<TermSummary, IdName> mappingContext) {
                return new IdName();
            }
        });
        return variableMapper;
    }
}
