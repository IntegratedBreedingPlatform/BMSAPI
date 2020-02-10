package org.ibp.api.brapi.v2.location;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"coordinates", "type"})
public class Geometry {

	private List<Integer> coordinates;

	private String type;

	public Geometry() {

	}

	private Geometry(final List<Integer> coordinates, final String type) {
		this.coordinates = coordinates;
		this.type = type;
	}

	public List<Integer> getCoordinates() {
		return coordinates;
	}

	public Geometry setCoordinates(final List<Integer> coordinates) {
		this.coordinates = coordinates;
		return this;
	}

	public String getType() {
		return type;
	}

	public Geometry setType(final String type) {
		this.type = type;
		return this;
	}


}
