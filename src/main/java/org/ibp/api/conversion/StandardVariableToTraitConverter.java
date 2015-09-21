package org.ibp.api.conversion;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.ibp.api.domain.study.Trait;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StandardVariableToTraitConverter implements Converter<StandardVariable, Trait>{

	
	
	@Override
	public Trait convert(StandardVariable source) {
		if(source == null)
			return null;
		
		return new Trait(
				source.getId(),
				source.getName());
	}

}
