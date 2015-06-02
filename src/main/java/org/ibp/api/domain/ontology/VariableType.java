
package org.ibp.api.domain.ontology;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

public class VariableType {

	private Integer id;
	private String name;
	private String description;

	public VariableType() {
	}

	public VariableType(Integer id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
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
		if (!(other instanceof VariableType))
			return false;
		VariableType castOther = (VariableType) other;
		return new EqualsBuilder().append(id, castOther.id).append(name, castOther.name).append(description, castOther.description)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).append(name).append(description).toHashCode();
	}

}
