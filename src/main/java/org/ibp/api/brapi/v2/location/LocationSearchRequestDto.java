package org.ibp.api.brapi.v2.location;

import org.generationcp.middleware.api.location.Coordinate;
import org.generationcp.middleware.domain.search_request.SearchRequestDto;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.ArrayList;
import java.util.List;

@AutoProperty
public class LocationSearchRequestDto extends SearchRequestDto {

	private List<Integer> locationDbIds = new ArrayList<>();
	private List<String> locationNames = new ArrayList<>();
	private List<String> locationTypes = new ArrayList<>();
	private List<String> abbreviations = new ArrayList<>();

	private Double altitudeMin;
	private Double altitudeMax;

	private Coordinate coordinates;
	private List<String> countryCodes = new ArrayList<>();
	private List<String> countryNames = new ArrayList<>();

	public LocationSearchRequestDto() {

	}

	public List<Integer> getLocationDbIds() {
		return this.locationDbIds;
	}

	public void setLocationDbIds(final List<Integer> locationDbIds) {
		this.locationDbIds = locationDbIds;
	}

	public List<String> getAbbreviations() {
		return this.abbreviations;
	}

	public void setAbbreviations(final List<String> abbreviations) {
		this.abbreviations = abbreviations;
	}

	public Double getAltitudeMin() {
		return this.altitudeMin;
	}

	public void setAltitudeMin(final Double altitudeMin) {
		this.altitudeMin = altitudeMin;
	}

	public Double getAltitudeMax() {
		return this.altitudeMax;
	}

	public void setAltitudeMax(final Double altitudeMax) {
		this.altitudeMax = altitudeMax;
	}

	public List<String> getLocationNames() {
		return this.locationNames;
	}

	public void setLocationNames(final List<String> locationNames) {
		this.locationNames = locationNames;
	}

	public List<String> getLocationTypes() {
		return this.locationTypes;
	}

	public void setLocationTypes(final List<String> locationTypes) {
		this.locationTypes = locationTypes;
	}

	public Coordinate getCoordinates() {
		return this.coordinates;
	}

	public void setCoordinates(final Coordinate coordinates) {
		this.coordinates = coordinates;
	}

	public List<String> getCountryCodes() {
		return this.countryCodes;
	}

	public void setCountryCodes(final List<String> countryCodes) {
		this.countryCodes = countryCodes;
	}

	public List<String> getCountryNames() {
		return this.countryNames;
	}

	public void setCountryNames(final List<String> countryNames) {
		this.countryNames = countryNames;
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
