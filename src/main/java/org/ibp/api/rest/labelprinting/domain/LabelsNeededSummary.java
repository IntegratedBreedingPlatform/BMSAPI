package org.ibp.api.rest.labelprinting.domain;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@AutoProperty
public class LabelsNeededSummary {

	@AutoProperty
	public static class Row {
		private String instanceNumber;
		private Long subObservationNumber;
		private Long labelsNeeded;
		private Long entries;
		private Long reps;

		public Row() {
		}

		public Row(final String instanceNumber, final Long subObservationNumber, final Long labelsNeeded) {
			this.instanceNumber = instanceNumber;
			this.subObservationNumber = subObservationNumber;
			this.labelsNeeded = labelsNeeded;
		}

		public Row(final String instanceNumber, final Long subObservationNumber, final Long labelsNeeded, final Long reps, final Long entries) {
			this.instanceNumber = instanceNumber;
			this.subObservationNumber = subObservationNumber;
			this.labelsNeeded = labelsNeeded;
			this.entries = entries;
			this.reps = reps;
		}

		public String getInstanceNumber() {
			return this.instanceNumber;
		}

		public void setInstanceNumber(final String instanceNumber) {
			this.instanceNumber = instanceNumber;
		}

		public Long getSubObservationNumber() {
			return this.subObservationNumber;
		}

		public void setSubObservationNumber(final Long subObservationNumber) {
			this.subObservationNumber = subObservationNumber;
		}

		public Long getLabelsNeeded() {
			return this.labelsNeeded;
		}

		public void setLabelsNeeded(final Long labelsNeeded) {
			this.labelsNeeded = labelsNeeded;
		}

		public Long getEntries() {
			return this.entries;
		}

		public Row setEntries(final Long entries) {
			this.entries = entries;
			return this;
		}

		public Long getReps() {
			return reps;
		}

		public Row setReps(final Long reps) {
			this.reps = reps;
			return this;
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

	private List<Row> rows = new LinkedList<>();

	private Long totalNumberOfLabelsNeeded;

	public List<Row> getRows() {
		return this.rows;
	}

	public void setRows(final List<Row> rows) {
		this.rows = rows;
	}

	public Long getTotalNumberOfLabelsNeeded() {
		return this.totalNumberOfLabelsNeeded;
	}

	public void setTotalNumberOfLabelsNeeded(final Long totalNumberOfLabelsNeeded) {
		this.totalNumberOfLabelsNeeded = totalNumberOfLabelsNeeded;
	}

	public void addRow (final Row row) {
		this.rows.add(row);
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
