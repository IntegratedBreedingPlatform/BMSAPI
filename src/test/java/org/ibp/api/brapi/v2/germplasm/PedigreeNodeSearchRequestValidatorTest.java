package org.ibp.api.brapi.v2.germplasm;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v2.germplasm.PedigreeNodeSearchRequest;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PedigreeNodeSearchRequestValidatorTest {

    @InjectMocks
    private PedigreeNodeSearchRequestValidator pedigreeNodeSearchRequestValidator;

    @Test
    public void testValidatePedigreeNodeSearchRequest_Fail() {
        try {
            this.pedigreeNodeSearchRequestValidator.validatePedigreeNodeSearchRequest(new PedigreeNodeSearchRequest());
            Assert.fail("Should throw an ApiRequestValidationException");
        } catch (ApiRequestValidationException e) {
            assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("pedigree.node.search.request.invalid"));
        }
    }

    @Test
    public void testValidatePedigreeNodeSearchRequest_Success() {
        try {
            final PedigreeNodeSearchRequest pedigreeNodeSearchRequest = new PedigreeNodeSearchRequest();
            pedigreeNodeSearchRequest.setAccessionNumbers(Collections.singletonList(RandomStringUtils.randomAlphanumeric(5)));
            pedigreeNodeSearchRequest.setCommonCropNames(Collections.singletonList(RandomStringUtils.randomAlphanumeric(5)));
            pedigreeNodeSearchRequest.setExternalReferenceIds(Collections.singletonList(RandomStringUtils.randomAlphanumeric(5)));
            pedigreeNodeSearchRequest.setExternalReferenceSources(Collections.singletonList(RandomStringUtils.randomAlphanumeric(5)));
            pedigreeNodeSearchRequest.setGenus(Collections.singletonList(RandomStringUtils.randomAlphanumeric(5)));
            pedigreeNodeSearchRequest.setGermplasmDbIds(Collections.singletonList(RandomStringUtils.randomAlphanumeric(5)));
            pedigreeNodeSearchRequest.setGermplasmNames(Collections.singletonList(RandomStringUtils.randomAlphanumeric(5)));
            pedigreeNodeSearchRequest.setGermplasmPUIs(Collections.singletonList(RandomStringUtils.randomAlphanumeric(5)));
            pedigreeNodeSearchRequest.setSpecies(Collections.singletonList(RandomStringUtils.randomAlphanumeric(5)));
            pedigreeNodeSearchRequest.setStudyDbIds(Collections.singletonList(RandomStringUtils.randomAlphanumeric(5)));
            pedigreeNodeSearchRequest.setSynonyms(Collections.singletonList(RandomStringUtils.randomAlphanumeric(5)));
            pedigreeNodeSearchRequest.setTrialDbIds(Collections.singletonList(RandomStringUtils.randomAlphanumeric(5)));
            pedigreeNodeSearchRequest.setTrialNames(Collections.singletonList(RandomStringUtils.randomAlphanumeric(5)));

            this.pedigreeNodeSearchRequestValidator.validatePedigreeNodeSearchRequest(pedigreeNodeSearchRequest);
        } catch (ApiRequestValidationException e) {
            Assert.fail("Should not throw an ApiRequestValidationException");
        }
    }
}
