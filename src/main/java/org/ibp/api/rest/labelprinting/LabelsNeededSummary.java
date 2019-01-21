package org.ibp.api.rest.labelprinting;

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

		public String getInstanceNumber() {
			return instanceNumber;
		}

		public void setInstanceNumber(final String instanceNumber) {
			this.instanceNumber = instanceNumber;
		}

		public Long getSubObservationNumber() {
			return subObservationNumber;
		}

		public void setSubObservationNumber(final Long subObservationNumber) {
			this.subObservationNumber = subObservationNumber;
		}

		public Long getLabelsNeeded() {
			return labelsNeeded;
		}

		public void setLabelsNeeded(final Long labelsNeeded) {
			this.labelsNeeded = labelsNeeded;
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
		return rows;
	}

	public void setRows(final List<Row> rows) {
		this.rows = rows;
	}

	public Long getTotalNumberOfLabelsNeeded() {
		return totalNumberOfLabelsNeeded;
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
