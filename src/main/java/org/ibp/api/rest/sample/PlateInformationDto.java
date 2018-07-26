package org.ibp.api.rest.sample;

import java.util.ArrayList;
import java.util.List;

public class PlateInformationDto {

	private Integer listId;
	private String sampleIdHeader;
	private String plateIdHeader;
	private String wellHeader;
	private List<List<String>> importData = new ArrayList<>();

	public String getSampleIdHeader() {
		return sampleIdHeader;
	}

	public void setSampleIdHeader(final String sampleIdHeader) {
		this.sampleIdHeader = sampleIdHeader;
	}

	public String getPlateIdHeader() {
		return plateIdHeader;
	}

	public void setPlateIdHeader(final String plateIdHeader) {
		this.plateIdHeader = plateIdHeader;
	}

	public String getWellHeader() {
		return wellHeader;
	}

	public void setWellHeader(final String wellHeader) {
		this.wellHeader = wellHeader;
	}

	public List<List<String>> getImportData() {
		return importData;
	}

	public void setImportData(final List<List<String>> importData) {
		this.importData = importData;
	}

	public Integer getListId() {
		return listId;
	}

	public void setListId(final Integer listId) {
		this.listId = listId;
	}
}
