package org.generationcp.bms.domain;

public class Trait {

	private final int id;
	private String name;
	private String description;
	private String property;
	private String method;
	private String scale;
	private String type;
	
	private boolean numeric;
	
	private long numberOfMeasurements;
	
	private String observationDetailsUrl;

	public Trait(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getNumberOfMeasurements() {
		return numberOfMeasurements;
	}

	public void setNumberOfMeasurements(long numberOfMeasurements) {
		this.numberOfMeasurements = numberOfMeasurements;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getScale() {
		return scale;
	}

	public void setScale(String scale) {
		this.scale = scale;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isNumeric() {
		return numeric;
	}

	public void setNumeric(boolean numeric) {
		this.numeric = numeric;
	}

	public String getObservationDetailsUrl() {
		return observationDetailsUrl;
	}

	public void setObservationDetailsUrl(String observationDetailsUrl) {
		this.observationDetailsUrl = observationDetailsUrl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Trait other = (Trait) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Trait [id=" + id + ", name=" + name + ", numberOfMeasurements="
				+ numberOfMeasurements + "]";
	}
}
