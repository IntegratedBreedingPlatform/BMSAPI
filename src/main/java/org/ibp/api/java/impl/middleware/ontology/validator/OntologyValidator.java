package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.OntologyVariableSummary;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.google.common.base.Strings;

public abstract class OntologyValidator extends BaseValidator {

	protected static final String TERM_DOES_NOT_EXIST = "term.does.not.exist";
	protected static final String INVALID_ID = "invalid.id";
	protected static final String SHOULD_NOT_NULL_OR_EMPTY = "should.not.be.null";
	protected static final String SHOULD_BE_UNIQUE = "should.be.unique";
	protected static final String ENUM_TYPE_NOT_VALID = "enum.type.not.valid";
	protected static final String CAN_NOT_DELETE_REFERRED_TERM = "can.not.delete.referred.term";
	protected static final String NAME_LENGTH_SHOULD_NOT_EXCEED_200_CHARS = "name.should.not.exceed.max.chars";
	protected static final String NAME_LENGTH_SHOULD_NOT_EXCEED_32_CHARS = "variable.name.should.not.exceed.max.chars";
	protected static final String DESCRIPTION_LENGTH_SHOULD_NOT_EXCEED_255_CHARS = "description.should.not.exceed.max.chars";
	protected static final String NAME_SHOULD_NOT_HAVE_SPECIAL_CHARACTERS = "name.should.not.have.special.character";
	protected static final String FIRST_CHARACTER_SHOULD_NOT_BE_NUMERIC = "first.character.should.not.be.numeric";
	protected static final String MIN_MAX_NOT_EXPECTED = "scale.min.max.should.not.supply.when.data.type.non.numeric";
	protected static final String VALUE_SHOULD_BE_NUMERIC = "value.should.be.numeric";
	protected static final String MIN_MAX_NOT_VALID = "scale.min.max.not.valid";
	protected static final String SCALE_DOES_NOT_HAVE_DATA_TYPE = "scale.does.not.have.data.type";
	protected static final String METHOD_PROPERTY_SCALE_COMBINATION_EXIST = "method.property.scale.combination.already.exist";
	protected static final String PROGRAM_DOES_NOT_EXIST = "program.does.not.exist";
	protected static final String EXPECTED_MIN_SHOULD_NOT_LESSER_THAN_SCALE_MIN = "expected.min.should.not.be.smaller";
	protected static final String EXPECTED_MAX_SHOULD_NOT_GREATER_THAN_SCALE_MAX = "expected.max.should.not.be.greater";
	protected static final String SHOULD_BE_STRING = "should.be.string";
	protected static final String TERM_NOT_EDITABLE = "term.not.editable";
	protected static final String SHOULD_HAVE_VALID_DATA_TYPE = "should.have.valid.data.type";
	protected static final String CATEGORY_SHOULD_HAVE_AT_LEAST_ONE_ITEM = "category.should.have.at.least.one.item";

	@Autowired
	protected OntologyManagerService ontologyManagerService;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	protected void checkNumberFieldAndLength(String value, Errors errors) {
		if (value.matches("^[0-9]+$")) {
			try {
				Integer.valueOf(value);
				return;
			} catch (Exception ignored) {
				this.addCustomError(errors, OntologyValidator.INVALID_ID, null);
				return;
			}
		}
		this.addCustomError(errors, OntologyValidator.INVALID_ID, null);
	}

	protected void shouldNotNullOrEmpty(String fieldName, Object value, Errors errors) {
		if (!this.isNullOrEmpty(value)) {
			return;
		}
		this.addCustomError(errors, fieldName, OntologyValidator.SHOULD_NOT_NULL_OR_EMPTY,
				new Object[] { fieldName });
	}

	protected void checkTermExist(String termName, Integer id, Integer cvId, Errors errors) {
		this.checkTermExist(termName, null, id, cvId, errors);
	}

	protected void checkTermExist(String termName, String fieldName, Integer id, Integer cvId,
			Errors errors) {
		try {
			Term term = this.ontologyManagerService.getTermById(id);
			if (Objects.equals(term, null) || !Objects.equals(term.getVocabularyId(), cvId)) {
				if (Strings.isNullOrEmpty(fieldName)) {
					this.addCustomError(errors, OntologyValidator.TERM_DOES_NOT_EXIST,
							new Object[] { termName, id.toString() });
				} else {
					this.addCustomError(errors, fieldName, OntologyValidator.TERM_DOES_NOT_EXIST,
							new Object[] { termName, id.toString() });
				}
			}
		} catch (MiddlewareQueryException e) {
			this.log.error("Error while validating object", e);
			this.addDefaultError(errors);
		}
	}

