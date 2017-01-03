package org.ibp.api.brapi.v1.study;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.ibp.api.brapi.v1.common.Metadata;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty @JsonInclude(JsonInclude.Include.NON_NULL) @JsonPropertyOrder({"metadata", "result"}) public class StudyDetails {

	private Metadata metadata = new Metadata();

	private StudyDetailsData result = new StudyDetailsData();

	/**
	 * Empty constructor
	 */
	public StudyDetails() {
	}

	/**
	 * Full constructor
	 *
	 * @param metadata
	 * @param result
	 */
	public StudyDetails(final Metadata metadata, final StudyDetailsData result) {
		this.metadata = metadata;
		this.result = result;
	}

	/**
	 * @return Metadata
	 */
	public Metadata getMetadata() {
		return metadata;
	}

	/**
	 * @param metadata
	 * @return the study details
	 */
	public StudyDetails setMetadata(final Metadata metadata) {
		this.metadata = metadata;
		return this;
	}

	/**
	 * @return the results
	 */
	public StudyDetailsData getResult() {
		return result;
	}

	/**
	 * @param result
	 * @return StudyDetails
	 */
	public StudyDetails setResult(final StudyDetailsData result) {
		this.result = result;
		return this;
	}

	@Override public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override public String toString() {
		return Pojomatic.toString(this);
	}

	@Override public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}

}
