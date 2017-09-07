package org.ibp.api.domain.sample;

import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SampleMapper {

	private static final SimpleDateFormat DATE_FORMAT = DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT_3);

	private static ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	static {
		SampleMapper.addSampleMapper(SampleMapper.applicationWideModelMapper);
	}

	private SampleMapper() { }

	public static ModelMapper getInstance() {
		return SampleMapper.applicationWideModelMapper;
	}

	private static void addSampleMapper(ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<SampleDTO, org.ibp.api.domain.sample.SampleDTO>() {

			private Converter<Date, String> dateConverter = new AbstractConverter<Date, String>() {

				protected String convert(final Date source) {
					return source == null ? "" : DATE_FORMAT.format(source);
				}
			};

			@Override
			protected void configure() {
				this.map().setSampleName(this.source.getSampleName());
				this.map().setSampleBusinessKey(this.source.getSampleBusinessKey());
				this.map().setTakenBy(this.source.getTakenBy());
				this.using(dateConverter).map(this.source.getSamplingDate()).setSamplingDate(null);
				this.map().setSampleList(this.source.getSampleList());
				this.map().setPlantNumber(this.source.getPlantNumber());
				this.map().setPlantBusinessKey(this.source.getPlantBusinessKey());
			}
		});
	}
}
