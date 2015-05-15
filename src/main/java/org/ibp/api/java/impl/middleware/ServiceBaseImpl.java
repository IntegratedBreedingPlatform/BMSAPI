package org.ibp.api.java.impl.middleware;

import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.ontology.validator.MiddlewareIdFormatValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermDeletableValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

public class ServiceBaseImpl {

    @Autowired
    protected TermDataManager termDataManager;

    @Autowired
    protected TermDeletableValidator termDeletableValidator;

    @Autowired
    protected MiddlewareIdFormatValidator idFormatValidator;

    @Autowired
    protected TermValidator termValidator;

    // Note: Used for validating id format and id exists or not
    protected void validateId(String id, String termName) {
        BindingResult errors = new MapBindingResult(new HashMap<String, String>(), termName);
        this.idFormatValidator.validate(id, errors);
        if (errors.hasErrors()) {
            throw new ApiRequestValidationException(errors.getAllErrors());
        }
    }

}
