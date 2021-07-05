
package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.base.Strings;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyMethodDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.ontology.ScaleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import java.util.Objects;

public abstract class OntologyValidator extends BaseValidator {

	@Autowired
	protected TermDataManager termDataManager;

	@Autowired
	protected OntologyScaleDataManager ontologyScaleDataManager;

	@Autowired
	protected OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	protected OntologyPropertyDataManager ontologyPropertyDataManager;

	@Autowired
	protected OntologyMethodDataManager ontologyMethodDataManager;

	@Autowired
	protected ScaleService scaleService;

	private static final Logger LOGGER = LoggerFactory.getLogger(OntologyValidator.class);

	protected void checkTermExist(String termName, String id, Integer cvId, Errors errors) {
		this.checkTermExist(termName, null, id, cvId, errors);
	}

	protected void checkTermExist(String termName, String fieldName, String id, Integer cvId, Errors errors) {
		try {
			Term term = this.termDataManager.getTermById(Integer.valueOf(id));
			if (Objects.equals(term, null) || !Objects.equals(term.getVocabularyId(), cvId)) {
				if (Strings.isNullOrEmpty(fieldName)) {
					this.addCustomError(errors, BaseValidator.ID_DOES_NOT_EXIST, new Object[] {termName, id});
				} else {
					this.addCustomError(errors, fieldName, BaseValidator.ID_DOES_NOT_EXIST, new Object[] {termName, id});
				}
			}
		} catch (MiddlewareException e) {
			OntologyValidator.LOGGER.error("Error while validating object", e);
			this.addDefaultError(errors);
		}
	}

	protected void checkTermUniqueness(String termName, Integer id, String name, Integer cvId, Errors errors) {

		try {
			Term term = this.termDataManager.getTermByNameAndCvId(name, cvId);
			if (term == null) {
				return;
			}

			if (Objects.equals(id, null) && Objects.equals(term, null)) {
				return;
			}

			if (id != null && Objects.equals(id, term.getId())) {
				return;
			}

			this.addCustomError(errors, "name", BaseValidator.NAME_ALREADY_EXIST, new Object[] {termName});
		} catch (MiddlewareException e) {
			OntologyValidator.LOGGER.error("Error checking uniqueness of term name", e);
		}
	}

	protected void checkTermUniqueness(final String fieldName, final String termName, final Integer id, final String name,
		final Integer cvId, final Errors errors) {

		try {
			Term term = this.termDataManager.getTermByNameAndCvId(name, cvId);
			if (term == null) {
				return;
			}

			if (Objects.equals(id, null) && Objects.equals(term, null)) {
				return;
			}

			if (id != null && Objects.equals(id, term.getId())) {
				return;
			}

			this.addCustomError(errors, fieldName.toLowerCase(), BaseValidator.NAME_OR_ALIAS_ALREADY_EXIST, new Object[] {fieldName, termName});
		} catch (MiddlewareException e) {
			OntologyValidator.LOGGER.error("Error checking uniqueness of term name", e);
		}
	}

	protected void fieldShouldNotOverflow(String fieldName, String value, Integer limit, Errors errors) {

		if (Strings.isNullOrEmpty(value)) {
			return;
		}

		if (value.length() > limit) {
			this.addCustomError(errors, fieldName, BaseValidator.TEXTUAL_FIELD_IS_TOO_LONG, new Object[] {limit});
		}
	}

	protected void listShouldNotOverflow(String termName, String fieldName, String value, Integer limit, Errors errors) {

		if (Strings.isNullOrEmpty(value)) {
			return;
		}

		if (value.length() > limit) {
			this.addCustomError(errors, fieldName, BaseValidator.LIST_TEXTUAL_FIELD_IS_TOO_LONG, new Object[] {termName, limit});
		}
	}

	public void setTermDataManager(TermDataManager termDataManager) {
		this.termDataManager = termDataManager;
	}

	public void setOntologyMethodDataManager(OntologyMethodDataManager ontologyMethodDataManager) {
		this.ontologyMethodDataManager = ontologyMethodDataManager;
	}

	public void setOntologyScaleDataManager(OntologyScaleDataManager ontologyScaleDataManager) {
		this.ontologyScaleDataManager = ontologyScaleDataManager;
	}

	public void setOntologyPropertyDataManager(OntologyPropertyDataManager ontologyPropertyDataManager) {
		this.ontologyPropertyDataManager = ontologyPropertyDataManager;
	}

	public void setOntologyVariableDataManager(OntologyVariableDataManager ontologyVariableDataManager) {
		this.ontologyVariableDataManager = ontologyVariableDataManager;
	}

	public void setScaleService (ScaleService scaleService) {
		this.scaleService = scaleService;
	}

}
