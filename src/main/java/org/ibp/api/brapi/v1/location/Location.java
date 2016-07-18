
package org.ibp.api.brapi.v1.location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"locationDbId", "name", "countryCode", "countryName", "latitude", "longitude", "altitude", "attributes"})
public class Location {

	@JsonProperty("locationDbId")
	private Integer locationDbId;

	@JsonProperty("name")
	private String name;

	@JsonProperty("countryCode")
	private String countryCode;

	@JsonProperty("countryName")
	private String countryName;

	@JsonProperty("latitude")
	private Double latitude;

	@JsonProperty("longitude")
	private Double longitude;

	@JsonProperty("altitude")
	private Double altitude;

	@JsonProperty("attributes")
	private List<Object> attributes = new ArrayList<Object>();

	@JsonIgnore
	private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public Location() {
	}

	/**
	 *
	 * @param countryName
	 * @param altitude
	 * @param name
	 * @param countryCode
	 * @param longitude
	 * @param attributes
	 * @param latitude
	 * @param locationDbId
	 */
	public Location(final Integer locationDbId, final String name, final String countryCode, final String countryName, final Double latitude,
			final Double longitude, final Double altitude, final List<Object> attributes) {
		this.locationDbId = locationDbId;
		this.name = name;
		this.countryCode = countryCode;
		this.countryName = countryName;
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.attributes = attributes;
	}

	/**
	 *
	 * @return The locationDbId
	 */
	@JsonProperty("locationDbId")
	public Integer getLocationDbId() {
		return this.locationDbId;
	}

	/**
	 *
	 * @param locationDbId The locationDbId
	 */
	@JsonProperty("locationDbId")
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
	@JsonProperty("name")
	public String getName() {
		return this.name;
	}

	/**
	 *
	 * @param name The name
	 */
	@JsonProperty("name")
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
	@JsonProperty("countryCode")
	public String getCountryCode() {
		return this.countryCode;
	}

	/**
	 *
	 * @param countryCode The countryCode
	 */
	@JsonProperty("countryCode")
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
	@JsonProperty("countryName")
	public String getCountryName() {
		return this.countryName;
	}

	/**
	 *
	 * @param countryName The countryName
	 */
	@JsonProperty("countryName")
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
	@JsonProperty("latitude")
	public Double getLatitude() {
		return this.latitude;
	}

	/**
	 *
	 * @param latitude The latitude
	 */
	@JsonProperty("latitude")
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
	@JsonProperty("longitude")
	public Double getLongitude() {
		return this.longitude;
	}

	/**
	 *
	 * @param longitude The longitude
	 */
	@JsonProperty("longitude")
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
	@JsonProperty("altitude")
	public Double getAltitude() {
		return this.altitude;
	}

	/**
	 *
	 * @param altitude The altitude
	 */
	@JsonProperty("altitude")
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
	@JsonProperty("attributes")
	public List<Object> getAttributes() {
		return this.attributes;
	}

	/**
	 *
	 * @param attributes The attributes
	 */
	@JsonProperty("attributes")
	public void setAttributes(final List<Object> attributes) {
		this.attributes = attributes;
	}

	public Location withAttributes(final List<Object> attributes) {
		this.attributes = attributes;
		return this;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(final String name, final Object value) {
		this.additionalProperties.put(name, value);
	}

	public Location withAdditionalProperty(final String name, final Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

}
