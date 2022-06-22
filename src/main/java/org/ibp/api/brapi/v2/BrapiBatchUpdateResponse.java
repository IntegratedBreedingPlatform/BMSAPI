package org.ibp.api.brapi.v2;

import org.pojomatic.Pojomatic;
import org.springframework.validation.ObjectError;

import java.util.List;

public abstract class BrapiBatchUpdateResponse<T> {

	private Integer updatedSize;
	private Integer updateListSize;
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

	public Integer getUpdatedSize() {
		return this.updatedSize;
	}

	public void setUpdatedSize(final Integer updatedSize) {
		this.updatedSize = updatedSize;
	}

	public Integer getUpdateListSize() {
		return this.updateListSize;
	}

	public void setUpdateListSize(final Integer updateListSize) {
		this.updateListSize = updateListSize;
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
