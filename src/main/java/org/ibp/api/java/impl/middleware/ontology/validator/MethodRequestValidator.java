package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.base.Strings;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.ibp.api.CommonUtil;
import org.ibp.api.domain.ontology.MethodRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Objects;

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
public class MethodRequestValidator extends OntologyValidator implements org.springframework.validation.Validator {

	private static final Integer NAME_TEXT_LIMIT = 200;
	private static final Integer DESCRIPTION_TEXT_LIMIT = 255;

	@Override
	public boolean supports(Class<?> aClass) {
		return MethodRequest.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {

		MethodRequest request = (MethodRequest) target;

		boolean nameValidationResult = nameValidationProcessor(request, errors);

		descriptionValidationProcessor(request, errors);

		if(nameValidationResult) {
			methodShouldBeEditable(request, errors);
		}
	}

	private void methodShouldBeEditable(MethodRequest request, Errors errors) {

		if (request.getId() == null) {
			return;
		}

		try {

			Method oldMethod = this.ontologyManagerService.getMethod(CommonUtil.tryParseSafe(request.getId()));

			// that method should exist with requestId
			if (Objects.equals(oldMethod, null)) {
				this.addCustomError(errors, ID_DOES_NOT_EXIST, new Object[] { "Method", request.getId()});
				return;
			}

			boolean isEditable = !this.ontologyManagerService.isTermReferred(CommonUtil.tryParseSafe(request.getId()));
			if (isEditable) {
				return;
			}

			if (Objects.equals(request.getName(), oldMethod.getName())) {
				return;
			}

		} catch (MiddlewareException e) {
			this.logError(e);
			this.addDefaultError(errors);
			return;
		}

		this.addCustomError(errors, "name", RECORD_IS_NOT_EDITABLE, new Object[] { "method", "Name" });
	}

	private boolean nameValidationProcessor(MethodRequest request, Errors errors){

		Integer initialCount = errors.getErrorCount();

		// 1. Name is required
		this.shouldNotNullOrEmpty("Name", "name", request.getName(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		//Trim name
		request.setName(request.getName().trim());

		// 4. Name is no more than 200 characters
		this.fieldShouldNotOverflow("name", request.getName(), NAME_TEXT_LIMIT, errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 2. Name is unique
		this.checkTermUniqueness("Method", CommonUtil.tryParseSafe(request.getId()), request.getName(), CvId.METHODS.getId(), errors);

		return errors.getErrorCount() == initialCount;
	}

	// 5. Description is optional no more than 255 characters
	private boolean descriptionValidationProcessor(MethodRequest request, Errors errors){

		Integer initialCount = errors.getErrorCount();

		if(Strings.isNullOrEmpty(request.getDescription())) {
			request.setDescription("");
		} else {
			request.setDescription(request.getDescription().trim());
		}

		if(!isNullOrEmpty(request.getDescription())){
			this.fieldShouldNotOverflow("description", request.getDescription(), DESCRIPTION_TEXT_LIMIT, errors);
		}

		return errors.getErrorCount() == initialCount;
	}
}
