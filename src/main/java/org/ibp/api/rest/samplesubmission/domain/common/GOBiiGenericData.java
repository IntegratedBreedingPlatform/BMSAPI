package org.ibp.api.rest.samplesubmission.domain.common;

import java.util.Date;
import java.util.List;

/**
 * Created by clarysabel on 9/12/18.
 */
public abstract class GOBiiGenericData {

	private List<String> allowedProcessTypes;

	private Integer createdBy;

	private Date createdDate;

	private Integer modifiedBy;

	private Date modifiedDate;

	private Integer id;

	public List<String> getAllowedProcessTypes() {
		return allowedProcessTypes;
	}

	public void setAllowedProcessTypes(final List<String> allowedProcessTypes) {
		this.allowedProcessTypes = allowedProcessTypes;
	}

	public Integer getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(final Integer createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(final Date createdDate) {
		this.createdDate = createdDate;
	}

	public Integer getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(final Integer modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(final Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

}
