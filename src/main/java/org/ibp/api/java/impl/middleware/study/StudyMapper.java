
package org.ibp.api.java.impl.middleware.study;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.service.api.study.TraitDto;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.MeasurementIdentifier;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.Trait;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

public class StudyMapper {

	private static ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

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
		StudyMapper.addStudyGermplasmDtoMapping(StudyMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return StudyMapper.applicationWideModelMapper;
	}

	private static class MeasurementDtoConverter implements Converter<List<MeasurementDto>, List<Measurement>> {

		@Override
		public List<Measurement> convert(final MappingContext<List<MeasurementDto>, List<Measurement>> context) {
			final List<Measurement> measurements = new ArrayList<Measurement>();
			for (final MeasurementDto measurementDto : context.getSource()) {
				final TraitDto trait = measurementDto.getTrait();
				measurements.add(new Measurement(new MeasurementIdentifier(measurementDto.getPhenotypeId(), new Trait(trait.getTraitId(),
						trait.getTraitName())), measurementDto.getTriatValue()));
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
				this.map().setReplicationNumber(this.source.getRepitionNumber());
				this.map().setSeedSource(this.source.getSeedSource());
				this.using(new MeasurementDtoConverter()).map(this.source.getTraitMeasurements()).setMeasurements(null);
			}
		});
	}

	private static void addStudyGermplasmDtoMapping(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<StudyGermplasmDto, StudyGermplasm>() {

			@Override
			protected void configure() {
				this.map().setEntryNumber(this.source.getEntryNumber());
				this.map().setEntryType(this.source.getEntryType());
				this.map().setPosition(this.source.getPosition());
				this.map().getGermplasmListEntrySummary().setGid(this.source.getGermplasmId());
				this.map().getGermplasmListEntrySummary().setCross(this.source.getCross());
				this.map().getGermplasmListEntrySummary().setDesignation(this.source.getDesignation());
				this.map().getGermplasmListEntrySummary().setEntryCode(this.source.getEntryCode());
				this.map().getGermplasmListEntrySummary().setSeedSource(this.source.getSeedSource());
			}
		});
	}
}
