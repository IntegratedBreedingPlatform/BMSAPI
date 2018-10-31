package org.ibp.api.rest.dataset;

import java.util.List;
import java.util.Objects;

public class ObservationUnitTable {

	private Integer recordsFiltered;

	private Integer recordsTotal;

	private String draw;

	private List<ObservationUnitRow> data;

	public ObservationUnitTable() {

	}

	public Integer getRecordsFiltered() {
		return this.recordsFiltered;
	}

	public void setRecordsFiltered(final Integer recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public Integer getRecordsTotal() {
		return this.recordsTotal;
	}

	public void setRecordsTotal(final Integer recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	public String getDraw() {
		return this.draw;
	}

	public void setDraw(final String draw) {
		this.draw = draw;
	}

	public List<ObservationUnitRow> getData() {
		return this.data;
	}

	public void setData(final List<ObservationUnitRow> data) {
		this.data = data;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (o == null || this.getClass() != o.getClass())
			return false;
		final ObservationUnitTable that = (ObservationUnitTable) o;
		return Objects.equals(this.recordsFiltered, that.recordsFiltered) &&
			Objects.equals(this.recordsTotal, that.recordsTotal) &&
			Objects.equals(this.draw, that.draw) &&
			Objects.equals(this.data, that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.recordsFiltered, this.recordsTotal, this.draw, this.data);
	}

	@Override
	public String toString() {
		return "ObservationUnitTable{" +
			"recordsFiltered=" + this.recordsFiltered +
			", recordsTotal=" + this.recordsTotal +
			", draw='" + this.draw + '\'' +
			", data=" + this.data +
			'}';
	}
}