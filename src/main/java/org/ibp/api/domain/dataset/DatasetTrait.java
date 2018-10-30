package org.ibp.api.domain.dataset;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

public class DatasetTrait {
	
	@NotNull
	private Integer traitId;
	
	@NotBlank
	private String studyAlias;
	
	public DatasetTrait(Integer traitId, String studyAlias) {
		super();
		this.traitId = traitId;
		this.studyAlias = studyAlias;
	}

	public Integer getTraitId() {
		return traitId;
	}
	
	public void setTraitId(Integer traitId) {
		this.traitId = traitId;
	}
	
	public String getStudyAlias() {
		return studyAlias;
	}
	
	public void setStudyAlias(String studyAlias) {
		this.studyAlias = studyAlias;
	}

}
