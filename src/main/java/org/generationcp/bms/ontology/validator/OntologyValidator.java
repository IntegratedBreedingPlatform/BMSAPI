package org.generationcp.bms.ontology.validator;

import org.generationcp.middleware.domain.oms.*;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;


public abstract class OntologyValidator extends BaseValidator {

    protected static final String DOES_NOT_EXIST = "does.not.exist";
    protected static final String SHOULD_BE_NUMERIC = "should.be.numeric";
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

    @Autowired
    protected OntologyManagerService ontologyManagerService;

    @Autowired
    private WorkbenchDataManager workbenchDataManager;

    protected void checkNumberField(String fieldName, String value, Errors errors){
        if(value.matches("^[0-9]+$")){
            return;
        }
        addCustomError(errors, fieldName, SHOULD_BE_NUMERIC, null);
    }

    protected void shouldNotNullOrEmpty(String fieldName, Object value, Errors errors){
        if(!isNullOrEmpty(value)){
            return;
        }
        addCustomError(errors, fieldName, SHOULD_NOT_NULL_OR_EMPTY, null);
    }

    protected void checkTermExist(Integer id, Integer cvId, Errors errors){
        try {
            Term term = ontologyManagerService.getTermById(id);
            if(Objects.equals(term, null) || !Objects.equals(term.getVocabularyId(), cvId) ){
                addCustomError(errors, "id", DOES_NOT_EXIST, new Object[]{id.toString()});
            }
        } catch (Exception e) {
            log.error("Error while validating object", e);
            addDefaultError(errors);
        }
    }

    protected void checkTermUniqueness(Integer id, String name, Integer cvId, Errors errors) {

        try {
            Term term = ontologyManagerService.getTermByNameAndCvId(name, cvId);
            if (term == null){
                return;
            }

            if(Objects.equals(id, null) && Objects.equals(term, null)){
                return;
            }

            if (id != null && Objects.equals(id, term.getId())) {
                return;
            }

            addCustomError(errors, "name", SHOULD_BE_UNIQUE, null);
        } catch (MiddlewareQueryException e) {
            log.error("Error checking uniqueness of term name", e);
        }
    }

    protected void checkDataTypeNull(String fieldName, Scale scale, Errors errors){
        if(scale.getDataType() == null){
            addCustomError(errors,fieldName, SCALE_DOES_NOT_HAVE_DATA_TYPE, null);
        }
    }

    protected void shouldHaveValidDataType(String fieldName, Integer dataTypeId, Errors errors){
        if(DataType.getById(dataTypeId) == null) {
            addCustomError(errors,fieldName, ENUM_TYPE_NOT_VALID, null);
        }
    }

    protected void nameShouldHaveMax200Chars(String fieldName, String value, Errors errors){
       if(value.trim().length() > 200){
           addCustomError(errors, fieldName, NAME_LENGTH_SHOULD_NOT_EXCEED_200_CHARS, null);
       }
    }

    protected void nameShouldHaveMax32Chars(String fieldName, String value, Errors errors){
        if(value.trim().length() > 32){
            addCustomError(errors, fieldName, NAME_LENGTH_SHOULD_NOT_EXCEED_32_CHARS, null);
        }
    }

    protected void descriptionShouldHaveMax255Chars(String fieldName, String value, Errors errors){
        if(!value.isEmpty()){
            if(value.trim().length() > 255){
                addCustomError(errors, fieldName, DESCRIPTION_LENGTH_SHOULD_NOT_EXCEED_255_CHARS, null);
            }
        }
    }

    protected void nameShouldNotHaveSpecialCharacterAndNoDigitInStart(String fieldName, String value, Errors errors){
        Pattern regex = Pattern.compile("[$&+,:;=?@#|]");
        Matcher matcher = regex.matcher(value);

        if(matcher.find()){
            addCustomError(errors, fieldName, NAME_SHOULD_NOT_HAVE_SPECIAL_CHARACTERS, null);
        }
        if(Character.isDigit(value.charAt(0))){
            addCustomError(errors, fieldName, FIRST_CHARACTER_SHOULD_NOT_BE_NUMERIC, null);
        }
    }

    protected void shouldHaveValidVariableType(String fieldName, Integer variableTypeId, Errors errors){
        if(VariableType.getById(variableTypeId) == null) {
            addCustomError(errors,fieldName, ENUM_TYPE_NOT_VALID, null);
        }
    }

    protected boolean checkNumericDataType(DataType dataType){
        return Objects.equals(dataType, DataType.NUMERIC_VARIABLE);
    }

    protected void checkIntegerNull(String fieldName, Integer value, Errors errors){
        if(value == null){
            addCustomError(errors,fieldName, SHOULD_NOT_NULL_OR_EMPTY, null);
        }
    }

    protected void checkIfMethodPropertyScaleCombination(String fieldName, Integer methodId, Integer propertyId, Integer scaleId, Errors errors){
        try {
            List<OntologyVariableSummary> variableSummary =  ontologyManagerService.getWithFilter(null, null, methodId, propertyId, scaleId);
            if(!variableSummary.isEmpty()){
                addCustomError(errors,fieldName, METHOD_PROPERTY_SCALE_COMBINATION_EXIST, null);
            }
        } catch (MiddlewareQueryException e) {
            log.error("Error occur while fetching variable in checkIfMethodPropertyScaleCombination", e);
        }
    }

    protected void checkIfProgramExist(String fieldName, String programId, Errors errors){
        try {
            Project project = workbenchDataManager.getProjectById(Long.valueOf(programId));
            if(Objects.equals(project, null)){
                addCustomError(errors,fieldName, PROGRAM_DOES_NOT_EXIST, null);
            }
        } catch (MiddlewareQueryException e) {
            log.error("Error occur while fetching program data", e);
        }
    }

    protected Scale getScaleData(Integer scaleId){
        try {
            return ontologyManagerService.getScaleById(scaleId);
        } catch (MiddlewareQueryException e) {
            log.error("Error occur while fetching scale", e);
        }
        return null;
    }

    protected void logError(final Throwable cause){
        Throwable rootCause = cause;
        while(rootCause.getCause() != null &&  rootCause.getCause() != rootCause){
            rootCause = rootCause.getCause();
        }

        log.error(String.format("Error in %s.%s", rootCause.getStackTrace()[0].getClassName(), rootCause.getStackTrace()[0].getMethodName()), cause);
    }
}
