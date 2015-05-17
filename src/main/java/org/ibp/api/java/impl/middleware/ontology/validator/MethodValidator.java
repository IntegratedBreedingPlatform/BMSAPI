package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.base.Strings;

import java.util.Objects;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.ibp.api.domain.ontology.MethodSummary;
import org.ibp.api.java.impl.middleware.common.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * Method add/update validator
 * 1 Name is required
 * 2 Name is unique
 * 3 Name cannot change if the method is already in use
 * 4 Name is no more than 200 characters
 * 5 Description is no more than 255 characters
 * 6 Name and description are textual
 */
@Component
public class MethodValidator extends OntologyValidator implements org.springframework.validation.Validator {

	private static final Integer NAME_TEXT_LIMIT = 200;
	private static final Integer DESCRIPTION_TEXT_LIMIT = 255;

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodValidator.class);
	
	@Override
	public boolean supports(Class<?> aClass) {
		return MethodSummary.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {

		MethodSummary method = (MethodSummary) target;

		boolean nameValidationResult = nameValidationProcessor(method, errors);

		descriptionValidationProcessor(method, errors);

		if(nameValidationResult) {
			methodShouldBeEditable(method, errors);
		}
	}

	private void methodShouldBeEditable(MethodSummary method, Errors errors) {

		if (method.getId() == null) {
			return;
		}

		try {

			Method existingMethod = this.ontologyMethodDataManager.getMethod(CommonUtil.tryParseSafe(method.getId()));

			if (existingMethod == null) {
				this.addCustomError(errors, ID_DOES_NOT_EXIST, new Object[] { "Method", method.getId()});
				return;
			}

			boolean isEditable = !this.termDataManager.isTermReferred(CommonUtil.tryParseSafe(method.getId()));
			if (isEditable) {
				return;
			}

			if (Objects.equals(method.getName(), existingMethod.getName())) {
				return;
			}

		} catch (MiddlewareException e) {
			Throwable rootCause = getRootCause(e);
			LOGGER.error(String.format("Error in %s.%s", rootCause.getStackTrace()[0].getClassName(), rootCause.getStackTrace()[0].getMethodName()), e);
			this.addDefaultError(errors);
			return;
		}

		this.addCustomError(errors, "name", RECORD_IS_NOT_EDITABLE, new Object[] { "method", "Name" });
	}

	private boolean nameValidationProcessor(MethodSummary method, Errors errors){

		Integer initialCount = errors.getErrorCount();

		// 1. Name is required
		this.shouldNotNullOrEmpty("Name", "name", method.getName(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		//Trim name
		method.setName(method.getName().trim());

		// 4. Name is no more than 200 characters
		this.fieldShouldNotOverflow("name", method.getName(), NAME_TEXT_LIMIT, errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 2. Name is unique
		this.checkTermUniqueness("Method", CommonUtil.tryParseSafe(method.getId()), method.getName(), CvId.METHODS.getId(), errors);

		return errors.getErrorCount() == initialCount;
	}

	// 5. Description is optional, when provided no more than 255 characters in length.
	private boolean descriptionValidationProcessor(MethodSummary method, Errors errors){

		Integer initialCount = errors.getErrorCount();

		if(Strings.isNullOrEmpty(method.getDescription())) {
			method.setDescription("");
		} else {
			method.setDescription(method.getDescription().trim());
		}

		this.fieldShouldNotOverflow("description", method.getDescription(), DESCRIPTION_TEXT_LIMIT, errors);

		return errors.getErrorCount() == initialCount;
	}
}
