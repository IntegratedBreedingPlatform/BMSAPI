package org.generationcp.bms.ontology.validator;

import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import java.util.Objects;


public abstract class OntologyValidator extends BaseValidator {

    protected static final String DOES_NOT_EXIST = "does.not.exist";
    protected static final String SHOULD_BE_NUMERIC = "should.be.numeric";
    protected static final String SHOULD_NOT_NULL_OR_EMPTY = "should.not.be.null";
    protected static final String SHOULD_BE_UNIQUE = "should.be.unique";
    protected static final String ENUM_TYPE_NOT_VALID = "enum.type.not.valid";

    @Autowired
    protected OntologyManagerService ontologyManagerService;

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

            if (id == null || !Objects.equals(id, term.getId())) {
                addCustomError(errors, "name", SHOULD_BE_UNIQUE, null);
            }
        } catch (MiddlewareQueryException e) {
            log.error("Error checking uniqueness of term name", e);
        }
    }

    protected void shouldHaveValidDataType(String fieldName, Integer dataTypeId, Errors errors){
        if(DataType.getById(dataTypeId) == null) {
            addCustomError(errors,fieldName, ENUM_TYPE_NOT_VALID, null);
        }
    }
}
