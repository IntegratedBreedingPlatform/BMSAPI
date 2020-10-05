package org.ibp.api.domain.study;

import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceDto;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
public class StudyEntryTable {

	private Integer recordsFiltered;

	private Integer recordsTotal;

	private List<StudyEntryDto> data;

	public StudyEntryTable() {

	}

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

	public List<StudyEntryDto> getData() {
		return data;
	}

	public void setData(final List<StudyEntryDto> data) {
		this.data = data;
	}
}
