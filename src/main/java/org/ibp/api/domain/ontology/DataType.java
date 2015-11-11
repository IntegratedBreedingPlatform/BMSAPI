
package org.ibp.api.domain.ontology;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class DataType {

	private String id;
	private String name;
	private boolean systemDataType;

	public DataType() {
	}

	public DataType(String id, String name, boolean systemDataType) {
		this.id = id;
		this.name = name;
		this.systemDataType = systemDataType;
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

	public boolean isSystemDataType() {
		return systemDataType;
	}

	public void setSystemDataType(boolean systemDataType) {
		this.systemDataType = systemDataType;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof DataType)) {
			return false;
		}
		DataType castOther = (DataType) other;
		return new EqualsBuilder().append(this.id, castOther.id).append(this.name, castOther.name).append(this.systemDataType, castOther.systemDataType).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.id).append(this.name).append(this.systemDataType).toHashCode();
	}

}
