
package org.ibp.api.domain.location;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Location {

	private String id;
	private String name;
	private String abbreviation;
	private LocationType locationType;
	private Double latitude;
	private Double longitude;
	private Double altitude;

	public Location() {

	}

	public Location(String id, String name, String abbreviation, LocationType locationType, 
			Double latitude, Double longitude, Double altitude) {
		this.id = id;
		this.name = name;
		this.abbreviation = abbreviation;
		this.locationType = locationType;
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAbbreviation() {
		return this.abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public LocationType getLocationType() {
		return this.locationType;
	}

	public void setLocationType(LocationType locationType) {
		this.locationType = locationType;
	}

	public Double getLatitude() {
		return this.latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return this.longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getAltitude() {
		return this.altitude;
	}

	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Location)) {
			return false;
		}
		Location castOther = (Location) other;
		return new EqualsBuilder().append(this.id, castOther.id).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.id).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", this.id).append("name", this.name).append("abbreviation", this.abbreviation).append("locationType", this.locationType)
				.append("latitude", this.latitude).append("longitude", this.longitude).append("altitude", this.altitude).toString();
	}
}
