
package org.ibp.api.java.impl.middleware.study.conversion;

import org.generationcp.middleware.domain.oms.Property;
import org.ibp.api.domain.study.Trait;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PropertyToTraitConverter implements Converter<Property, Trait> {

	@Override
	public Trait convert(final Property source) {
		if (source == null || source.getTerm() == null) {
			return null;
		}

		return new Trait(source.getTerm().getId(), source.getTerm().getName());
	}

}
