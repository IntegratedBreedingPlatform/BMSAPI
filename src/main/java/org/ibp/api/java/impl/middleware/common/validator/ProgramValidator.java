package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;

@Component
public class ProgramValidator extends BaseValidator implements Validator {

    protected static final String PROGRAM_DOES_NOT_EXIST = "program.does.not.exist";

    @Override
    public boolean supports(Class<?> aClass) {
        return String.class.isAssignableFrom(aClass);
    }

    @Autowired
    private WorkbenchDataManager workbenchDataManager;


    @Override
    public void validate(Object target, Errors errors) {

        // check for program id should not be null
        shouldNotNullOrEmpty("id", target, errors);
        if(errors.hasErrors()) {
            return;
        }

        // check if program id is non numeric
        String id = (String) target;
        checkNumberFieldAndLength(id, errors);

        if(errors.hasErrors()) {
            return;
        }

        checkIfProgramExist("programId", id, errors);
    }

    protected void checkIfProgramExist(String fieldName, String programId, Errors errors){
        try {
            Project project = workbenchDataManager.getProjectById(Long.valueOf(programId));
            if(Objects.equals(project, null)){
                addCustomError(errors, fieldName, PROGRAM_DOES_NOT_EXIST, null);
            }
        } catch (MiddlewareQueryException e) {
            log.error("Error occur while fetching program data", e);
        }
    }
}
