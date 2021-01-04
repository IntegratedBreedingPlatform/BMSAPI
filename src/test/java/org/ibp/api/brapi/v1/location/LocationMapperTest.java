package org.ibp.api.brapi.v1.location;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.generationcp.middleware.api.brapi.v1.location.LocationDetailsDto;
import org.junit.Test;
import org.modelmapper.ModelMapper;

public class LocationMapperTest {

	@Test
	public void locationDetailsMapperTest () {
		final ModelMapper mapper = LocationMapper.getInstance();
		LocationDetailsDto locationDetailsDto =
				new LocationDetailsDto(156, "COUNTRY", null, "NZL", "NZL", "", 156.2, 58.6, 5.2);
		Location location = mapper.map(locationDetailsDto, Location.class);
		System.out.println(location);

		assertThat(locationDetailsDto.getLocationDbId().toString(), equalTo(location.getLocationDbId()));
		assertThat(location.getLocationType(), equalTo("Country"));
		assertThat(location.getName(), equalTo(null));
		assertThat(location.getAbbreviation(), equalTo(locationDetailsDto.getAbbreviation()));
		assertThat(location.getCountryCode(), equalTo(locationDetailsDto.getCountryCode()));
		assertThat(location.getCountryName(), equalTo(""));
		assertThat(location.getLatitude(), equalTo(locationDetailsDto.getLatitude()));
		assertThat(location.getLongitude(), equalTo(locationDetailsDto.getLongitude()));
		assertThat(location.getAltitude(), equalTo(locationDetailsDto.getAltitude()));
	}

}
