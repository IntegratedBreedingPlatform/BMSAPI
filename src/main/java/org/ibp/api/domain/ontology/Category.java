package org.ibp.api.domain.ontology;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


public class Category extends TermSummary {

	private Boolean isEditable;

	public Category() {
		super();
		this.isEditable = Boolean.TRUE;
	}

	public Category(
	final String id, final String name, final String description, final boolean isEditable) {
		super(id, name, description);
		this.isEditable = isEditable;
	}

	public boolean isEditable() {
		return isEditable;
	}

	public void setEditable(final Boolean isEditable) {
		this.isEditable = isEditable;
	}

	@Override public String toString() {
		return "TermSummary{" + "id='" + super.getId() + '\'' + ", name='" + super.getName() + '\'' + ", description='" + super
				.getDescription() + '\'' + ", editable='" + this.isEditable() + '\'' + '}';
	}

	@Override public boolean equals(final Object other) {
		if (!(other instanceof Category)) {
			return false;
		}
		Category castOther = (Category) other;
		return new EqualsBuilder().append(super.getId(), castOther.getId()).append(super.getName(), castOther.getName())
				.append(super.getDescription(), castOther.getDescription()).append(this.isEditable(), castOther.isEditable()).isEquals();
	}

	@Override public int hashCode() {
		return new HashCodeBuilder().append(super.getId()).append(super.getName()).append(super.getDescription()).append(this.isEditable())
				.toHashCode();
	}

}
