package org.ibp.api.domain.ontology;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class Category extends TermSummary {

	private Boolean editable = Boolean.TRUE;

	public Category() {
	}

	public Category(final String id, final String name, final String description, final boolean editable) {
		super(id, name, description);
		this.editable = editable;
	}

	public Boolean getEditable() {
		return editable;
	}

	public Boolean isEditable() {
		return editable;
	}

	public Category setEditable(final Boolean editable) {
		this.editable = editable;
		return this;
	}

	@Override public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override public String toString() {
		return Pojomatic.toString(this);
	}

	@Override public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}

}
