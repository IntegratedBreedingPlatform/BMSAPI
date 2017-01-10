
package org.ibp.api.brapi.v1.location;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.generationcp.middleware.service.api.location.LocationDetailsDto;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class LocationMapper {

	private static ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private static Converter<String, String> toUnknown = new AbstractConverter<String, String>() {
		protected String convert(String source) {
			return !StringUtils.isBlank(source) ? source : "Unknown";
		}
	};

	private static Converter<String, String> toCapitalize = new AbstractConverter<String, String>() {
		protected String convert(String source) {
			return !StringUtils.isBlank(source)
									? WordUtils.capitalize(source.toLowerCase()) : "Unknown";
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

				this.map().setLocationDbId(this.source.getLocationDbId());
				this.map().setName(this.source.getName());
				this.map().setAbbreviation(this.source.getAbbreviation());
				this.map().setLatitude(this.source.getLatitude());
				this.map().setLongitude(this.source.getLongitude());
				this.map().setAltitude(this.source.getAltitude());
				this.map().setAdditionalInfo(this.source.getAdditionalInfo().getToMap());
				using(toUnknown).map().setCountryName(source.getCountryName());
				using(toUnknown).map().setCountryCode(source.getCountryCode());
				using(toCapitalize).map().setLocationType(source.getLocationType());
			}

		});
	}
}
