package org.ibp.api.rest.inventory.study;

import org.generationcp.middleware.api.inventory.study.StudyTransactionsDto;

import java.util.List;

public class StudyInventoryTable {

	private Integer recordsFiltered;
	private Integer recordsTotal;
	private String draw;
	private List<StudyTransactionsDto> data;

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

	public List<StudyTransactionsDto> getData() {
		return this.data;
	}

	public void setData(final List<StudyTransactionsDto> data) {
		this.data = data;
	}
}
