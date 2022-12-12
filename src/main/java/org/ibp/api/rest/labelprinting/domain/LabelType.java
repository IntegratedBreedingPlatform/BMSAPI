package org.ibp.api.rest.labelprinting.domain;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
public class LabelType {

	private String title;

	private String key;

	private List<Field> fields;

	public LabelType(final String title, final String key) {
		this.title = title;
		this.key = key;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getKey() {
		return this.key;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public List<Field> getFields() {
		return this.fields;
	}

	public void setFields(final List<Field> fields) {
		this.fields = fields;
	}

	public LabelType withFields(final List<Field> fields) {
		this.fields = fields;
		return this;
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
