package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.util.Util;
import org.ibp.api.domain.ontology.PropertyRequest;
import org.ibp.api.java.impl.middleware.common.CommonUtil;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.*;

/**
 * Request validator for add/edit property
 * 1 Name is required
 * 2 Name is no more than 200 characters
 * 3 Name is unique
 * 4 Description is no more than 255 characters
 * 5 Classes must be an array containing at least one string
 * 6 Each class should contain unique value
 * 7 Name cannot change if the property is already in use
 * 8 Individual classes may not be longer than 100 characters each
 */
@Component
public class PropertyRequestValidator extends OntologyValidator implements org.springframework.validation.Validator {

	private static final Integer NAME_TEXT_LIMIT = 200;
	private static final Integer DESCRIPTION_TEXT_LIMIT = 255;
	private static final Integer CLASS_TEXT_LIMIT = 100;
	private static final String PROPERTY_CLASS_NOT_VALID = "class.not.valid";

	@Override
	public boolean supports(Class<?> aClass) {
		return PropertyRequest.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {

		PropertyRequest request = (PropertyRequest) target;

		boolean nameValidationResult = nameValidationProcessor(request, errors);

		descriptionValidationProcessor(request, errors);

		cropOntologyIDValidationProcessor(request, errors);

		classValidationProcessor(request, errors);

		if(nameValidationResult) {
			propertyShouldBeEditableProcessor(request, errors);
		}
	}

	private boolean nameValidationProcessor(PropertyRequest request, Errors errors){

		Integer initialCount = errors.getErrorCount();

		// 1. Name is required
		this.shouldNotNullOrEmpty("Name", "name", request.getName(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		//Trim name
		request.setName(request.getName().trim());

		// 2. Name is no more than 200 characters
		this.fieldShouldNotOverflow("name", request.getName(), NAME_TEXT_LIMIT, errors);

		// 3. Name is unique
		this.checkTermUniqueness("Property", CommonUtil.tryParseSafe(request.getId()), request.getName(), CvId.PROPERTIES.getId(), errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean descriptionValidationProcessor(PropertyRequest request, Errors errors){
		Integer initialCount = errors.getErrorCount();

		if(Strings.isNullOrEmpty(request.getDescription())) {
			request.setDescription("");
		} else {
			request.setDescription(request.getDescription().trim());
		}

		// 4. Description is no more than 255 characters
		this.fieldShouldNotOverflow("description", request.getDescription(), DESCRIPTION_TEXT_LIMIT, errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean cropOntologyIDValidationProcessor(PropertyRequest request, Errors errors){
		Integer initialCount = errors.getErrorCount();

		if(Strings.isNullOrEmpty(request.getCropOntologyId())) {
			request.setCropOntologyId("");
		} else {
			request.setCropOntologyId(request.getCropOntologyId().trim());
		}

		// 4. Description is no more than 255 characters
		this.fieldShouldNotOverflow("cropOntologyId", request.getCropOntologyId(), NAME_TEXT_LIMIT, errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean classValidationProcessor(PropertyRequest request, Errors errors){
		Integer initialCount = errors.getErrorCount();

		List<String> nonEmptyClasses = new ArrayList<>();
		Set<String> classesSet = new HashSet<>();

		List<String> traitClasses = new ArrayList<>();
		List<String> allTerms = new ArrayList<>();
		try {

			//Add all classes to String list
			traitClasses = Util.convertAll(this.ontologyBasicDataManager.getAllTraitClass(), new Function<Term, String>() {
				@Override
				public String apply(Term term) {
					return term.getName();
				}
			});

			//Add all terms to String list
			allTerms = Util.convertAll(this.ontologyBasicDataManager.getTermByCvId(CvId.IBDB_TERMS.getId()), new Function<Term, String>() {
				@Override
				public String apply(Term term) {
					return term.getName();
				}
			});

		} catch (MiddlewareException e) {
			log.error("Error in getting all trait classes.");
		}

		for(String c : request.getClasses()) {
			if(isNullOrEmpty(c) || classesSet.contains(c.toLowerCase())){
				continue;
			}

			// Check if class element already in terms
			if(!traitClasses.contains(c) && allTerms.contains(c)) {
				this.addCustomError(errors, "classes", PROPERTY_CLASS_NOT_VALID, new Object[]{c});
			}

			classesSet.add(c.toLowerCase());
			nonEmptyClasses.add(c.trim());
		}

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		request.setClasses(nonEmptyClasses);

		// 5. Classes must be an array containing at least one string
		if(request.getClasses().isEmpty()){
			this.addCustomError(errors, "classes", LIST_SHOULD_NOT_BE_EMPTY, new Object[]{"class"});
		}

		// Need to return from here because we should not check other
		// constraints if request is not required to process
		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 6 Each class should contain unique values
		List<String> classes = request.getClasses();

		for (int i = 1; i <= classes.size(); i++) {
			this.listShouldNotOverflow("class names", "classes", classes.get(i-1), CLASS_TEXT_LIMIT, errors);
			if (errors.getErrorCount() > initialCount) {
				break;
			}
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

			Property oldProperty = this.ontologyPropertyDataManager.getProperty(propertyId);

			// that property should exist with requestId
			if (Objects.equals(oldProperty, null)) {
				this.addCustomError(errors, ID_DOES_NOT_EXIST, new Object[] { "Property", request.getId() });
				return false;
			}

			boolean isEditable = !this.ontologyBasicDataManager.isTermReferred(propertyId);

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

		this.addCustomError(errors, "name", RECORD_IS_NOT_EDITABLE, new Object[]{"property", "Name"});

		return errors.getErrorCount() == initialCount;
	}
}
