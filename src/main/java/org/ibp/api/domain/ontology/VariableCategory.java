
package org.ibp.api.domain.ontology;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

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
		if (!(other instanceof VariableCategory))
			return false;
		VariableCategory castOther = (VariableCategory) other;
		return new EqualsBuilder().append(name, castOther.name).append(description, castOther.description).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(name).append(description).toHashCode();
	}

}
