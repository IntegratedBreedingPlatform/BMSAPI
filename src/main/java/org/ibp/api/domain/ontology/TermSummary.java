
package org.ibp.api.domain.ontology;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class TermSummary {

	private String id;

	private String name;

	private String description;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
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
	public String toString() {
		return "TermSummary{" + "id='" + this.id + '\'' + ", name='" + this.name + '\'' + ", description='" + this.description + '\'' + '}';
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof TermSummary)) {
			return false;
		}
		TermSummary castOther = (TermSummary) other;
		return new EqualsBuilder().append(this.id, castOther.id).append(this.name, castOther.name)
				.append(this.description, castOther.description).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.id).append(this.name).append(this.description).toHashCode();
	}

}
