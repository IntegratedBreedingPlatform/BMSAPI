package org.ibp.api.brapi.v2.germplasm;

import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.pojomatic.annotations.AutoProperty;
import org.springframework.validation.ObjectError;

import java.util.List;

@AutoProperty
public class GermplasmImportResponse {

	private String status;
	private List<ObjectError> errors;
	private List<GermplasmDTO> germplasmList;

	public GermplasmImportResponse() {

	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public List<ObjectError> getErrors() {
		return errors;
	}

	public void setErrors(final List<ObjectError> errors) {
		this.errors = errors;
	}

	public List<GermplasmDTO> getGermplasmList() {
		return germplasmList;
	}

	public void setGermplasmList(final List<GermplasmDTO> germplasmList) {
		this.germplasmList = germplasmList;
	}
}
