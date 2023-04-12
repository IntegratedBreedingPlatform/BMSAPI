package org.ibp.api.brapi.v2.location;

import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class LocationMapper {

	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private LocationMapper() {

	}

	static {
		LocationMapper.addLocationDtoDataMapping(LocationMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return LocationMapper.applicationWideModelMapper;
	}

	private static void addLocationDtoDataMapping(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<LocationSearchRequestDto, LocationSearchRequest>() {

			@Override
			protected void configure() {
				this.map().setAbbreviations(this.source.getAbbreviations());
				this.map().setCoordinates(this.source.getCoordinates());
				this.map().setLocationDbIds(this.source.getLocationDbIds());
				this.map().setLocationNames(this.source.getLocationNames());
				this.map().setLocationTypes(this.source.getLocationTypes());
				this.map().setAltitudeMax(this.source.getAltitudeMax());
				this.map().setAltitudeMin(this.source.getAltitudeMin());
				this.map().setCountryCodes(this.source.getCountryCodes());
				this.map().setCountryNames(this.source.getCountryNames());
			}
		});
	}
}