	protected void checkTermUniqueness(Integer id, String name, Integer cvId, Errors errors) {

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

			this.addCustomError(errors, "name", OntologyValidator.SHOULD_BE_UNIQUE, null);
		} catch (MiddlewareQueryException e) {
			this.log.error("Error checking uniqueness of term name", e);
		}
	}

	protected void checkDataTypeNull(String fieldName, Scale scale, Errors errors) {
		if (scale.getDataType() == null) {
			this.addCustomError(errors, fieldName, OntologyValidator.SCALE_DOES_NOT_HAVE_DATA_TYPE,
					null);
		}
	}

	protected void shouldHaveValidDataType(String fieldName, Integer dataTypeId, Errors errors) {
		if (DataType.getById(dataTypeId) == null) {
			this.addCustomError(errors, fieldName, OntologyValidator.SHOULD_HAVE_VALID_DATA_TYPE,
					null);
		}
	}

	protected void nameShouldHaveMax200Chars(String fieldName, String value, Errors errors) {
		if (value.trim().length() > 200) {
			this.addCustomError(errors, fieldName,
					OntologyValidator.NAME_LENGTH_SHOULD_NOT_EXCEED_200_CHARS, null);
		}
	}

	protected void nameShouldHaveMax32Chars(String fieldName, String value, Errors errors) {
		if (value.trim().length() > 32) {
			this.addCustomError(errors, fieldName,
					OntologyValidator.NAME_LENGTH_SHOULD_NOT_EXCEED_32_CHARS, null);
		}
	}

	protected void descriptionShouldHaveMax255Chars(String fieldName, String value, Errors errors) {
		if (!value.isEmpty() && value.trim().length() > 255) {
			this.addCustomError(errors, fieldName,
					OntologyValidator.DESCRIPTION_LENGTH_SHOULD_NOT_EXCEED_255_CHARS, null);
		}
	}

	protected void nameShouldNotHaveSpecialCharacterAndNoDigitInStart(String fieldName,
			String value, Errors errors) {
		Pattern regex = Pattern.compile("[$&+,:;=?@#|]");
		Matcher matcher = regex.matcher(value);

		if (matcher.find()) {
			this.addCustomError(errors, fieldName,
					OntologyValidator.NAME_SHOULD_NOT_HAVE_SPECIAL_CHARACTERS, null);
		}
		if (Character.isDigit(value.charAt(0))) {
			this.addCustomError(errors, fieldName,
					OntologyValidator.FIRST_CHARACTER_SHOULD_NOT_BE_NUMERIC, null);
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

	protected void shouldHaveValidVariableType(String fieldName, Integer variableTypeId,
			Errors errors) {
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

	protected void checkIfMethodPropertyScaleCombination(String fieldName, Integer methodId,
			Integer propertyId, Integer scaleId, Errors errors) {
		try {
			List<OntologyVariableSummary> variableSummary = this.ontologyManagerService
					.getWithFilter(null, null, methodId, propertyId, scaleId);
			if (!variableSummary.isEmpty()) {
				this.addCustomError(errors, fieldName,
						OntologyValidator.METHOD_PROPERTY_SCALE_COMBINATION_EXIST, null);
			}
		} catch (MiddlewareQueryException e) {
			this.log.error(
					"Error occur while fetching variable in checkIfMethodPropertyScaleCombination",
					e);
		}
	}

	protected void checkIfProgramExist(String fieldName, String programId, Errors errors) {
		try {
			Project project = this.workbenchDataManager.getProjectById(Long.valueOf(programId));
			if (Objects.equals(project, null)) {
				this.addCustomError(errors, fieldName, OntologyValidator.PROGRAM_DOES_NOT_EXIST,
						null);
			}
		} catch (MiddlewareQueryException e) {
			this.log.error("Error occur while fetching program data", e);
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

		this.log.error(String.format("Error in %s.%s", rootCause.getStackTrace()[0].getClassName(),
				rootCause.getStackTrace()[0].getMethodName()), cause);
	}
}
