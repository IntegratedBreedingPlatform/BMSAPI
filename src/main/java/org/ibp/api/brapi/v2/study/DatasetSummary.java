package org.ibp.api.brapi.v2.study;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatasetSummary {

	private Integer datasetId;

	private String datasetName;

	private Integer datasetTypeId;

	public DatasetSummary(final Integer datasetId, final String datasetName, final  Integer datasetTypeId){
		this.datasetId = datasetId;
		this.datasetName = datasetName;
		this.datasetTypeId = datasetTypeId;
	}

	public Integer getDatasetId() {
		return datasetId;
	}

	public void setDatasetId(final Integer datasetId) {
		this.datasetId = datasetId;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(final String datasetName) {
		this.datasetName = datasetName;
	}

	public Integer getDatasetTypeId() {
		return datasetTypeId;
	}

	public void setDatasetTypeId(final Integer datasetType) {
		this.datasetTypeId = datasetType;
	}

}
