package org.generationcp.bms.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class DatasetSummary {

	private Integer id;
	private String name;
	private String description;
	private String datasetDetailUrl;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDatasetDetailUrl() {
		return datasetDetailUrl;
	}

	public void setDatasetDetailUrl(String datasetDetailUrl) {
		this.datasetDetailUrl = datasetDetailUrl;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		DatasetSummary rhs = (DatasetSummary) obj;
		return new EqualsBuilder().append(this.getId(), rhs.getId()).isEquals();
	}

}
