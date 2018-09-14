package org.ibp.api.rest.sample;

import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.pojos.SampleList;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

import java.util.Date;

public class SampleListMapper {

	private static ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private SampleListMapper() {
	}

	static {
		addMappings(applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return applicationWideModelMapper;
	}

	private static void addMappings(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<SampleList, SampleListDto>() {

			@Override
			protected void configure() {
				map().setCreatedBy(this.source.getCreatedBy().getName());
				using(new AbstractConverter<Date, String>() {

					@Override
					protected String convert(final Date date) {
						return DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT_3).format(date);
					}
				}).map(this.source.getCreatedDate()).setCreatedDate(null);
			}
		});
	}

}
