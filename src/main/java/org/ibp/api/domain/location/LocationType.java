
package org.ibp.api.domain.location;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class LocationType {

	private String id;
	private String name;
	private String description;

	public LocationType() {

	}

	public LocationType(String id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

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
	public boolean equals(final Object other) {
		if (!(other instanceof LocationType)) {
			return false;
		}
		LocationType castOther = (LocationType) other;
		return new EqualsBuilder().append(this.id, castOther.id).append(this.name, castOther.name).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.id).append(this.name).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", this.id).append("name", this.name).append("description", this.description).toString();
	}

}
