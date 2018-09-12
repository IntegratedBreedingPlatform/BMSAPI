package org.ibp.api.rest.sample_submission.domain;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;


/**
 * Created by clarysabel on 9/12/18.
 */
@AutoProperty
public class GOBiiProject extends GOBiiGenericData {

	@AutoProperty
	class Property {

		private Integer entityIdId;
		private Integer propertyId;
		private String propertyName;
		private String propertyValue;

		public Integer getEntityIdId() {
			return entityIdId;
		}

		public void setEntityIdId(final Integer entityIdId) {
			this.entityIdId = entityIdId;
		}

		public Integer getPropertyId() {
			return propertyId;
		}

		public void setPropertyId(final Integer propertyId) {
			this.propertyId = propertyId;
		}

		public String getPropertyName() {
			return propertyName;
		}

		public void setPropertyName(final String propertyName) {
			this.propertyName = propertyName;
		}

		public String getPropertyValue() {
			return propertyValue;
		}

		public void setPropertyValue(final String propertyValue) {
			this.propertyValue = propertyValue;
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

	private String entityNameType;

	private Integer projectId;

	private String projectName;

	private String projectCode;

	private String projectDescription;

	private Integer piContact;

	private Integer projectStatus;

	private List principleInvestigators;

	private List<Property> properties;

	public String getEntityNameType() {
		return entityNameType;
	}

	public void setEntityNameType(final String entityNameType) {
		this.entityNameType = entityNameType;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(final Integer projectId) {
		this.projectId = projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(final String projectName) {
		this.projectName = projectName;
	}

	public String getProjectCode() {
		return projectCode;
	}

	public void setProjectCode(final String projectCode) {
		this.projectCode = projectCode;
	}

	public String getProjectDescription() {
		return projectDescription;
	}

	public void setProjectDescription(final String projectDescription) {
		this.projectDescription = projectDescription;
	}

	public Integer getPiContact() {
		return piContact;
	}

	public void setPiContact(final Integer piContact) {
		this.piContact = piContact;
	}

	public Integer getProjectStatus() {
		return projectStatus;
	}

	public void setProjectStatus(final Integer projectStatus) {
		this.projectStatus = projectStatus;
	}

	public List getPrincipleInvestigators() {
		return principleInvestigators;
	}

	public void setPrincipleInvestigators(final List principleInvestigators) {
		this.principleInvestigators = principleInvestigators;
	}

	public List<Property> getProperties() {
		return properties;
	}

	public void setProperties(final List<Property> properties) {
		this.properties = properties;
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
