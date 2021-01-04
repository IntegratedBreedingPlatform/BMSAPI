
package org.ibp.api.brapi.v1.location;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.generationcp.middleware.api.brapi.v1.location.LocationDetailsDto;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class LocationMapper {

	private static ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private static Converter<String, String> toCapitalize = new AbstractConverter<String, String>() {

		protected String convert(final String source) {
			return !StringUtils.isBlank(source)
				? WordUtils.capitalize(source.toLowerCase()) : source;
		}
	};

	private LocationMapper() {

	}

	static {
		LocationMapper.addLocationDetailsDataMapping(LocationMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return LocationMapper.applicationWideModelMapper;
	}

	private static void addLocationDetailsDataMapping(final ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<LocationDetailsDto, Location>() {

			@Override protected void configure() {

				this.map().setLocationDbId(String.valueOf(this.source.getLocationDbId()));
				this.map().setName(this.source.getName());
				this.map().setAbbreviation(this.source.getAbbreviation());
				this.map().setLatitude(this.source.getLatitude());
				this.map().setLongitude(this.source.getLongitude());
				this.map().setAltitude(this.source.getAltitude());
				this.map().setAdditionalInfo(this.source.getAdditionalInfo().getToMap());
				this.map().setCountryName(this.source.getCountryName());
				this.map().setCountryCode(this.source.getCountryCode());
				using(toCapitalize).map().setLocationType(this.source.getLocationType());
			}

		});
	}
}
