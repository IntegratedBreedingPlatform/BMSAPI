
package org.ibp.api.domain.ontology;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class DataType {

	private Integer id;
	private String name;

	public DataType() {
	}

	public DataType(Integer id, String name) {
		this.id = id;
		this.name = name;
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

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof DataType)) {
			return false;
		}
		DataType castOther = (DataType) other;
		return new EqualsBuilder().append(this.id, castOther.id).append(this.name, castOther.name).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.id).append(this.name).toHashCode();
	}

}
