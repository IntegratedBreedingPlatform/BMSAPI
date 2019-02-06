package org.ibp.api.rest.dataset;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.domain.study.StudyInstance;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.io.Serializable;
import java.util.List;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"datasetId", "name", "description", "datasetTypeId", "studyId", "parentDatasetId", "cropName", "variables",
	"instances"})
public class DatasetDTO implements Serializable {

	private Integer datasetId;

	private Integer datasetTypeId;

	private String name;

	private Integer parentDatasetId;

	private String description;

	private Integer studyId;

	private String cropName;

	private List<StudyInstance> instances;

	private List<MeasurementVariable> variables;

	private Boolean hasPendingData;


	public Integer getDatasetId() {
		return this.datasetId;
	}

	public void setDatasetId(final Integer datasetId) {
		this.datasetId = datasetId;
	}

	public Integer getDatasetTypeId() {
		return this.datasetTypeId;
	}

	public void setDatasetTypeId(final Integer datasetTypeId) {
		this.datasetTypeId = datasetTypeId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Integer getStudyId() {
		return this.studyId;
	}

	public void setStudyId(final Integer studyId) {
		this.studyId = studyId;
	}

	public String getCropName() {
		return this.cropName;
	}

	public void setCropName(final String cropName) {
		this.cropName = cropName;
	}

	public List<StudyInstance> getInstances() {
		return this.instances;
	}

	public void setInstances(final List<StudyInstance> instances) {
		this.instances = instances;
	}

	public List<MeasurementVariable> getVariables() {
		return this.variables;
	}

	public void setVariables(final List<MeasurementVariable> variables) {
		this.variables = variables;
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
