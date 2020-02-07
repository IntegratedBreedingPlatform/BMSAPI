package org.ibp.api.brapi.v2.location;

import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class LocationMapper {

	private static ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private LocationMapper() {

	}

	static {
		LocationMapper
			.addLocationDataMapping(LocationMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return LocationMapper.applicationWideModelMapper;
	}

	private static void addLocationDataMapping(final ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<org.generationcp.middleware.pojos.Location, Location>() {

			@Override
			protected void configure() {
				this.map().setLocationName(this.source.getLname());
				this.map().setAbbreviation(this.source.getLabbr());
				this.map().setLatitude(this.source.getLatitude());
				this.map().setLongitude(this.source.getLongitude());
				this.map().setAltitude(this.source.getAltitude());
				this.map().setLocationType(this.source.getLtype().toString());
			}

		});
	}
}
