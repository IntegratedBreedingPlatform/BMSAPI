
package org.ibp.api.brapi.v1.location;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.generationcp.middleware.service.api.location.LocationDetailsDto;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class LocationMapper extends ModelMapper{

	private static ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private LocationMapper() {

	}

	static {
		LocationMapper.addLocationDetailsDataMapping(LocationMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return new ModelMapper();
	}

	private static void addLocationDetailsDataMapping(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<LocationDetailsDto, Location>() {

			@Override protected void configure() {
				this.map().setLocationDbId(this.source.getLocationDbId());
				this.map().setName(this.source.getName());
				this.map().setAbbreviation(this.source.getAbbreviation());
				this.map().setLocationType(!StringUtils.isBlank(this.source.getLocationType())
						? WordUtils.capitalize(this.source.getLocationType().toLowerCase()) : "Unknown");
				this.map().setCountryCode(!StringUtils.isBlank(this.source.getCountryCode()) ? this.source.getCountryCode() : "Unknown");
				this.map().setCountryName(!StringUtils.isBlank(this.source.getCountryName()) ? this.source.getCountryName() : "Unknown");
				this.map().setLatitude(this.source.getLatitude());
				this.map().setLongitude(this.source.getLongitude());
				this.map().setAltitude(this.source.getAltitude());
				this.map().setAdditionalInfo(this.source.getAdditionalInfo().getToMap());
			}

		});
	}
}
