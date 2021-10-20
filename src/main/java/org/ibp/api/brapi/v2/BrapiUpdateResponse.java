package org.ibp.api.brapi.v2;

import org.pojomatic.Pojomatic;
import org.springframework.validation.ObjectError;

import java.util.List;

public abstract class BrapiUpdateResponse<T> {

	private List<ObjectError> errors;

	private T entityObject;

	public List<ObjectError> getErrors() {
		return this.errors;
	}

	public void setErrors(final List<ObjectError> errors) {
		this.errors = errors;
	}

	public T getEntityObject() {
		return this.entityObject;
	}

	public void setEntityObject(final T entityObject) {
		this.entityObject = entityObject;
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

	public abstract String getEntityName();

}
