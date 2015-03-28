package org.generationcp.bms.ontology.validators;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.bms.ontology.dto.VariableRequest;
import org.generationcp.bms.ontology.validator.VariableRequestValidator;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyManagerService;
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

public class VariableRequestValidatorTest extends ApiUnitTestBase {

    @Configuration
    public static class TestConfiguration {

        @Bean
        @Primary
        public OntologyManagerService ontologyManagerService() {
            return Mockito.mock(OntologyManagerService.class);
        }

        @Bean
        @Primary
        public VariableRequestValidator variableRequestValidator() {
            return Mockito.mock(VariableRequestValidator.class);
        }
    }

    @Autowired OntologyManagerService ontologyManagerService;

    @Autowired VariableRequestValidator variableRequestValidator;

    Integer cvId = CvId.VARIABLES.getId();
    String variableName = "My Variable";
    String description = "Variable Description";

    @Before
    public void reset(){
        Mockito.reset(ontologyManagerService);
    }

    @After
    public void validate() {
        Mockito.validateMockitoUsage();
    }

    /**
     * Test for Name is required
     * @throws org.generationcp.middleware.exceptions.MiddlewareQueryException
     */
    @Test
    public void testWithNullNameRequest() throws MiddlewareQueryException {

        Mockito.doReturn(null).when(ontologyManagerService).getTermByNameAndCvId(variableName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

        VariableRequest request = new VariableRequest();
        request.setName("");
        request.setDescription(description);

        variableRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("name"));
    }

    /**
     * Test for to check name length not exceed 32 characters
     * @throws org.generationcp.middleware.exceptions.MiddlewareQueryException
     */
    @Test
    public void testWithNameLengthExceedMaxLimit() throws MiddlewareQueryException {

        Mockito.doReturn(null).when(ontologyManagerService).getTermByNameAndCvId(variableName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

        VariableRequest request = new VariableRequest();
        request.setName(randomString(35));
        request.setDescription(description);

        variableRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("name"));
    }

    /**
     * Test for to check description length not exceed 255 characters
     * @throws org.generationcp.middleware.exceptions.MiddlewareQueryException
     */
    @Test
    public void testWithDescriptionLengthExceedMaxLimit() throws MiddlewareQueryException {

        Mockito.doReturn(null).when(ontologyManagerService).getTermByNameAndCvId(variableName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

        VariableRequest request = new VariableRequest();
        request.setName(variableName);
        request.setDescription(randomString(260));

        variableRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("description"));
    }

    /**
     * Test for Name is unique
     * @throws org.generationcp.middleware.exceptions.MiddlewareQueryException
     */
    @Test
    public void testWithUniqueNonNullVariableName() throws MiddlewareQueryException {

        Mockito.doReturn(new Term(10, variableName, description)).when(ontologyManagerService).getTermByNameAndCvId(variableName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

        VariableRequest request = new VariableRequest();
        request.setName(variableName);
        request.setDescription(description);

        variableRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("name"));
    }

    /**
     * Test for Name contain special character and start with digit
     */
    @Test
    public void testWithSpecialCharacterAnsStartWithDigitVariableName(){
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

        VariableRequest request = new VariableRequest();
        request.setName("V@riable");
        request.setDescription(description);

        variableRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("name"));
    }

    /**
     * Test for propertyId is required
     */
    @Test
    public void testWithPropertyIdRequiredRequest(){
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

        VariableRequest request = new VariableRequest();
        request.setName(variableName);
        request.setDescription(description);

        variableRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("propertyId"));
    }

    /**
     * Test for property id exist or not
     * @throws MiddlewareQueryException
     */
    @Test
    public void testWithPropertyIdExistOrNotRequest() throws MiddlewareQueryException {

        VariableRequest request = new VariableRequest();
        request.setName(variableName);
        request.setDescription(description);
        request.setPropertyId(0);

        Mockito.doReturn(null).when(ontologyManagerService).getTermById(request.getPropertyId());
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

        variableRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("id"));
    }

    /**
     * Test for methodId is required
     */
    @Test
    public void testWithMethodIdRequiredRequest() throws MiddlewareQueryException {

        Mockito.doReturn(new Term(10, "name", "", CvId.PROPERTIES.getId(), false)).when(ontologyManagerService).getTermById(10);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

        VariableRequest request = new VariableRequest();
        request.setName(variableName);
        request.setDescription(description);
        request.setPropertyId(10);

        variableRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("methodId"));
    }

    /**
     * Test for method id exist or not
     * @throws MiddlewareQueryException
     */
    @Test
    public void testWithMethodIdExistOrNotRequest() throws MiddlewareQueryException {

        VariableRequest request = new VariableRequest();
        request.setName(variableName);
        request.setDescription(description);
        request.setPropertyId(10);
        request.setMethodId(0);

        Mockito.doReturn(null).when(ontologyManagerService).getTermById(request.getMethodId());
        Mockito.doReturn(new Term(10, "name", "", CvId.PROPERTIES.getId(), false)).when(ontologyManagerService).getTermById(10);
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

        variableRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("id"));
    }

    /**
     * Test for scaleId is required
     */
    @Test
    public void testWithScaleIdRequiredRequest() throws MiddlewareQueryException {

        Mockito.doReturn(new Term(10, "pName", "", CvId.PROPERTIES.getId(), false)).when(ontologyManagerService).getTermById(10);
        Mockito.doReturn(new Term(11, "mName", "", CvId.METHODS.getId(), false)).when(ontologyManagerService).getTermById(11);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

        VariableRequest request = new VariableRequest();
        request.setName(variableName);
        request.setDescription(description);
        request.setPropertyId(10);
        request.setMethodId(11);

        variableRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("scaleId"));
    }

    /**
     * Test for scale id exist or not
     * @throws MiddlewareQueryException
     */
    @Test
    public void testWithScaleIdExistOrNotRequest() throws MiddlewareQueryException {

        VariableRequest request = new VariableRequest();
        request.setName(variableName);
        request.setDescription(description);
        request.setPropertyId(10);
        request.setMethodId(11);
        request.setScaleId(0);

        Mockito.doReturn(null).when(ontologyManagerService).getTermById(request.getScaleId());
        Mockito.doReturn(new Term(10, "name", "", CvId.PROPERTIES.getId(), false)).when(ontologyManagerService).getTermById(10);
        Mockito.doReturn(new Term(11, "mName", "", CvId.METHODS.getId(), false)).when(ontologyManagerService).getTermById(11);
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");

        variableRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("id"));
    }
}
