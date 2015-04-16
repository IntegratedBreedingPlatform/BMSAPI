package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CropNameValidator extends BaseValidator implements Validator {

    protected static final String INVALID_CROP_NAME = "selected.crop.not.valid";

    @Override
    public boolean supports(Class<?> aClass) {
        return String.class.isAssignableFrom(aClass);
    }

    @Autowired
    private WorkbenchDataManager workbenchDataManager;


    @Override
    public void validate(Object target, Errors errors) {

        // check for cropname should not be null
        shouldNotNullOrEmpty("cropname", target, errors);
        if(errors.hasErrors()) {
            return;
        }

        String cropName = (String) target;

        try {

            CropType cropType = workbenchDataManager.getCropTypeByName(cropName);
            if(cropType == null) {
                addCustomError(errors, "cropname", INVALID_CROP_NAME, null);
            }

        } catch (MiddlewareException e) {
            log.error("Error occur while fetching program data", e);
        }
    }
}
