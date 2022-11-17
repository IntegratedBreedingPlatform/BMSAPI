package org.ibp.api.brapi.v2.germplasm;

import org.generationcp.middleware.api.brapi.v2.germplasm.PedigreeNodeSearchRequest;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

@Component
public class PedigreeNodeSearchRequestValidator {

    public void validatePedigreeNodeSearchRequest(final PedigreeNodeSearchRequest pedigreeNodeSearchRequest) {
        final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());

        if (CollectionUtils.isEmpty(pedigreeNodeSearchRequest.getAccessionNumbers()) &&
            CollectionUtils.isEmpty(pedigreeNodeSearchRequest.getCommonCropNames()) &&
            CollectionUtils.isEmpty(pedigreeNodeSearchRequest.getExternalReferenceIds()) &&
            CollectionUtils.isEmpty(pedigreeNodeSearchRequest.getExternalReferenceSources()) &&
            CollectionUtils.isEmpty(pedigreeNodeSearchRequest.getGenus()) &&
            CollectionUtils.isEmpty(pedigreeNodeSearchRequest.getGermplasmDbIds()) &&
            CollectionUtils.isEmpty(pedigreeNodeSearchRequest.getGermplasmNames()) &&
            CollectionUtils.isEmpty(pedigreeNodeSearchRequest.getGermplasmPUIs()) &&
            CollectionUtils.isEmpty(pedigreeNodeSearchRequest.getSpecies()) &&
            CollectionUtils.isEmpty(pedigreeNodeSearchRequest.getStudyDbIds()) &&
            CollectionUtils.isEmpty(pedigreeNodeSearchRequest.getSynonyms()) &&
            CollectionUtils.isEmpty(pedigreeNodeSearchRequest.getTrialDbIds()) &&
            CollectionUtils.isEmpty(pedigreeNodeSearchRequest.getTrialNames())) {

            errors.reject("pedigree.node.search.request.invalid", "");
            throw new ApiRequestValidationException(errors.getAllErrors());
        }
    }
}
