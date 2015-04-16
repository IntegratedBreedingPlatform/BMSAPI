package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.*;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Property;
import org.ibp.api.CommonUtil;
import org.ibp.api.domain.ontology.PropertyRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * Request validator for add/edit property 1 Name is required 2 Name is no more
 * than 200 characters 3 Name is unique 4 Description is no more than 255
 * characters 5 Classes must be an array containing at least one string 6 Each
 * class should contain unique value 7 Name cannot change if the property is
 * already in use 8 Individual classes may not be longer than 200 characters each
 */
@Component
public class PropertyRequestValidator extends OntologyValidator implements org.springframework.validation.Validator {

	static final String DUPLICATE_ENTRIES_IN_CLASSES = "property.class.duplicate.entries";
	static final String CLASS_IS_NECESSARY = "property.class.is.necessary";

	@Override
	public boolean supports(Class<?> aClass) {
		return PropertyRequest.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {

		this.shouldNotNullOrEmpty("request", target, errors);

		PropertyRequest request = (PropertyRequest) target;

		if (errors.hasErrors()) {
			return;
		}

		boolean nameValidationResult = nameValidationProcessor(request, errors);

		descriptionValidationProcessor(request, errors);

		classValidationProcessor(request, errors);

		if(nameValidationResult) {
			propertyShouldBeEditableProcessor(request, errors);
		}
	}

	private boolean nameValidationProcessor(PropertyRequest request, Errors errors){

		Integer initialCount = errors.getErrorCount();

		// 1. Name is required
		this.shouldNotNullOrEmpty("name", request.getName(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 2. Name is no more than 200 characters
		this.nameShouldHaveMax200Chars("name", request.getName(), errors);

		// 3. Name is unique
		this.checkTermUniqueness(CommonUtil.tryParseSafe(request.getId()), request.getName(), CvId.PROPERTIES.getId(), "property", errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean descriptionValidationProcessor(PropertyRequest request, Errors errors){
		Integer initialCount = errors.getErrorCount();

		// 4. Description is no more than 255 characters
		this.descriptionShouldHaveMax255Chars("description", request.getDescription(), errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean classValidationProcessor(PropertyRequest request, Errors errors){
		Integer initialCount = errors.getErrorCount();

		List<String> nonEmptyClasses = new ArrayList<>();

		for(String c : request.getClasses()) {
			if(isNullOrEmpty(c)){
				continue;
			}
			nonEmptyClasses.add(c);
		}

		request.setClasses(nonEmptyClasses);

		// 5. Classes must be an array containing at least one string
		//this.shouldNotNullOrEmpty("classes", request.getClasses(), errors);
		if(request.getClasses().isEmpty()){
			this.addCustomError(errors, "classes", PropertyRequestValidator.CLASS_IS_NECESSARY, null);
		}

		// Need to return from here because we should not check other
		// constraints if request is not required to process
		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 6 Each class should contain unique values

		List<String> classes = request.getClasses();

		for (int i = 1; i <= classes.size(); i++) {
			this.classShouldHaveMax200Chars("classes[" + i + "]", classes.get(i-1), errors);
		}

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// Convert to set to check duplication
		Set<String> classesSet = new HashSet<>();

		for(String c : request.getClasses()) {
			String loweredName = c.toLowerCase();
			if(classesSet.contains(loweredName)) {
				this.addCustomError(errors, "classes", PropertyRequestValidator.DUPLICATE_ENTRIES_IN_CLASSES, null);
				break;
			}
			classesSet.add(c.toLowerCase());
		}

		return errors.getErrorCount() == initialCount;
	}

	private boolean propertyShouldBeEditableProcessor(PropertyRequest request, Errors errors) {
		Integer initialCount = errors.getErrorCount();

		//Check method for edit request
		if (request.getId() == null) {
			return true;
		}

		Integer propertyId = CommonUtil.tryParseSafe(request.getId());

		try {

			Property oldProperty = this.ontologyManagerService.getProperty(propertyId);

			// that property should exist with requestId
			if (Objects.equals(oldProperty, null)) {
				this.addCustomError(errors, OntologyValidator.TERM_DOES_NOT_EXIST, new Object[] { "property", request.getId() });
				return false;
			}

			boolean isEditable = !this.ontologyManagerService.isTermReferred(propertyId);

			if (isEditable) {
				return true;
			}

			//Term is referred and check for name changes
			if (Objects.equals(request.getName(), oldProperty.getName())) {
				return true;
			}

		} catch (Exception e) {
			this.logError(e);
			this.addDefaultError(errors);
			return false;
		}

		this.addCustomError(errors, "name", OntologyValidator.TERM_NOT_EDITABLE, new Object[]{"property", "name"});

		return errors.getErrorCount() == initialCount;
	}
}
