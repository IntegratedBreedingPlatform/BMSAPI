package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.ibp.api.domain.common.ValidationErrors;
import org.ibp.api.domain.ontology.ProgramId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ProgramValidator2 extends RequestValidatorHelper implements RequestValidator<ProgramId> {

    @Autowired
    public WorkbenchDataManager workbenchDataManager;

    @Override
    public void validate(ProgramId target, ValidationErrors errors) {

        final String fieldName = "programId";
        final String termName = "program";

        // check for program id should not be null
        shouldNotNullOrEmpty(termName, fieldName, target, errors);
        if(!errors.isValid()) {
            return;
        }

        try {
            Project project = workbenchDataManager.getProjectByUuid(target.getId());
            if(Objects.equals(project, null)){
                errors.addError(ID_DOES_NOT_EXIST, fieldName, termName, target.getId());
            }
        } catch (MiddlewareException e) {
            log.error("Error occur while fetching program data", e);
            addMiddlewareError(e, errors);
        }
    }
}

