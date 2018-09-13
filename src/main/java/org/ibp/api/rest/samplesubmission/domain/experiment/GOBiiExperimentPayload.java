package org.ibp.api.rest.samplesubmission.domain.experiment;

import java.util.ArrayList;
import java.util.List;
import org.ibp.api.rest.samplesubmission.domain.common.GOBiiGenericData;
import org.ibp.api.rest.samplesubmission.domain.common.GOBiiGenericPayload;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

/**
 * Created by clarysabel on 9/13/18.
 */
@AutoProperty
public class GOBiiExperimentPayload extends GOBiiGenericPayload {


	@AutoProperty
	public static class ExperimentData extends GOBiiGenericData {

		private String entityNameType;

		private Integer experimentId;

		private String experimentName;

		private String experimentCode;

		private String experimentDataFile;

		private Integer vendorProtocolId;

		private Integer manifestId;

		private Integer statusId;

		private Integer projectId;

		//missing dataset type
		private List dataset = new ArrayList();

		public ExperimentData() {
			entityNameType = "EXPERIMENT";
		}

		public String getEntityNameType() {
			return entityNameType;
		}

		public void setEntityNameType(final String entityNameType) {
			this.entityNameType = entityNameType;
		}

		public Integer getExperimentId() {
			return experimentId;
		}

		public void setExperimentId(final Integer experimentId) {
			this.experimentId = experimentId;
		}

		public String getExperimentName() {
			return experimentName;
		}

		public void setExperimentName(final String experimentName) {
			this.experimentName = experimentName;
		}

		public String getExperimentCode() {
			return experimentCode;
		}

		public void setExperimentCode(final String experimentCode) {
			this.experimentCode = experimentCode;
		}

		public String getExperimentDataFile() {
			return experimentDataFile;
		}

		public void setExperimentDataFile(final String experimentDataFile) {
			this.experimentDataFile = experimentDataFile;
		}

		public Integer getVendorProtocolId() {
			return vendorProtocolId;
		}

		public void setVendorProtocolId(final Integer vendorProtocolId) {
			this.vendorProtocolId = vendorProtocolId;
		}

		public Integer getManifestId() {
			return manifestId;
		}

		public void setManifestId(final Integer manifestId) {
			this.manifestId = manifestId;
		}

		public Integer getStatusId() {
			return statusId;
		}

		public void setStatusId(final Integer statusId) {
			this.statusId = statusId;
		}

		public List getDataset() {
			return dataset;
		}

		public void setDataset(final List dataset) {
			this.dataset = dataset;
		}

		public Integer getProjectId() {
			return projectId;
		}

		public void setProjectId(final Integer projectId) {
			this.projectId = projectId;
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
		public boolean equals(Object o) {
			return Pojomatic.equals(this, o);
		}

	}

	private List<ExperimentData> data;

	public List<ExperimentData> getData() {
		return data;
	}

	public void setData(final List<ExperimentData> data) {
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
	public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}

}
