package org.ibp.api.java.impl.middleware.design.transformer;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.springframework.stereotype.Component;

@Component
public class StandardVariableTransformer {

	public MeasurementVariable convert(final StandardVariable standardVariable, final VariableType variableType) {
		final MeasurementVariable measurementVariable =
			new MeasurementVariable(standardVariable.getId(), standardVariable.getName(), standardVariable.getDescription(),
				standardVariable
					.getScale().getName(), standardVariable.getMethod().getName(), standardVariable.getProperty().getName(),
				standardVariable
					.getDataType().getName(), "", standardVariable.getPhenotypicType().getLabelList().get(0));
		measurementVariable.setDataTypeId(standardVariable.getDataType().getId());
		measurementVariable.setVariableType(variableType);
		return measurementVariable;
	}
}
