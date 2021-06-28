package org.ibp.api.brapi.v2;

import org.pojomatic.Pojomatic;
import org.springframework.validation.ObjectError;

import java.util.List;

public abstract class BrapiImportResponse<T> {

	private Integer createdSize;
	private Integer importListSize;
	private List<ObjectError> errors;
	private List<T> entityList;

	public List<ObjectError> getErrors() {
		return this.errors;
	}

	public void setErrors(final List<ObjectError> errors) {
		this.errors = errors;
	}

	public List<T> getEntityList() {
		return this.entityList;
	}

	public void setEntityList(final List<T> entityList) {
		this.entityList = entityList;
	}

	public Integer getCreatedSize() {
		return this.createdSize;
	}

	public void setCreatedSize(final Integer createdSize) {
		this.createdSize = createdSize;
	}

	public Integer getImportListSize() {
		return this.importListSize;
	}

	public void setImportListSize(final Integer importListSize) {
		this.importListSize = importListSize;
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	public abstract String getEntity();

}
