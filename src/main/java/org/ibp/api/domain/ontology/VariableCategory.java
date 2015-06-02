
package org.ibp.api.domain.ontology;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class VariableCategory {

	private String name;
	private String description;

	public VariableCategory() {
	}

	public VariableCategory(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof VariableCategory)) {
			return false;
		}
		VariableCategory castOther = (VariableCategory) other;
		return new EqualsBuilder().append(this.name, castOther.name).append(this.description, castOther.description).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.name).append(this.description).toHashCode();
	}

}
