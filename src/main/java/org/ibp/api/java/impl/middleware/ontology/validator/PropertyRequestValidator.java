package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Property;
import org.ibp.api.domain.ontology.PropertyRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.google.common.base.Strings;

/**
 * Request validator for add/edit property 1 Name is required 2 Name is no more
 * than 200 characters 3 Name is unique 4 Description is no more than 255
 * characters 5 Classes must be an array containing at least one string 6 Each
 * class should contain unique value 7 Name cannot change if the property is
 * already in use
 */
@Component
public class PropertyRequestValidator extends OntologyValidator implements
		org.springframework.validation.Validator {

	static final String DUPLICATE_ENTRIES_IN_CLASSES = "property.class.duplicate.entries";
	static final String CLASS_SHOULD_NOT_NULL_OR_EMPTY = "property.class.should.not.null.or.empty";
	static final String CLASS_IS_NECESSARY = "property.class.is.necessary";

	@Override
	public boolean supports(Class<?> aClass) {
		return PropertyRequest.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {

		this.shouldNotNullOrEmpty("request", target, errors);

		if (errors.hasErrors()) {
			return;
		}

		PropertyRequest request = (PropertyRequest) target;

		// 7 Name cannot change if the method is already in use
		this.propertyShouldBeEditable(request, errors);

		// Need to return from here because we should not check other
		// constraints if request is not required to process
		if (errors.hasErrors()) {
			return;
		}

		// 1. Name is required
		this.shouldNotNullOrEmpty("name", request.getName(), errors);

		if (errors.hasErrors()) {
			return;
		}

		// 2. Name is no more than 200 characters
		this.nameShouldHaveMax200Chars("name", request.getName(), errors);

		// 3. Name is unique
		this.checkTermUniqueness(request.getId(), request.getName(), CvId.PROPERTIES.getId(),
				errors);

		// 4. Description is no more than 255 characters
		this.descriptionShouldHaveMax255Chars("description", request.getDescription(), errors);

		// 5. Classes must be an array containing at least one string
		//this.shouldNotNullOrEmpty("classes", request.getClasses(), errors);
	    if(request.getClasses().isEmpty()){
		    this.addCustomError(errors, "classes",
				  PropertyRequestValidator.CLASS_IS_NECESSARY, null);
		}

		// Need to return from here because we should not check other
		// constraints if request is not required to process
		if (errors.hasErrors()) {
			return;
		}

		// 6 Each class should contain unique values
		this.shouldClassesContainUniqueValue(request.getClasses(), errors);
	}

	private void shouldClassesContainUniqueValue(List<String> classes, Errors errors) {

		for (int i = 0; i < classes.size(); i++) {
			if (Strings.isNullOrEmpty(classes.get(i))) {
				this.addCustomError(errors, "classes[" + i + "]",
						PropertyRequestValidator.CLASS_SHOULD_NOT_NULL_OR_EMPTY, null);
			}else {
			  this.classShouldHaveMax200Chars("classes", classes.get(i), errors);
			}
		}

		if (errors.hasErrors()) {
		  	return;
		}

		// Convert to set to check duplication
		Set<String> classesSet = new HashSet<>(classes);

		// If both size are same then there is no duplication of classes
		if (classesSet.size() != classes.size()) {
			this.addCustomError(errors, "classes",
					PropertyRequestValidator.DUPLICATE_ENTRIES_IN_CLASSES, null);
		}
	}

	private void propertyShouldBeEditable(PropertyRequest request, Errors errors) {
		if (request.getId() == null) {
			return;
		}

		try {

			Property oldProperty = this.ontologyManagerService.getProperty(request.getId());

			// that property should exist with requestId
			if (Objects.equals(oldProperty, null)) {
				this.addCustomError(errors, OntologyValidator.TERM_DOES_NOT_EXIST, new Object[] {
						"property", request.getId() });
				return;
			}

			boolean isEditable = !this.ontologyManagerService.isTermReferred(request.getId());
			if (isEditable) {
				return;
			}

			if (Objects.equals(request.getName(), oldProperty.getName())) {
				return;
			}

		} catch (Exception e) {
			this.logError(e);
			this.addDefaultError(errors);
			return;
		}

		this.addCustomError(errors, "name", OntologyValidator.TERM_NOT_EDITABLE, new Object[] {
				"property", "name" });
	}
}
