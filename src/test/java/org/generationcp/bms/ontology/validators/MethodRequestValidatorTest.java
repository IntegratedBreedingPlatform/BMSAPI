package org.generationcp.bms.ontology.validators;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.bms.ontology.dto.MethodRequest;
import org.generationcp.bms.ontology.validator.MethodRequestValidator;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.junit.After;
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

import static org.mockito.Mockito.validateMockitoUsage;

public class MethodRequestValidatorTest extends ApiUnitTestBase {

    @Configuration
    public static class TestConfiguration {

        @Bean
        @Primary
        public OntologyManagerService ontologyManagerService() {
            return Mockito.mock(OntologyManagerService.class);
        }

        @Bean
        @Primary
        public MethodRequestValidator methodRequestValidator() {
            return Mockito.mock(MethodRequestValidator.class);
        }
    }

    @Autowired
    private OntologyManagerService ontologyManagerService;

    @Autowired MethodRequestValidator methodRequestValidator;


    @Before
    public void reset(){
        Mockito.reset(ontologyManagerService);
    }

    @After
    public void validate() {
        validateMockitoUsage();
    }

    @Test
    public void testWithUniqueNonNullMethodName() throws MiddlewareQueryException {

        Integer cvId = CvId.METHODS.getId();
        String methodName = "MyMethod";
        String description = "Method Description";

        Term term = new Term(10, methodName + "0", description);

        Mockito.doReturn(term).when(ontologyManagerService).getTermByNameAndCvId(methodName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

        MethodRequest request = new MethodRequest();
        request.setName(methodName);
        request.setDescription(description);


        methodRequestValidator.validate(request, bindingResult);



    }
}
