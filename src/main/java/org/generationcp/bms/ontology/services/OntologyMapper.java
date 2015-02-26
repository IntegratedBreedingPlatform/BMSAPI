package org.generationcp.bms.ontology.services;


import org.generationcp.bms.ontology.dto.outgoing.MethodDTO;
import org.generationcp.middleware.domain.oms.Method;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

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
     * Custom Mapping for Middleware Method Class to MethodDTO
     * Definition to Description Mapping
     */
    public static PropertyMap<Method, MethodDTO> methodMap = new PropertyMap<Method, MethodDTO>() {
        @Override
        protected void configure() {
            map().setId(source.getId());
            map().setName(source.getName());
            map().setDescription(source.getDefinition());
        }
    };

    /**
     * Customise Mapped method 'methodMap' is Initialize in Mapper and Returned
     * @return ModelMapper Instance
     */
    public static ModelMapper methodMapper(){
        ModelMapper mapper = new ModelMapper();
        mapper.addMappings(methodMap);
        return mapper;
    }
}
