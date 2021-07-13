package org.ibp.api.domain.sample;

import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SampleObservationMapper {

	private static final SimpleDateFormat DATE_FORMAT = DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT_3);

	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	static {
		SampleObservationMapper.addSampleObservationMapper(SampleObservationMapper.applicationWideModelMapper);
	}

	private SampleObservationMapper() {
	}

	public static ModelMapper getInstance() {
		return SampleObservationMapper.applicationWideModelMapper;
	}

	private static void addSampleObservationMapper(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<SampleDetailsDTO, SampleObservationDto>() {

			private final Converter<Date, String> toDateConverter = new AbstractConverter<Date, String>() {

				protected String convert(final Date source) {
					return source == null ? "" : DATE_FORMAT.format(source);
				}
			};

			@Override
			protected void configure() {
				this.map().setStudyDbId(this.source.getStudyDbId());
				this.map().setLocationDbId(this.source.getLocationDbId());
				this.map().setObservationUnitDbId(this.source.getObsUnitId());
				this.map().setSampleDbId(this.source.getSampleBusinessKey());
				this.map().setTakenBy(this.source.getTakenBy());
				this.using(this.toDateConverter).map(this.source.getSampleDate()).setSampleDate(null);
				this.map().setSampleType(this.source.getSampleType());
				this.map().setTissueType(this.source.getTissueType());
				this.map().setNotes(this.source.getNotes());
				this.map().setStudyName(this.source.getStudyName());
				this.map().setSeason(this.source.getSeason());
				this.map().setLocationName(this.source.getLocationName());
				this.map().setEntryNumber(this.source.getEntryNo());
				this.map().setPlotNumber(this.source.getPlotNo());
				this.map().setGermplasmDbId(this.source.getGermplasmUUID());
				this.map().setPlantingDate(this.source.getSeedingDate());
				this.map().setHarvestDate(this.source.getHarvestDate());
			}
		});
	}
}

