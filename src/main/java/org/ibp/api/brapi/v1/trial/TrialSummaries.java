
package org.ibp.api.brapi.v1.trial;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Result;

public class TrialSummaries {

	private Metadata metadata;

	private Result<TrialSummary> result;

	public TrialSummaries() {
	}

	public Metadata getMetadata() {
		return this.metadata;
	}

	public void setMetadata(final Metadata metadata) {
		this.metadata = metadata;
	}

	public TrialSummaries withMetadata(final Metadata metadata) {
		this.metadata = metadata;
		return this;
	}

	public Result<TrialSummary> getResult() {
		return this.result;
	}

	public void setResult(final Result<TrialSummary> result) {
		this.result = result;
	}

	public TrialSummaries withResult(final Result<TrialSummary> result) {
		this.result = result;
		return this;
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
