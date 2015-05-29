package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.base.Strings;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.ontology.Property;
import org.ibp.api.domain.ontology.PropertySummary;
import org.ibp.api.java.impl.middleware.common.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Request validator for add/edit property
 * 1 Name is required
 * 2 Name is no more than 200 characters
 * 3 Name is unique
 * 4 Description is no more than 1024 characters
 * 5 Classes must be an array containing at least one string
 * 6 Each class should contain unique value
 * 7 Name cannot change if the property is already in use
 * 8 Individual classes may not be longer than 100 characters each
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
		return PropertySummary.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {

		PropertySummary propertySummary = (PropertySummary) target;

		boolean nameValidationResult = nameValidationProcessor(propertySummary, errors);

		descriptionValidationProcessor(propertySummary, errors);

		cropOntologyIDValidationProcessor(propertySummary, errors);

		classValidationProcessor(propertySummary, errors);

		if(nameValidationResult) {
			propertyShouldBeEditableProcessor(propertySummary, errors);
		}
	}

	private boolean nameValidationProcessor(PropertySummary propertySummary, Errors errors){

		Integer initialCount = errors.getErrorCount();

		// 1. Name is required
		this.shouldNotNullOrEmpty("Name", "name", propertySummary.getName(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		//Trim name
		propertySummary.setName(propertySummary.getName().trim());

		// 2. Name is no more than 200 characters
		this.fieldShouldNotOverflow("name", propertySummary.getName(), NAME_TEXT_LIMIT, errors);

		// 3. Name is unique
		this.checkTermUniqueness("Property", CommonUtil.tryParseSafe(propertySummary.getId()), propertySummary.getName(), CvId.PROPERTIES.getId(), errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean descriptionValidationProcessor(PropertySummary propertySummary, Errors errors){
		Integer initialCount = errors.getErrorCount();

		// Note: If Description is null then initialize it with empty value and if not null then trim.
		if(Strings.isNullOrEmpty(propertySummary.getDescription())) {
			propertySummary.setDescription("");
		} else {
			propertySummary.setDescription(propertySummary.getDescription().trim());
		}

		// 4. Description is no more than 1024 characters
		this.fieldShouldNotOverflow("description", propertySummary.getDescription(), DESCRIPTION_TEXT_LIMIT, errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean cropOntologyIDValidationProcessor(PropertySummary propertySummary, Errors errors){
		Integer initialCount = errors.getErrorCount();

		// Note: If cropOntologyId is null then initialize it with empty value if not then trim.
		if(Strings.isNullOrEmpty(propertySummary.getCropOntologyId())) {
			propertySummary.setCropOntologyId("");
		} else {
			propertySummary.setCropOntologyId(propertySummary.getCropOntologyId().trim());
		}

		// 4. Description is no more than 255 characters
		this.fieldShouldNotOverflow("cropOntologyId", propertySummary.getCropOntologyId(), NAME_TEXT_LIMIT, errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean classValidationProcessor(PropertySummary propertySummary, Errors errors){
		Integer initialCount = errors.getErrorCount();

		Set<String> nonEmptyClasses = new HashSet<>();
		Set<String> classesSet = new HashSet<>();

		// Note: Iterate through each class
		for(String c : propertySummary.getClasses()) {
			if(isNullOrEmpty(c) || classesSet.contains(c.toLowerCase())){
				continue;
			}

			classesSet.add(c.toLowerCase());
			nonEmptyClasses.add(c.trim());
		}

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		propertySummary.setClasses(nonEmptyClasses);

		// 5. Classes must be an array containing at least one string
		if(propertySummary.getClasses().isEmpty()){
			this.addCustomError(errors, "classes", LIST_SHOULD_NOT_BE_EMPTY, new Object[]{"class"});
		}

		// Need to return from here because we should not check other
		// constraints if request is not required to process
		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 6 Each class should contain unique values
		List<String> classes = new ArrayList<>();
		classes.addAll(propertySummary.getClasses());

		for (int i = 1; i <= classes.size(); i++) {
			this.listShouldNotOverflow("class names", "classes", classes.get(i-1), CLASS_TEXT_LIMIT, errors);
			if (errors.getErrorCount() > initialCount) {
				break;
			}
		}

		return errors.getErrorCount() == initialCount;
	}

	private boolean propertyShouldBeEditableProcessor(PropertySummary propertySummary, Errors errors) {
		Integer initialCount = errors.getErrorCount();

		//Check method for edit request
		if (propertySummary.getId() == null) {
			return true;
		}

		Integer propertyId = CommonUtil.tryParseSafe(propertySummary.getId());

		try {

			Property oldProperty = this.ontologyPropertyDataManager.getProperty(propertyId);

			// that property should exist with requestId
			if (Objects.equals(oldProperty, null)) {
				this.addCustomError(errors, ID_DOES_NOT_EXIST, new Object[] { "Property", propertySummary.getId() });
				return false;
			}

			boolean isEditable = !this.termDataManager.isTermReferred(propertyId);

			if (isEditable) {
				return true;
			}

			//Term is referred and check for name changes
			if (Objects.equals(propertySummary.getName(), oldProperty.getName())) {
				return true;
			}

		} catch (Exception e) {
			Throwable rootCause = getRootCause(e);
			LOGGER.error(String.format("Error in %s.%s", rootCause.getStackTrace()[0].getClassName(), rootCause.getStackTrace()[0].getMethodName()), e);
			this.addDefaultError(errors);
			return false;
		}

		this.addCustomError(errors, "name", RECORD_IS_NOT_EDITABLE, new Object[]{"property", "Name"});

		return errors.getErrorCount() == initialCount;
	}
}
