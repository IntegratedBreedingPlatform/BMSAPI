package org.ibp.api.rest.labelprinting.domain;

import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class Sortable {

	private String fieldName;
	private String sortBy;

	public Sortable(){

	}

	public Sortable(final String fieldName, final String sortBy) {
		this.fieldName = fieldName;
		this.sortBy = sortBy;
	}

	public String getFieldName() {
		return this.fieldName;
	}

	public void setFieldName(final String fieldName) {
		this.fieldName = fieldName;
	}

	public String getSortBy() {
		return this.sortBy;
	}

	public void setSortBy(final String sortBy) {
		this.sortBy = sortBy;
	}
}
