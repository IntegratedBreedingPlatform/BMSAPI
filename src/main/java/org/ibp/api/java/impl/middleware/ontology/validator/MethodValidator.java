
package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.Objects;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.ontology.MethodDetails;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.google.common.base.Strings;

/**
 * Method add/update validator 1 Name is required 2 Name is unique 3 Name cannot change if the method is already in use 4 Name is no more
 * than 200 characters 5 Description is no more than 1024 characters 6 Name and description are textual
 */
@Component
public class MethodValidator extends OntologyValidator implements org.springframework.validation.Validator {

	private static final Integer NAME_TEXT_LIMIT = 200;
	private static final Integer DESCRIPTION_TEXT_LIMIT = 1024;

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodValidator.class);

	@Override
	public boolean supports(Class<?> aClass) {
		return MethodDetails.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {

		MethodDetails method = (MethodDetails) target;

		boolean nameValidationResult = this.nameValidationProcessor(method, errors);

		this.descriptionValidationProcessor(method, errors);

		if (nameValidationResult) {
			this.methodShouldBeEditable(method, errors);
		}
	}

	private void methodShouldBeEditable(MethodDetails method, Errors errors) {

		if (method.getId() == null) {
			return;
		}

		try {

			Method existingMethod = this.ontologyMethodDataManager.getMethod(StringUtil.parseInt(method.getId(), null), true);

			if (existingMethod == null) {
				this.addCustomError(errors, BaseValidator.ID_DOES_NOT_EXIST, new Object[] {"Method", method.getId()});
				return;
			}

			// should not be a System method
			if(existingMethod.toCVTerm().getIsSystem()){
				this.addCustomError(errors, VariableValidator.METHOD_NOT_DELETABLE_AND_EDITABLE, new Object[] {method.getId()});
				return;
			}

			boolean isEditable = !this.termDataManager.isTermReferred(StringUtil.parseInt(method.getId(), null));
			if (isEditable) {
				return;
			}

			if (Objects.equals(method.getName(), existingMethod.getName())) {
				return;
			}

		} catch (MiddlewareException e) {
			Throwable rootCause = this.getRootCause(e);
			MethodValidator.LOGGER.error(
					String.format("Error in %s.%s", rootCause.getStackTrace()[0].getClassName(),
							rootCause.getStackTrace()[0].getMethodName()), e);
			this.addDefaultError(errors);
			return;
		}

		this.addCustomError(errors, "name", BaseValidator.RECORD_IS_NOT_EDITABLE, new Object[] {"method", "Name"});
	}

	private boolean nameValidationProcessor(MethodDetails method, Errors errors) {

		Integer initialCount = errors.getErrorCount();

		// 1. Name is required
		this.shouldNotNullOrEmpty("Name", "name", method.getName(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// Trim name
		method.setName(method.getName().trim());

		// 4. Name is no more than 200 characters
		this.fieldShouldNotOverflow("name", method.getName(), MethodValidator.NAME_TEXT_LIMIT, errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 2. Name is unique
		this.checkTermUniqueness("Method", StringUtil.parseInt(method.getId(), null), method.getName(), CvId.METHODS.getId(), errors);

		return errors.getErrorCount() == initialCount;
	}

	// 5. Description is optional, when provided no more than 1024 characters in length.
	private boolean descriptionValidationProcessor(MethodDetails method, Errors errors) {

		Integer initialCount = errors.getErrorCount();

		if (Strings.isNullOrEmpty(method.getDescription())) {
			method.setDescription("");
		} else {
			method.setDescription(method.getDescription().trim());
		}

		this.fieldShouldNotOverflow("description", method.getDescription(), MethodValidator.DESCRIPTION_TEXT_LIMIT, errors);

		return errors.getErrorCount() == initialCount;
	}
}
