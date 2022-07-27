package org.ibp.api.domain.study;

import org.generationcp.middleware.service.api.dataset.StockPropertyData;

import java.util.List;

public class StudyEntryDetailsValueMap {

	private String entryNumber;

	private List<StockPropertyData> data;

	public StudyEntryDetailsValueMap() {
		super();
	}

	public StudyEntryDetailsValueMap(final String entryNumber, final List<StockPropertyData> data) {
		super();
		this.entryNumber = entryNumber;
		this.data = data;
	}

	public String getEntryNumber() {
		return this.entryNumber;
	}

	public void setEntryNumber(final String entryNumber) {
		this.entryNumber = entryNumber;
	}

	public List<StockPropertyData> getData() {
		return this.data;
	}

	public void setData(final List<StockPropertyData> data) {
		this.data = data;
	}
}
