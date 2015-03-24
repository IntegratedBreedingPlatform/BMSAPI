package org.generationcp.bms.ontology.validators;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.bms.ontology.dto.ValidValues;
import org.generationcp.bms.ontology.dto.ScaleRequest;
import org.generationcp.bms.ontology.dto.NameDescription;
import org.generationcp.bms.ontology.validator.ScaleRequestValidator;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class ScaleRequestValidatorTest extends ApiUnitTestBase {

    @Configuration
    public static class TestConfiguration {

        @Bean
        @Primary
        public OntologyManagerService ontologyManagerService() {
            return Mockito.mock(OntologyManagerService.class);
        }

        @Bean
        @Primary
        public ScaleRequestValidator scaleRequestValidator() {
            return Mockito.mock(ScaleRequestValidator.class);
        }
    }

    @Autowired OntologyManagerService ontologyManagerService;

    @Autowired ScaleRequestValidator scaleRequestValidator;

    Integer cvId = CvId.SCALES.getId();
    String scaleName = "MyScale";
    String description = "Scale Description";

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

        Mockito.doReturn(null).when(ontologyManagerService).getTermByNameAndCvId(scaleName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

        ScaleRequest request = new ScaleRequest();
        request.setName("");
        request.setDescription(description);

        scaleRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("name"));
    }

    /**
     * Test for Name is unique
     * @throws org.generationcp.middleware.exceptions.MiddlewareQueryException
     */
    @Test
    public void testWithUniqueNonNullScaleName() throws MiddlewareQueryException {

        Mockito.doReturn(new Term(10, scaleName, description)).when(ontologyManagerService).getTermByNameAndCvId(scaleName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

        ScaleRequest request = new ScaleRequest();
        request.setName(scaleName);
        request.setDescription(description);

        scaleRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("name"));
    }

    /**
     * Test for DataType Required
     */
    @Test
    public void testWithDataTypeRequired(){
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

        ScaleRequest request = new ScaleRequest();
        request.setName(scaleName);
        request.setDescription(description);
        request.setDataTypeId(null);

        scaleRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("dataTypeId"));
    }

    /**
     * Test for to Check Valid DataType
     */
    @Test
    public void testWithValidDataType(){
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

        ScaleRequest request = new ScaleRequest();
        request.setName(scaleName);
        request.setDescription(description);
        request.setDataTypeId(0);

        scaleRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("dataTypeId"));
    }

    /**
     * Test for, If DataType is Categorical and No Categorical Valid Values
     */
    @Test
    public void testWithAtLeastOneCategory(){
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

        ScaleRequest request = new ScaleRequest();
        request.setName(scaleName);
        request.setDescription(description);
        request.setDataTypeId(DataType.CATEGORICAL_VARIABLE.getId());

        scaleRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("validValues.categories"));
    }

    /**
     * Test for No Categorical DataType with Categories
     */
    @Test
    public void testWithNoCategoryDataTypeWithCategoricalData(){
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

        List<NameDescription> categories = new ArrayList<>();
        categories.add(new NameDescription("1","description"));

        ValidValues validValues = new ValidValues();
        validValues.setCategories(categories);

        ScaleRequest request = new ScaleRequest();
        request.setName(scaleName);
        request.setDescription(description);
        request.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());
        request.setValidValues(validValues);

        scaleRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("validValues.categories"));
    }

    /**
     * Test for to Check Label and Value Uniqueness in Categories if DataType is Categorical
     */
    @Test
    public void testWithUniqueLabelNameInCategoricalDataType(){
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

        List<NameDescription> categories = new ArrayList<>();
        categories.add(new NameDescription("1","description"));
        categories.add(new NameDescription("1","description1"));

        ValidValues validValues = new ValidValues();
        validValues.setCategories(categories);

        ScaleRequest request = new ScaleRequest();
        request.setName(scaleName);
        request.setDescription(description);
        request.setDataTypeId(DataType.CATEGORICAL_VARIABLE.getId());
        request.setValidValues(validValues);

        scaleRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("validValues.categories[1].name"));
    }

    /**
     * Test for If DataType is Not Numeric and Mix and Max Value Provided
     */
    @Test
    public void testWithMinMaxValueNonNumericDataType(){
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

        List<NameDescription> categories = new ArrayList<>();
        categories.add(new NameDescription("1","description"));
        categories.add(new NameDescription("11","description1"));

        ValidValues validValues = new ValidValues();
        validValues.setMinValue("5");
        validValues.setMaxValue("10");
        validValues.setCategories(categories);

        ScaleRequest request = new ScaleRequest();
        request.setName(scaleName);
        request.setDescription(description);
        request.setDataTypeId(DataType.CATEGORICAL_VARIABLE.getId());
        request.setValidValues(validValues);

        scaleRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("validValues"));
    }

    /**
     * Test for If DataType is Numeric and Min or Max value is Non-Numeric
     */
    @Test
    public void testWithMinMaxValueNonNumericDataValue(){
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

        ValidValues validValues = new ValidValues();
        validValues.setMinValue("a");
        validValues.setMaxValue("10");

        ScaleRequest request = new ScaleRequest();
        request.setName(scaleName);
        request.setDescription(description);
        request.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());
        request.setValidValues(validValues);

        scaleRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("validValues.minValue"));
    }

    /**
     * Test for If DataType is Numeric and Min value is Greater than Max value.
     */
    @Test
    public void testWithMinValueGreater(){
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

        ValidValues validValues = new ValidValues();
        validValues.setMinValue("10");
        validValues.setMaxValue("1");

        ScaleRequest request = new ScaleRequest();
        request.setName(scaleName);
        request.setDescription(description);
        request.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());
        request.setValidValues(validValues);

        scaleRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("validValues"));
    }

    /**
     * Test for to check name length not exceed 200 characters
     * @throws org.generationcp.middleware.exceptions.MiddlewareQueryException
     */
    @Test
    public void testWithNameLengthExceedMaxLimit() throws MiddlewareQueryException {

        Mockito.doReturn(null).when(ontologyManagerService).getTermByNameAndCvId(scaleName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

        ScaleRequest request = new ScaleRequest();
        request.setName(randomString(205));
        request.setDescription(description);

        scaleRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("name"));
    }

    /**
     * Test for to check description length not exceed 255 characters
     * @throws org.generationcp.middleware.exceptions.MiddlewareQueryException
     */
    @Test
    public void testWithDescriptionLengthExceedMaxLimit() throws MiddlewareQueryException {

        Mockito.doReturn(null).when(ontologyManagerService).getTermByNameAndCvId(scaleName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

        ScaleRequest request = new ScaleRequest();
        request.setName(scaleName);
        request.setDescription(randomString(260));

        scaleRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("description"));
    }

    /**
     * Test for valid request
     */
    @Test
    public void testWithValidRequest(){
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Scale");

        List<NameDescription> categories = new ArrayList<>();
        categories.add(new NameDescription("1","description"));
        categories.add(new NameDescription("11","description1"));

        ValidValues validValues = new ValidValues();
        validValues.setCategories(categories);

        ScaleRequest request = new ScaleRequest();
        request.setName(scaleName);
        request.setDescription(description);
        request.setDataTypeId(DataType.CATEGORICAL_VARIABLE.getId());
        request.setValidValues(validValues);

        scaleRequestValidator.validate(request, bindingResult);
        Assert.assertFalse(bindingResult.hasErrors());
    }
}
