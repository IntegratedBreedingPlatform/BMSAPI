package org.ibp.api.rest.study;

import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
public class StudyEntryTable {

	private Integer recordsFiltered;

	private Integer recordsTotal;

	private String draw;

	private List<StudyEntryDto> data;

	public Integer getRecordsFiltered() {
		return recordsFiltered;
	}

	public void setRecordsFiltered(final Integer recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public Integer getRecordsTotal() {
		return recordsTotal;
	}

	public void setRecordsTotal(final Integer recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	public String getDraw() {
		return draw;
	}

	public void setDraw(final String draw) {
		this.draw = draw;
	}

	public List<StudyEntryDto> getData() {
		return data;
	}

	public void setData(final List<StudyEntryDto> data) {
		this.data = data;
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
