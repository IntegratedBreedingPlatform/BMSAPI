
package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.ontology.PropertyDetails;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.google.common.base.Strings;

/**
 * Request validator for add/edit property 1 Name is required 2 Name is no more than 200 characters 3 Name is unique 4 Description is no
 * more than 1024 characters 5 Classes must be an array containing at least one string 6 Each class should contain unique value 7 Name
 * cannot change if the property is already in use 8 Individual classes may not be longer than 100 characters each
 */

/**
 * Extended from {@link OntologyValidator} for basic validation functions and error messages
 */
@Component
public class PropertyValidator extends OntologyValidator implements org.springframework.validation.Validator {

	private static final Integer NAME_TEXT_LIMIT = 200;
	private static final Integer DESCRIPTION_TEXT_LIMIT = 1024;
	private static final Integer CLASS_TEXT_LIMIT = 100;

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyValidator.class);

	@Override
	public boolean supports(Class<?> aClass) {
		return PropertyDetails.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {

		PropertyDetails propertyDetails = (PropertyDetails) target;

		boolean nameValidationResult = this.nameValidationProcessor(propertyDetails, errors);

		this.descriptionValidationProcessor(propertyDetails, errors);

		this.cropOntologyIDValidationProcessor(propertyDetails, errors);

		this.classValidationProcessor(propertyDetails, errors);

		if (nameValidationResult) {
			this.propertyShouldBeEditableProcessor(propertyDetails, errors);
		}
	}

	private boolean nameValidationProcessor(PropertyDetails propertyDetails, Errors errors) {

		Integer initialCount = errors.getErrorCount();

		// 1. Name is required
		this.shouldNotNullOrEmpty("Name", "name", propertyDetails.getName(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// Trim name
		propertyDetails.setName(propertyDetails.getName().trim());

		// 2. Name is no more than 200 characters
		this.fieldShouldNotOverflow("name", propertyDetails.getName(), PropertyValidator.NAME_TEXT_LIMIT, errors);

		// 3. Name is unique
		this.checkTermUniqueness("Property", StringUtil.parseInt(propertyDetails.getId(), null), propertyDetails.getName(),
				CvId.PROPERTIES.getId(), errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean descriptionValidationProcessor(PropertyDetails propertyDetails, Errors errors) {
		Integer initialCount = errors.getErrorCount();

		// Note: If Description is null then initialize it with empty value and if not null then trim.
		if (Strings.isNullOrEmpty(propertyDetails.getDescription())) {
			propertyDetails.setDescription("");
		} else {
			propertyDetails.setDescription(propertyDetails.getDescription().trim());
		}

		// 4. Description is no more than 1024 characters
		this.fieldShouldNotOverflow("description", propertyDetails.getDescription(), PropertyValidator.DESCRIPTION_TEXT_LIMIT, errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean cropOntologyIDValidationProcessor(PropertyDetails propertyDetails, Errors errors) {
		Integer initialCount = errors.getErrorCount();

		// Note: If cropOntologyId is null then initialize it with empty value if not then trim.
		if (Strings.isNullOrEmpty(propertyDetails.getCropOntologyId())) {
			propertyDetails.setCropOntologyId("");
		} else {
			propertyDetails.setCropOntologyId(propertyDetails.getCropOntologyId().trim());
		}

		// 4. Description is no more than 255 characters
		this.fieldShouldNotOverflow("cropOntologyId", propertyDetails.getCropOntologyId(), PropertyValidator.NAME_TEXT_LIMIT, errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean classValidationProcessor(PropertyDetails propertyDetails, Errors errors) {
		Integer initialCount = errors.getErrorCount();

		Set<String> nonEmptyClasses = new HashSet<>();
		Set<String> classesSet = new HashSet<>();

		// Note: Iterate through each class
		for (String c : propertyDetails.getClasses()) {
			if (this.isNullOrEmpty(c) || classesSet.contains(c.toLowerCase())) {
				continue;
			}

			classesSet.add(c.toLowerCase());
			nonEmptyClasses.add(c.trim());
		}

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		propertyDetails.setClasses(nonEmptyClasses);

		// 5. Classes must be an array containing at least one string
		if (propertyDetails.getClasses().isEmpty()) {
			this.addCustomError(errors, "classes", BaseValidator.LIST_SHOULD_NOT_BE_EMPTY, new Object[] {"class"});
		}

		// Need to return from here because we should not check other
		// constraints if request is not required to process
		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 6 Each class should contain unique values
		List<String> classes = new ArrayList<>();
		classes.addAll(propertyDetails.getClasses());

		for (int i = 1; i <= classes.size(); i++) {
			this.listShouldNotOverflow("class names", "classes", classes.get(i - 1), PropertyValidator.CLASS_TEXT_LIMIT, errors);
			if (errors.getErrorCount() > initialCount) {
				break;
			}
		}

		return errors.getErrorCount() == initialCount;
	}

	private boolean propertyShouldBeEditableProcessor(PropertyDetails propertyDetails, Errors errors) {
		Integer initialCount = errors.getErrorCount();

		// Check method for edit request
		if (propertyDetails.getId() == null) {
			return true;
		}

		Integer propertyId = StringUtil.parseInt(propertyDetails.getId(), null);

		try {

			Property oldProperty = this.ontologyPropertyDataManager.getProperty(propertyId, true);

			// that property should exist with requestId
			if (Objects.equals(oldProperty, null)) {
				this.addCustomError(errors, BaseValidator.ID_DOES_NOT_EXIST, new Object[] {"Property", propertyDetails.getId()});
				return false;
			}

			boolean isEditable = !this.termDataManager.isTermReferred(propertyId);

			if (isEditable) {
				return true;
			}

			// Term is referred and check for name changes
			if (Objects.equals(propertyDetails.getName(), oldProperty.getName())) {
				return true;
			}

		} catch (Exception e) {
			Throwable rootCause = this.getRootCause(e);
			PropertyValidator.LOGGER.error(
					String.format("Error in %s.%s", rootCause.getStackTrace()[0].getClassName(),
							rootCause.getStackTrace()[0].getMethodName()), e);
			this.addDefaultError(errors);
			return false;
		}

		this.addCustomError(errors, "name", BaseValidator.RECORD_IS_NOT_EDITABLE, new Object[] {"property", "Name"});

		return errors.getErrorCount() == initialCount;
	}
}
