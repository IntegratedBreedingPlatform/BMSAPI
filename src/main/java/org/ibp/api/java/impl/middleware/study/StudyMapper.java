
package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.MeasurementIdentifier;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.Trait;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

import java.util.ArrayList;
import java.util.List;

public class StudyMapper {

	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	/**
	 * We do not want public constructor of this class as all methods are static
	 */
	private StudyMapper() {

	}

	/**
	 * Configuring the application wide {@link ModelMapper} with ontology related configuration.
	 */
	static {
		StudyMapper.addObservationMapper(StudyMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return StudyMapper.applicationWideModelMapper;
	}

	private static class MeasurementDtoConverter implements Converter<List<MeasurementDto>, List<Measurement>> {

		@Override
		public List<Measurement> convert(final MappingContext<List<MeasurementDto>, List<Measurement>> context) {
			final List<Measurement> measurements = new ArrayList<Measurement>();
			for (final MeasurementDto measurementDto : context.getSource()) {
				final MeasurementVariableDto measurementVariable = measurementDto.getMeasurementVariable();
				measurements.add(new Measurement(new MeasurementIdentifier(measurementDto.getPhenotypeId(), new Trait(
					measurementVariable.getId(),
					measurementVariable.getName())), measurementDto.getVariableValue(), measurementDto.getValueStatus()));
			}
			return context.getMappingEngine().map(context.create(measurements, context.getDestinationType()));
		}
	}

	private static void addObservationMapper(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<ObservationDto, Observation>() {

			@Override
			protected void configure() {
				this.map().setUniqueIdentifier(this.source.getMeasurementId());
				this.map().setEntryNumber(this.source.getEntryNo());
				this.map().setEntryType(this.source.getEntryType());
				this.map().setEnvironmentNumber(this.source.getTrialInstance());
				this.map().setGermplasmDesignation(this.source.getDesignation());
				this.map().setGermplasmId(this.source.getGid());
				this.map().setPlotNumber(this.source.getPlotNumber());
				this.map().setObsUnitId(this.source.getObsUnitId());
				this.map().setRowNumber(this.source.getRowNumber());
				this.map().setColumnNumber(this.source.getColumnNumber());
				this.map().setAdditionalGermplasmDescriptors(this.source.getAdditionalGermplasmDescriptors());
				this.map().setReplicationNumber(this.source.getRepitionNumber());
				this.map().setEntryCode(this.source.getEntryCode());
				this.using(new MeasurementDtoConverter()).map(this.source.getVariableMeasurements()).setMeasurements(null);
			}
		});
	}
}
