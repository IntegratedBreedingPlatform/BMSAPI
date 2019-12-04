package org.ibp.api.rest.samplesubmission.domain.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.Map;

/**
 * Created by clarysabel on 9/12/18.
 */
@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GOBiiProject {

	private Integer projectId;

	private Integer piContactId;

	private String projectName;

	private Integer status;

	private String projectDescription;

	private Map<String, String> properties;

	private String code;

	public Integer getPiContactId() {
		return piContactId;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(final Integer projectId) {
		this.projectId = projectId;
	}

	public void setPiContactId(final Integer piContactId) {
		this.piContactId = piContactId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(final Integer status) {
		this.status = status;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(final String projectName) {
		this.projectName = projectName;
	}

	public String getProjectDescription() {
		return projectDescription;
	}

	public void setProjectDescription(final String projectDescription) {
		this.projectDescription = projectDescription;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(final Map<String, String> properties) {
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
