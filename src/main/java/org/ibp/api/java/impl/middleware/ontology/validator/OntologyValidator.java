package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.base.Strings;
import org.generationcp.middleware.domain.oms.*;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class OntologyValidator extends BaseValidator {

	protected static final String TERM_DOES_NOT_EXIST = "term.does.not.exist";
	protected static final String SHOULD_NOT_NULL_OR_EMPTY = "should.not.be.null";
	protected static final String SHOULD_BE_UNIQUE = "should.be.unique";
	protected static final String ENUM_TYPE_NOT_VALID = "enum.type.not.valid";
	protected static final String CAN_NOT_DELETE_REFERRED_TERM = "can.not.delete.referred.term";
	protected static final String NAME_LENGTH_SHOULD_NOT_EXCEED_200_CHARS = "name.should.not.exceed.max.chars";
	protected static final String NAME_LENGTH_SHOULD_NOT_EXCEED_32_CHARS = "variable.name.should.not.exceed.max.chars";
	protected static final String DESCRIPTION_LENGTH_SHOULD_NOT_EXCEED_255_CHARS = "description.should.not.exceed.max.chars";
	protected static final String NAME_SHOULD_NOT_HAVE_SPECIAL_CHARACTERS_AND_NOT_START_WITH_DIGIT = "name.should.not.have.special.character.and.first.digit";
	protected static final String MIN_MAX_NOT_EXPECTED = "scale.min.max.should.not.supply.when.data.type.non.numeric";
	protected static final String VALUE_SHOULD_BE_NUMERIC = "value.should.be.numeric";
	protected static final String MIN_MAX_NOT_VALID = "scale.min.max.not.valid";
	protected static final String METHOD_PROPERTY_SCALE_COMBINATION_EXIST = "method.property.scale.combination.already.exist";
	protected static final String EXPECTED_MIN_SHOULD_NOT_LESSER_THAN_SCALE_MIN = "expected.min.should.not.be.smaller";
	protected static final String EXPECTED_MAX_SHOULD_NOT_GREATER_THAN_SCALE_MAX = "expected.max.should.not.be.greater";
  	protected static final String EXPECTED_MIN_SHOULD_NOT_BE_GREATER_THAN_MAX = "expected.range.min.should.not.be.greater.than.max";
	protected static final String SHOULD_BE_STRING = "should.be.string";
	protected static final String TERM_NOT_EDITABLE = "term.not.editable";
	protected static final String SHOULD_HAVE_VALID_DATA_TYPE = "should.have.valid.data.type";
	protected static final String CATEGORY_SHOULD_HAVE_AT_LEAST_ONE_ITEM = "category.should.have.at.least.one.item";
    protected static final String CATEGORY_DESCRIPTION_IS_NECESSARY = "category.description.is.necessary";
    protected static final String CATEGORY_NAME_IS_NECESSARY = "category.name.is.necessary";
  	protected static final String CLASS_LENGTH_SHOULD_NOT_EXCEED_200_CHARS = "class.should.not.exceed.max.chars";
  	protected static final String VARIABLE_TYPE_ID_IS_REQUIRED = "variable.type.id.is.required";

	@Autowired
	protected OntologyManagerService ontologyManagerService;

	protected void checkTermExist(String termName, String id, Integer cvId, Errors errors) {
		this.checkTermExist(termName, null, id, cvId, errors);
	}

	protected void checkTermExist(String termName, String fieldName, String id, Integer cvId, Errors errors) {
		try {
			Term term = this.ontologyManagerService.getTermById(Integer.valueOf(id));
			if (Objects.equals(term, null) || !Objects.equals(term.getVocabularyId(), cvId)) {
				if (Strings.isNullOrEmpty(fieldName)) {
					this.addCustomError(errors, OntologyValidator.TERM_DOES_NOT_EXIST, new Object[] { termName, id });
				} else {
					this.addCustomError(errors, fieldName, OntologyValidator.TERM_DOES_NOT_EXIST, new Object[] { termName, id });
				}
			}
		} catch (MiddlewareQueryException e) {
			this.log.error("Error while validating object", e);
			this.addDefaultError(errors);
		}
	}

	protected void checkTermUniqueness(Integer id, String name, Integer cvId, String termName, Errors errors) {

		try {
			Term term = this.ontologyManagerService.getTermByNameAndCvId(name, cvId);
			if (term == null) {
				return;
			}

			if (Objects.equals(id, null) && Objects.equals(term, null)) {
				return;
			}

			if (id != null && Objects.equals(id, term.getId())) {
				return;
			}

			this.addCustomError(errors, "name", OntologyValidator.SHOULD_BE_UNIQUE, new Object[]{termName});
		} catch (MiddlewareQueryException e) {
			this.log.error("Error checking uniqueness of term name", e);
		}
	}

	protected void shouldHaveValidDataType(String fieldName, Integer dataTypeId, Errors errors) {
		if (DataType.getById(dataTypeId) == null) {
			this.addCustomError(errors, fieldName, OntologyValidator.SHOULD_HAVE_VALID_DATA_TYPE, null);
		}
	}

	protected void nameShouldHaveMax200Chars(String fieldName, String value, Errors errors) {
		if (value.trim().length() > 200) {
			this.addCustomError(errors, fieldName, OntologyValidator.NAME_LENGTH_SHOULD_NOT_EXCEED_200_CHARS, null);
		}
	}

	protected void nameShouldHaveMax32Chars(String fieldName, String value, Errors errors) {
		if (value.trim().length() > 32) {
			this.addCustomError(errors, fieldName, OntologyValidator.NAME_LENGTH_SHOULD_NOT_EXCEED_32_CHARS, null);
		}
	}

	protected void descriptionShouldHaveMax255Chars(String fieldName, String value, Errors errors) {
		if (!value.isEmpty() && value.trim().length() > 255) {
			this.addCustomError(errors, fieldName, OntologyValidator.DESCRIPTION_LENGTH_SHOULD_NOT_EXCEED_255_CHARS, null);
		}
	}

	protected void classShouldHaveMax200Chars(String fieldName, String value, Errors errors) {
		if (value.trim().length() > 200) {
		  	this.addCustomError(errors, fieldName,
				  OntologyValidator.CLASS_LENGTH_SHOULD_NOT_EXCEED_200_CHARS, null);
		}
	}

	protected void nameShouldNotHaveSpecialCharacterAndNoDigitInStart(String fieldName, String value, Errors errors) {
		Pattern regex = Pattern.compile("[$&+,./%')\\[}\\]{(*^!`~:;=?@#|]");
		Matcher matcher = regex.matcher(value);

		if (matcher.find() || Character.isDigit(value.charAt(0))) {
			this.addCustomError(errors, fieldName, OntologyValidator.NAME_SHOULD_NOT_HAVE_SPECIAL_CHARACTERS_AND_NOT_START_WITH_DIGIT, null);
		}
	}

	protected void shouldHaveValidString(String fieldName, String value, Errors errors) {
		Pattern regex = Pattern.compile("[$&+,./%')\\[}\\]{(*^!`~:;=?@#|1234567890]");
		Matcher matcher = regex.matcher(value);

		if (value.isEmpty()) {
			return;
		}
		if (matcher.find()) {
			this.addCustomError(errors, fieldName, OntologyValidator.SHOULD_BE_STRING, null);
		}
	}

	protected void shouldHaveValidVariableType(String fieldName, Integer variableTypeId, Errors errors) {
		if (VariableType.getById(variableTypeId) == null) {
			this.addCustomError(errors, fieldName, OntologyValidator.ENUM_TYPE_NOT_VALID, null);
		}
	}

	protected boolean checkNumericDataType(DataType dataType) {
		return Objects.equals(dataType, DataType.NUMERIC_VARIABLE);
	}

	protected void checkIntegerNull(String fieldName, Integer value, Errors errors) {
		if (value == null) {
			this.addCustomError(errors, fieldName, OntologyValidator.SHOULD_NOT_NULL_OR_EMPTY, null);
		}
	}

	protected void checkIfMethodPropertyScaleCombination(String fieldName, Integer methodId, Integer propertyId, Integer scaleId, Errors errors) {
	  try {
		List<OntologyVariableSummary> variableSummary = this.ontologyManagerService
					.getWithFilter(null, null, methodId, propertyId, scaleId);
			if (!variableSummary.isEmpty()) {
				this.addCustomError(errors, fieldName,
						OntologyValidator.METHOD_PROPERTY_SCALE_COMBINATION_EXIST, null);
			}
		} catch (MiddlewareQueryException e) {
			this.log.error("Error occur while fetching variable in checkIfMethodPropertyScaleCombination", e);
		}
	}

	protected Scale getScaleData(Integer scaleId) {
		try {
			return this.ontologyManagerService.getScaleById(scaleId);
		} catch (MiddlewareQueryException e) {
			this.log.error("Error occur while fetching scale", e);
		}
		return null;
	}

	protected void logError(final Throwable cause) {
		Throwable rootCause = cause;
		while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
			rootCause = rootCause.getCause();
		}

		this.log.error(String.format("Error in %s.%s", rootCause.getStackTrace()[0].getClassName(), rootCause.getStackTrace()[0].getMethodName()), cause);
	}
}
