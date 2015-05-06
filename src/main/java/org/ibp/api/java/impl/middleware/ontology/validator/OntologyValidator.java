package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.base.Strings;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.*;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import java.util.Objects;

public abstract class OntologyValidator extends BaseValidator {

	@Autowired
	protected OntologyBasicDataManager ontologyBasicDataManager;

	@Autowired
	protected OntologyScaleDataManager ontologyScaleDataManager;

	@Autowired
	protected OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	protected OntologyPropertyDataManager ontologyPropertyDataManager;

	@Autowired
	protected OntologyMethodDataManager ontologyMethodDataManager;

	protected void checkTermExist(String termName, String id, Integer cvId, Errors errors) {
		this.checkTermExist(termName, null, id, cvId, errors);
	}

	protected void checkTermExist(String termName, String fieldName, String id, Integer cvId, Errors errors) {
		try {
			Term term = this.ontologyBasicDataManager.getTermById(Integer.valueOf(id));
			if (Objects.equals(term, null) || !Objects.equals(term.getVocabularyId(), cvId)) {
				if (Strings.isNullOrEmpty(fieldName)) {
					this.addCustomError(errors, ID_DOES_NOT_EXIST, new Object[] { termName, id });
				} else {
					this.addCustomError(errors, fieldName, ID_DOES_NOT_EXIST, new Object[] { termName, id });
				}
			}
		} catch (MiddlewareException e) {
			this.log.error("Error while validating object", e);
			this.addDefaultError(errors);
		}
	}

	protected void checkTermUniqueness(String termName, Integer id, String name, Integer cvId, Errors errors) {

		try {
			Term term = this.ontologyBasicDataManager.getTermByNameAndCvId(name, cvId);
			if (term == null) {
				return;
			}

			if (Objects.equals(id, null) && Objects.equals(term, null)) {
				return;
			}

			if (id != null && Objects.equals(id, term.getId())) {
				return;
			}

			this.addCustomError(errors, "name", NAME_ALREADY_EXIST, new Object[]{termName});
		} catch (MiddlewareException e) {
			this.log.error("Error checking uniqueness of term name", e);
		}
	}



	protected void fieldShouldNotOverflow(String fieldName, String value, Integer limit, Errors errors) {

		if(Strings.isNullOrEmpty(value)) {
			return;
		}

		if (value.length() > limit) {
			this.addCustomError(errors, fieldName, TEXTUAL_FIELD_IS_TOO_LONG, new Object[] {limit});
		}
	}

	protected void listShouldNotOverflow(String termName, String fieldName, String value, Integer limit, Errors errors) {

		if(Strings.isNullOrEmpty(value)) {
			return;
		}

		if (value.length() > limit) {
			this.addCustomError(errors, fieldName, OntologyValidator.LIST_TEXTUAL_FIELD_IS_TOO_LONG, new Object[] {termName, limit});
		}
	}

	public void setOntologyBasicDataManager(OntologyBasicDataManager ontologyBasicDataManager) {
		this.ontologyBasicDataManager = ontologyBasicDataManager;
	}

	public void setOntologyMethodDataManager(OntologyMethodDataManager ontologyMethodDataManager) {
		this.ontologyMethodDataManager = ontologyMethodDataManager;
	}

	public void setOntologyScaleDataManager(OntologyScaleDataManager ontologyScaleDataManager){
		this.ontologyScaleDataManager = ontologyScaleDataManager;
	}

	public void setOntologyPropertyDataManager(OntologyPropertyDataManager ontologyPropertyDataManager){
		this.ontologyPropertyDataManager = ontologyPropertyDataManager;
	}

	public void setOntologyVariableDataManager(OntologyVariableDataManager ontologyVariableDataManager){
		this.ontologyVariableDataManager = ontologyVariableDataManager;
	}
}
