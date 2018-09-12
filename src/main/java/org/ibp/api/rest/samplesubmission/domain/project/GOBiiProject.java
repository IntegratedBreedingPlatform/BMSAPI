package org.ibp.api.rest.samplesubmission.domain.project;

import org.ibp.api.rest.samplesubmission.domain.common.GOBiiHeader;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;


/**
 * Created by clarysabel on 9/12/18.
 */
@AutoProperty
public class GOBiiProject {

	private GOBiiProjectPayload payload;
	private GOBiiHeader header;

	public GOBiiProjectPayload getPayload() {
		return payload;
	}

	public void setPayload(final GOBiiProjectPayload payload) {
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
