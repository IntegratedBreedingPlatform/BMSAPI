
package org.ibp.api.domain.ontology;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

public class TermSummary {

	private String id;

	private String name;

	private String description;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "TermSummary{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", description='" + description + '\'' + '}';
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof TermSummary))
			return false;
		TermSummary castOther = (TermSummary) other;
		return new EqualsBuilder().append(id, castOther.id).append(name, castOther.name).append(description, castOther.description)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).append(name).append(description).toHashCode();
	}

}
