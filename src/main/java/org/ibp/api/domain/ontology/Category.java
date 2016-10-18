package org.ibp.api.domain.ontology;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Category extends TermSummary {

	private boolean editable;

	public Category() {
		super();
		this.editable = Boolean.TRUE;
	}

	public Category (TermSummary termSummary) {
		this.setEditable(Boolean.TRUE);
		super.setId(termSummary.getId());
		super.setName(termSummary.getName());
		super.setDescription(termSummary.getDescription());
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(final boolean editable) {
		this.editable = editable;
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
