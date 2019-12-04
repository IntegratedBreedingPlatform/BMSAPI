
package org.ibp.api.domain.location;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class LocationDto {

	private Integer id;
	private String name;
	private String abbreviation;
	private Double latitude;
	private Double longitude;
	private Double altitude;
	private boolean defaultLocation;

	public LocationDto() {

	}

	public LocationDto(
		final Integer id, final String name, final String abbreviation, final Double latitude, final Double longitude,
		final Double altitude, final boolean defaultLocation) {
		this.setId(id);
		this.setName(name);
		this.setAbbreviation(abbreviation);
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.setAltitude(altitude);
		this.setDefaultLocation(defaultLocation);
	}


	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(final String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(final Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(final Double longitude) {
		this.longitude = longitude;
	}

	public Double getAltitude() {
		return altitude;
	}

	public void setAltitude(final Double altitude) {
		this.altitude = altitude;
	}

	public boolean isDefaultLocation() {
		return defaultLocation;
	}

	public void setDefaultLocation(final boolean defaultLocation) {
		this.defaultLocation = defaultLocation;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}
}
