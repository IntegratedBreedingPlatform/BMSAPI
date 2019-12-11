package org.ibp.api.domain.location;

import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class LocationMapper {

	private static ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private LocationMapper() {

	}

	static {
		org.ibp.api.domain.location.LocationMapper
			.addLocationDataMapping(org.ibp.api.domain.location.LocationMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return org.ibp.api.domain.location.LocationMapper.applicationWideModelMapper;
	}

	private static void addLocationDataMapping(final ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<org.generationcp.middleware.pojos.Location, LocationDto>() {

			@Override
			protected void configure() {

				this.map().setId(this.source.getLocid());
				this.map().setName(this.source.getLname());
				this.map().setAbbreviation(this.source.getLabbr());
				this.map().setLatitude(this.source.getLatitude());
				this.map().setLongitude(this.source.getLongitude());
				this.map().setAltitude(this.source.getAltitude());
			}

		});
	}
}
