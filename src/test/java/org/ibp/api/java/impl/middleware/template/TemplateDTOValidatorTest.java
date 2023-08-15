package org.ibp.api.java.impl.middleware.template;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.template.TemplateDTO;
import org.generationcp.middleware.api.template.TemplateDetailsDTO;
import org.generationcp.middleware.api.template.TemplateService;
import org.generationcp.middleware.api.template.TemplateType;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TemplateDTOValidatorTest {

    private static final String PROGRAM_UUID = UUID.randomUUID().toString();

    @InjectMocks
    TemplateDTOValidator templateDTOValidator;

    @Mock
    TemplateService templateService;

    @Test
    public void testValidateTemplateDTO_SUCCESS() {
        try {
            final TemplateDTO templateDTO = this.createTemplateDTO();
            this.templateDTOValidator.validateTemplateDTO(templateDTO);
        } catch (ApiRequestValidationException e) {
            Assert.fail("Should not fail");
        }
    }

    @Test
    public void testValidateTemplateDTO_FAIL_EmptyTemplateDetails() {
        try {
            final TemplateDTO templateDTO = this.createTemplateDTO();
            templateDTO.setTemplateDetails(new ArrayList<>());
            this.templateDTOValidator.validateTemplateDTO(templateDTO);
            Assert.fail("Should fail");
        } catch (ApiRequestValidationException exception) {
            assertThat(exception, instanceOf(ApiRequestValidationException.class));
            assertThat(exception.getErrors().get(0).getCode(), is("template.details.required"));
        }
    }

    @Test
    public void testValidateTemplateDTO_FAIL_EmptyTemplateName() {
        try {
            final TemplateDTO templateDTO = this.createTemplateDTO();
            templateDTO.setTemplateName(null);
            this.templateDTOValidator.validateTemplateDTO(templateDTO);
            Assert.fail("Should fail");
        } catch (ApiRequestValidationException exception) {
            assertThat(exception, instanceOf(ApiRequestValidationException.class));
            assertThat(exception.getErrors().get(0).getCode(), is("template.name.required"));
        }
    }

    @Test
    public void testValidateTemplateDTO_FAIL_TemplateNameExceedsMaxLength() {
        try {
            final TemplateDTO templateDTO = this.createTemplateDTO();
            templateDTO.setTemplateName(RandomStringUtils.randomAlphabetic(256));
            this.templateDTOValidator.validateTemplateDTO(templateDTO);
            Assert.fail("Should fail");
        } catch (ApiRequestValidationException exception) {
            assertThat(exception, instanceOf(ApiRequestValidationException.class));
            assertThat(exception.getErrors().get(0).getCode(), is("template.name.invalid.length"));
        }
    }

    @Test
    public void testValidateTemplateDTO_FAIL_EmptyProgramUUID() {
        try {
            final TemplateDTO templateDTO = this.createTemplateDTO();
            templateDTO.setProgramUUID(null);
            this.templateDTOValidator.validateTemplateDTO(templateDTO);
            Assert.fail("Should fail");
        } catch (ApiRequestValidationException exception) {
            assertThat(exception, instanceOf(ApiRequestValidationException.class));
            assertThat(exception.getErrors().get(0).getCode(), is("template.programuuid.required"));
        }
    }

    @Test
    public void testValidateTemplateDTO_FAIL_EmptyTemplateType() {
        try {
            final TemplateDTO templateDTO = this.createTemplateDTO();
            templateDTO.setTemplateType(null);
            this.templateDTOValidator.validateTemplateDTO(templateDTO);
            Assert.fail("Should fail");
        } catch (ApiRequestValidationException exception) {
            assertThat(exception, instanceOf(ApiRequestValidationException.class));
            assertThat(exception.getErrors().get(0).getCode(), is("template.type.required"));
        }
    }

    @Test
    public void testValidateTemplateDTO_FAIL_InvalidTemplateType() {
        try {
            final TemplateDTO templateDTO = this.createTemplateDTO();
            templateDTO.setTemplateType(RandomStringUtils.randomAlphabetic(5));
            this.templateDTOValidator.validateTemplateDTO(templateDTO);
            Assert.fail("Should fail");
        } catch (ApiRequestValidationException exception) {
            assertThat(exception, instanceOf(ApiRequestValidationException.class));
            assertThat(exception.getErrors().get(0).getCode(), is("template.type.invalid"));
        }
    }

    @Test
    public void testValidateTemplateDTO_FAIL_EmptyTemplateDetailName() {
        try {
            final TemplateDTO templateDTO = this.createTemplateDTO();
            templateDTO.getTemplateDetails().get(0).setName(null);
            this.templateDTOValidator.validateTemplateDTO(templateDTO);
            Assert.fail("Should fail");
        } catch (ApiRequestValidationException exception) {
            assertThat(exception, instanceOf(ApiRequestValidationException.class));
            assertThat(exception.getErrors().get(0).getCode(), is("template.details.name.required"));
        }
    }

    @Test
    public void testValidateTemplateDTO_FAIL_EmptyTemplateDetailType() {
        try {
            final TemplateDTO templateDTO = this.createTemplateDTO();
            templateDTO.getTemplateDetails().get(0).setType(null);
            this.templateDTOValidator.validateTemplateDTO(templateDTO);
            Assert.fail("Should fail");
        } catch (ApiRequestValidationException exception) {
            assertThat(exception, instanceOf(ApiRequestValidationException.class));
            assertThat(exception.getErrors().get(0).getCode(), is("template.details.type.required"));
        }
    }

    @Test
    public void testValidateTemplateDTO_FAIL_EmptyTemplateDetailVariableId() {
        try {
            final TemplateDTO templateDTO = this.createTemplateDTO();
            templateDTO.getTemplateDetails().get(0).setVariableId(null);
            this.templateDTOValidator.validateTemplateDTO(templateDTO);
            Assert.fail("Should fail");
        } catch (ApiRequestValidationException exception) {
            assertThat(exception, instanceOf(ApiRequestValidationException.class));
            assertThat(exception.getErrors().get(0).getCode(), is("template.details.variable.id.required"));
        }
    }

    @Test
    public void testValidateSaveTemplateDTO_SUCCESS() {
        try {
            final TemplateDTO templateDTO = this.createTemplateDTO();
            this.templateDTOValidator.validateSaveTemplateDTO(templateDTO);
        } catch (ApiRequestValidationException e) {
            Assert.fail("Should not fail");
        }
    }

    @Test
    public void testValidateSaveTemplateDTO_FAIL_InvalidTemplateName() {
        try {
            final TemplateDTO templateDTO = this.createTemplateDTO();
            Mockito.when(this.templateService.getTemplateByNameAndProgramUUID(templateDTO.getTemplateName(), PROGRAM_UUID)).thenReturn(new TemplateDTO());
            this.templateDTOValidator.validateSaveTemplateDTO(templateDTO);
            Assert.fail("Should fail");
        } catch (ApiRequestValidationException exception) {
            assertThat(exception, instanceOf(ApiRequestValidationException.class));
            assertThat(exception.getErrors().get(0).getCode(), is("template.name.invalid"));
        }
    }


    @Test
    public void testValidateUPDATETemplateDTO_SUCCESS() {
        try {
            final TemplateDTO templateDTO = this.createTemplateDTO();
            templateDTO.setTemplateId(1);
            Mockito.when(this.templateService.getTemplateByIdAndProgramUUID(templateDTO.getTemplateId(), PROGRAM_UUID)).thenReturn(templateDTO);
            Mockito.when(this.templateService.getTemplateByNameAndProgramUUID(templateDTO.getTemplateName(), PROGRAM_UUID)).thenReturn(templateDTO);
            this.templateDTOValidator.validateUpdateTemplateDTO(templateDTO);
        } catch (ApiRequestValidationException e) {
            Assert.fail("Should not fail");
        }
    }

    @Test
    public void testValidateUPDATETemplateDTO_FAIL_InvalidTemplateId() {
        try {
            final TemplateDTO templateDTO = this.createTemplateDTO();
            templateDTO.setTemplateId(1);
            this.templateDTOValidator.validateUpdateTemplateDTO(templateDTO);
            Assert.fail("Should fail");
        } catch (ApiRequestValidationException exception) {
            assertThat(exception, instanceOf(ApiRequestValidationException.class));
            assertThat(exception.getErrors().get(0).getCode(), is("template.id.invalid"));
        }
    }

    @Test
    public void testValidateUPDATETemplateDTO_FAIL_InvalidTemplateName() {
        try {
            final TemplateDTO templateDTO = this.createTemplateDTO();
            templateDTO.setTemplateId(1);
            Mockito.when(this.templateService.getTemplateByIdAndProgramUUID(templateDTO.getTemplateId(), PROGRAM_UUID)).thenReturn(templateDTO);
            Mockito.when(this.templateService.getTemplateByNameAndProgramUUID(templateDTO.getTemplateName(), PROGRAM_UUID)).thenReturn(new TemplateDTO());
            this.templateDTOValidator.validateUpdateTemplateDTO(templateDTO);
            Assert.fail("Should fail");
        } catch (ApiRequestValidationException exception) {
            assertThat(exception, instanceOf(ApiRequestValidationException.class));
            assertThat(exception.getErrors().get(0).getCode(), is("template.name.invalid"));
        }
    }

    public TemplateDTO createTemplateDTO() {
        final TemplateDTO templateDTO = new TemplateDTO();
        templateDTO.setTemplateName(RandomStringUtils.randomAlphabetic(10));
        templateDTO.setTemplateType(TemplateType.DESCRIPTORS.getName());
        templateDTO.setProgramUUID(PROGRAM_UUID);
        final List<String> possibleValues = new ArrayList<>();
        possibleValues.add(RandomStringUtils.randomAlphabetic(5));
        final TemplateDetailsDTO detail = new TemplateDetailsDTO();
        detail.setName(RandomStringUtils.randomAlphabetic(10));
        detail.setVariableId(Integer.valueOf(RandomStringUtils.randomNumeric(4)));
        detail.setType(VariableType.GERMPLASM_ATTRIBUTE.name());
        templateDTO.setTemplateDetails(Collections.singletonList(detail));
        return templateDTO;
    }
}
