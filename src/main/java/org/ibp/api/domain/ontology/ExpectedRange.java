
package org.ibp.api.domain.ontology;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

public class ExpectedRange {

	private String min;
	private String max;

	public String getMin() {
		return this.min;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public String getMax() {
		return this.max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof ExpectedRange))
			return false;
		ExpectedRange castOther = (ExpectedRange) other;
		return new EqualsBuilder().append(min, castOther.min).append(max, castOther.max).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(min).append(max).toHashCode();
	}

}
