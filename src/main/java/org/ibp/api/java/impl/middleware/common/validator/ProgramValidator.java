package org.ibp.api.java.impl.middleware.common.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ProgramValidator extends BaseValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return String.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

        // check for program id should not be null
        shouldNotNullOrEmpty("programId", target, errors);
        if(errors.hasErrors()) {
            return;
        }

        // check if program id is non numeric
        String id = (String) target;

        checkIfProgramExist("programId", id, errors);
    }

    //TODO: Need method for get project by uuid from middleware.
    protected void checkIfProgramExist(String fieldName, String programId, Errors errors){
        /*try {
            Project project = workbenchDataManager.getProjectById(Long.valueOf(programId));
            if(Objects.equals(project, null)){
                addCustomError(errors, fieldName, PROGRAM_DOES_NOT_EXIST, null);
            }
        } catch (MiddlewareQueryException e) {
            log.error("Error occur while fetching program data", e);
        }*/
    }
}
