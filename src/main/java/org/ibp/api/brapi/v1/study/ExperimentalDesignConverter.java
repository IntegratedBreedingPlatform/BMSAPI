package org.ibp.api.brapi.v1.study;

import org.ibp.api.brapi.v2.study.ExperimentalDesign;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

public class ExperimentalDesignConverter implements Converter<String, ExperimentalDesign> {

	@Override public ExperimentalDesign convert(final MappingContext<String, ExperimentalDesign> context) {
		final ExperimentalDesign experimentalDesign = new ExperimentalDesign();
		experimentalDesign.setDescription(context.getSource());
		return context.getMappingEngine().map(context.create(experimentalDesign, context.getDestinationType()));
	}

}
