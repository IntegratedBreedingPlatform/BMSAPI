package org.ibp.api.java.impl.middleware.study;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class StudyEntryMetadata {

	private Long testEntriesCount;

	private Long checkEntriesCount;

	private Long nonTestEntriesCount;

	private Boolean hasUnassignedStudyEntriesToPlot;

	public Long getTestEntriesCount() {
		return this.testEntriesCount;
	}

	public void setTestEntriesCount(final Long testEntriesCount) {
		this.testEntriesCount = testEntriesCount;
	}

	public Long getCheckEntriesCount() {
		return this.checkEntriesCount;
	}

	public void setCheckEntriesCount(final Long checksCount) {
		this.checkEntriesCount = checksCount;
	}

	public Long getNonTestEntriesCount() {
		return this.nonTestEntriesCount;
	}

	public void setNonTestEntriesCount(final Long nonTestEntriesCount) {
		this.nonTestEntriesCount = nonTestEntriesCount;
	}

	public Boolean getHasUnassignedStudyEntriesToPlot() {
		return this.hasUnassignedStudyEntriesToPlot;
	}

	public void setHasUnassignedStudyEntriesToPlot(final Boolean hasUnassignedStudyEntriesToPlot) {
		this.hasUnassignedStudyEntriesToPlot = hasUnassignedStudyEntriesToPlot;
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
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}
}
