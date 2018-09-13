package org.ibp.api.rest.samplesubmission.domain.experiment;

import org.ibp.api.rest.samplesubmission.domain.common.GOBiiHeader;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

/**
 * Created by clarysabel on 9/13/18.
 */
@AutoProperty
public class GOBiiExperiment {

	private GOBiiExperimentPayload payload;

	private GOBiiHeader header;

	public GOBiiExperimentPayload getPayload() {
		return payload;
	}

	public void setPayload(final GOBiiExperimentPayload payload) {
		this.payload = payload;
	}

	public GOBiiHeader getHeader() {
		return header;
	}

	public void setHeader(final GOBiiHeader header) {
		this.header = header;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}

}
