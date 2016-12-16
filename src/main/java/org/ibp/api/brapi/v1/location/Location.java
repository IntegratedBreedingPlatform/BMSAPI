
package org.ibp.api.brapi.v1.location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"locationDbId", "locationType", "name", "abbreviation", "countryCode", "countryName", "latitude", "longitude",
		"altitude", "attributes", "additionalInfo"})
public class Location {

	private Integer locationDbId;

	private String locationType;

	private String name;

	private String abbreviation;

	private String countryCode;

	private String countryName;

	private Double latitude;

	private Double longitude;

	private Double altitude;

	@JsonInclude(Include.NON_EMPTY)
	private List<Object> attributes = new ArrayList<Object>();

	private Map<String, Object> additionalInfo = new HashMap<>();

	/**
	 * No args constructor required by serialization libraries.
	 */
	public Location() {
	}

	public Location(final Integer locationDbId, final String locationType, final String name, final String abbreviation,
			final String countryCode, final String countryName, final Double latitude, final Double longitude, final Double altitude,
			final List<Object> attributes, final Map<String, Object> additionalInfo) {
		this.locationDbId = locationDbId;
		this.locationType = locationType;
		this.name = name;
		this.abbreviation = abbreviation;
		this.countryCode = countryCode;
		this.countryName = countryName;
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.attributes = attributes;
		this.additionalInfo = additionalInfo;
	}

	/**
	 *
	 * @return The locationDbId
	 */
	public Integer getLocationDbId() {
		return this.locationDbId;
	}

	/**
	 *
	 * @param locationDbId The locationDbId
	 */
	public void setLocationDbId(final Integer locationDbId) {
		this.locationDbId = locationDbId;
	}

	public Location withLocationDbId(final Integer locationDbId) {
		this.locationDbId = locationDbId;
		return this;
	}

	/**
	 *
	 * @return The name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 *
	 * @param name The name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	public Location withName(final String name) {
		this.name = name;
		return this;
	}

	/**
	 *
	 * @return The countryCode
	 */
	public String getCountryCode() {
		return this.countryCode;
	}

	/**
	 *
	 * @param countryCode The countryCode
	 */
	public void setCountryCode(final String countryCode) {
		this.countryCode = countryCode;
	}

	public Location withCountryCode(final String countryCode) {
		this.countryCode = countryCode;
		return this;
	}

	/**
	 *
	 * @return The countryName
	 */
	public String getCountryName() {
		return this.countryName;
	}

	/**
	 *
	 * @param countryName The countryName
	 */
	public void setCountryName(final String countryName) {
		this.countryName = countryName;
	}

	public Location withCountryName(final String countryName) {
		this.countryName = countryName;
		return this;
	}

	/**
	 *
	 * @return The latitude
	 */
	public Double getLatitude() {
		return this.latitude;
	}

	/**
	 *
	 * @param latitude The latitude
	 */
	public void setLatitude(final Double latitude) {
		this.latitude = latitude;
	}

	public Location withLatitude(final Double latitude) {
		this.latitude = latitude;
		return this;
	}

	/**
	 *
	 * @return The longitude
	 */
	public Double getLongitude() {
		return this.longitude;
	}

	/**
	 *
	 * @param longitude The longitude
	 */
	public void setLongitude(final Double longitude) {
		this.longitude = longitude;
	}

	public Location withLongitude(final Double longitude) {
		this.longitude = longitude;
		return this;
	}

	/**
	 *
	 * @return The altitude
	 */
	public Double getAltitude() {
		return this.altitude;
	}

	/**
	 *
	 * @param altitude The altitude
	 */
	public void setAltitude(final Double altitude) {
		this.altitude = altitude;
	}

	public Location withAltitude(final Double altitude) {
		this.altitude = altitude;
		return this;
	}

	/**
	 *
	 * @return The attributes
	 */
	public List<Object> getAttributes() {
		return this.attributes;
	}

	/**
	 *
	 * @param attributes The attributes
	 */
	public void setAttributes(final List<Object> attributes) {
		this.attributes = attributes;
	}

	public Location withAttributes(final List<Object> attributes) {
		this.attributes = attributes;
		return this;
	}

	public String getLocationType() {
		return this.locationType;
	}

	public void setLocationType(final String locationType) {
		this.locationType = locationType;
	}

	public String getAbbreviation() {
		return this.abbreviation;
	}

	public void setAbbreviation(final String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public Map<String, Object> getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(final Map<String, Object> additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Location)) {
			return false;
		}
		final Location castOther = (Location) other;
		return new EqualsBuilder().append(this.locationDbId, castOther.locationDbId).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.locationDbId).hashCode();
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}
}
