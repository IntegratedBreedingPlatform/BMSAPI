package org.ibp.api.java.impl.middleware.ontology;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.ibp.api.domain.study.Trait;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.ontology.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

@Service
public class OntologyServiceImpl implements OntologyService {

	@Autowired
	private org.generationcp.middleware.service.api.OntologyService ontologyService;
	@Autowired
	private ConversionService converter;

	@Override
	public List<Trait> getTraitGroups() {
		try {
			List<Property> properties = ontologyService.getAllPropertiesWithTraitClass();
			return convert(properties, Trait.class);

		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			throw new ApiRuntimeException("Couldn't convert Property to TraitGroup", e);
		}
	}

	@Override
	public List<Trait> getTraitsByGroup(int groupId) {
		try {
			List<StandardVariable> traits = ontologyService.getStandardVariablesByProperty(groupId);
			
			return convert(traits, Trait.class); 

		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			throw new ApiRuntimeException("Couldn't convert StandardVariable to Trait", e);
		}
	}
	
	private final <T,S>List<T> convert(List<S> beanList, Class<T> clazz){
        if(null == beanList) return null;
        
        List<T> convertedList = new ArrayList<>();
        for(S s : beanList){
                convertedList.add(converter.convert(s, clazz));
        }
        return convertedList;
	}
	

}
