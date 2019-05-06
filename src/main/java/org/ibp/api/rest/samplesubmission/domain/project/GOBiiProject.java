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

	private Integer id;

	private Integer piContactId;

	private String name;

	private Integer status;

	private String code;

	private String description;

	private Map<String, String> properties;

	public Integer getPiContactId() {
		return piContactId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public void setPiContactId(final Integer piContactId) {
		this.piContactId = piContactId;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(final Integer status) {
		this.status = status;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
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
