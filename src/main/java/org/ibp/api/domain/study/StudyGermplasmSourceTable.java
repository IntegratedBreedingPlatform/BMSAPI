package org.ibp.api.domain.study;

import org.generationcp.middleware.service.api.study.germplasm.source.StudyGermplasmSourceDto;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
public class StudyGermplasmSourceTable {

	private Integer recordsFiltered;

	private Integer recordsTotal;

	private List<StudyGermplasmSourceDto> data;

	public StudyGermplasmSourceTable() {

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

	public List<StudyGermplasmSourceDto> getData() {
		return this.data;
	}

	public void setData(final List<StudyGermplasmSourceDto> data) {
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
