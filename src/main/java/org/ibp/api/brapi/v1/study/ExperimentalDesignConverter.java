package org.ibp.api.brapi.v1.study;

import org.generationcp.middleware.service.api.study.StudyMetadata;
import org.ibp.api.brapi.v2.study.ExperimentalDesign;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

public class ExperimentalDesignConverter implements Converter<StudyMetadata, ExperimentalDesign> {

	@Override public ExperimentalDesign convert(final MappingContext<StudyMetadata, ExperimentalDesign> context) {
		final ExperimentalDesign experimentalDesign = new ExperimentalDesign();
		experimentalDesign.setDescription(context.getSource().getExperimentalDesign());
		experimentalDesign.setPUI(context.getSource().getExperimentalDesignId());
		return context.getMappingEngine().map(context.create(experimentalDesign, context.getDestinationType()));
	}

}
