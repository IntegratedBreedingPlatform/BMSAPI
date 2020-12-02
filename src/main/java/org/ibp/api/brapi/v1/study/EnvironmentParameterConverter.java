package org.ibp.api.brapi.v1.study;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.brapi.v2.study.EnvironmentParameter;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentParameterConverter implements Converter<List<MeasurementVariable>, List<EnvironmentParameter>> {

	@Override public List<EnvironmentParameter> convert(final MappingContext<List<MeasurementVariable>, List<EnvironmentParameter>> context) {
		final List<EnvironmentParameter> environmentParameters = new ArrayList<>();
		for (final MeasurementVariable variable : context.getSource()) {
			final EnvironmentParameter environmentParameter = new EnvironmentParameter();
			environmentParameter.setDescription(variable.getDescription());
			environmentParameter.setValue(variable.getValue());
			environmentParameter.setUnit(variable.getScale());
			environmentParameter.setParameterName(variable.getName());
			environmentParameter.setParameterPUI(String.valueOf(variable.getTermId()));
			environmentParameter.setUnitPUI(variable.getScaleId());
			environmentParameters.add(environmentParameter);
		}
		return context.getMappingEngine().map(context.create(environmentParameters, context.getDestinationType()));
	}

}
