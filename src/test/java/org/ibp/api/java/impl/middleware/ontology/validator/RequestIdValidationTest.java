package org.ibp.api.java.impl.middleware.ontology.validator;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.impl.middleware.ontology.validator.RequestIdValidator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

public class RequestIdValidationTest extends ApiUnitTestBase {

    @Configuration
    public static class TestConfiguration {

        @Bean
        @Primary
        public OntologyManagerService ontologyManagerService() {
            return Mockito.mock(OntologyManagerService.class);
        }

        @Bean
        @Primary
        public RequestIdValidator requestIdValidator() {
            return Mockito.mock(RequestIdValidator.class);
        }
    }

    @Autowired
    private OntologyManagerService ontologyManagerService;

    @Autowired
    RequestIdValidator requestIdValidator;

    @Before
    public void reset(){
        Mockito.reset(ontologyManagerService);
    }

    @After
    public void validate() {
        Mockito.validateMockitoUsage();
    }

    @Test
    public void testWithNullId() throws MiddlewareQueryException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
        requestIdValidator.validate(null, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("id"));
    }

    @Test
    public void testWithEmptyId() throws MiddlewareQueryException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
        requestIdValidator.validate("", bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("id"));
    }

    @Test
    public void testWithInvalidFormatId() throws MiddlewareQueryException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
        requestIdValidator.validate("1L", bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
    }

    @Test
    public void testWithValidStringId() throws MiddlewareQueryException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
        requestIdValidator.validate("1", bindingResult);
        Assert.assertFalse(bindingResult.hasErrors());
    }

    @Test
    public void testWithIdMoreThanMaximumLimit() throws MiddlewareQueryException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
        requestIdValidator.validate("12345678901", bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
    }

    @Test
    public void testWithValidIntegerId() throws MiddlewareQueryException {
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
        requestIdValidator.validate(1, bindingResult);
        Assert.assertFalse(bindingResult.hasErrors());
    }
}
