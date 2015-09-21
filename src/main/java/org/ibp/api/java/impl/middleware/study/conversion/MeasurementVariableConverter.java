package org.ibp.api.java.impl.middleware.study.conversion;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.domain.study.Trait;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MeasurementVariableConverter implements Converter<Trait, MeasurementVariable>{

	@Override
	public MeasurementVariable convert(Trait source) {

		MeasurementVariable measurementVariable = new MeasurementVariable();

		//TODO get from database by traitId
		measurementVariable.setTermId(source.getTraitId());
		measurementVariable.setName(source.getTraitName());
		measurementVariable.setDescription(source.getTraitName());
		measurementVariable.setProperty("prop");
		measurementVariable.setMethod("method");
		measurementVariable.setScale("scale");
		measurementVariable.setDataType("N");
		measurementVariable.setValue(null);
		measurementVariable.setLabel(source.getTraitName());
		
		measurementVariable.setRole(PhenotypicType.VARIATE);

		measurementVariable.setFactor(false);
		
		return measurementVariable;
	}

}
