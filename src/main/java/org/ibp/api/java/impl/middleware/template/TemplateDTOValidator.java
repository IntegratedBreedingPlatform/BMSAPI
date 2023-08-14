package org.ibp.api.java.impl.middleware.template;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.template.TemplateDTO;
import org.generationcp.middleware.api.template.TemplateDetailsDTO;
import org.generationcp.middleware.api.template.TemplateService;
import org.generationcp.middleware.api.template.TemplateType;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkNotNull;

@Component
public class TemplateDTOValidator {
    private static final int MAX_TEMPLATE_NAME_LENGTH = 255;
    @Autowired
    private TemplateService templateService;

    public void validateSaveTemplateDTO(final TemplateDTO templateDTO) {
        this.validateTemplateDTO(templateDTO);

        if (this.templateService.getTemplateByNameAndProgramUUID(templateDTO.getTemplateName(), templateDTO.getProgramUUID()) != null) {
            throw new ApiRequestValidationException("template.name.invalid", new Object[] {templateDTO.getTemplateName()});
        }

    }

    private static void validateTemplateDTO(TemplateDTO templateDTO) {
        checkNotNull(templateDTO, "request.null");
        if (CollectionUtils.isEmpty(templateDTO.getTemplateDetails())) {
            throw new ApiRequestValidationException("template.details.required", new Object[] {});
        }

        if (StringUtils.isEmpty(templateDTO.getTemplateName())) {
            throw new ApiRequestValidationException("template.name.required", new Object[] {});
        }

        if (templateDTO.getTemplateName().length() > MAX_TEMPLATE_NAME_LENGTH) {
            throw new ApiRequestValidationException("template.name.invalid.length", new Object[] {});
        }

        if (StringUtils.isEmpty(templateDTO.getProgramUUID())) {
            throw new ApiRequestValidationException("template.programuuid.required", new Object[] {});
        }

        if (StringUtils.isEmpty(templateDTO.getTemplateType())) {
            throw new ApiRequestValidationException("template.type.required", new Object[] {});
        }

        if (!TemplateType.DESCRIPTORS.getName().equals(templateDTO.getTemplateType())) {
            throw new ApiRequestValidationException("template.type.invalid", new Object[] {templateDTO.getTemplateType()});
        }

        for(final TemplateDetailsDTO detailsDTO: templateDTO.getTemplateDetails()) {
            if (detailsDTO.getVariableId() == null) {
                throw new ApiRequestValidationException("template.details.variable.id.required", new Object[] {});
            }
            if (StringUtils.isEmpty(detailsDTO.getName())) {
                throw new ApiRequestValidationException("template.details.name.required", new Object[] {});
            }
            if (StringUtils.isEmpty(detailsDTO.getType())) {
                throw new ApiRequestValidationException("template.details.type.required", new Object[] {});
            }
        }
    }

    public void validateUpdateTemplateDTO(final TemplateDTO templateDTO) {
        checkNotNull(templateDTO, "request.null");
        final TemplateDTO existingTemplate = this.templateService.getTemplateByIdAndProgramUUID(templateDTO.getTemplateId(), templateDTO.getProgramUUID());
        if (existingTemplate == null) {
            throw new ApiRequestValidationException("template.id.invalid", new Object[] {templateDTO.getTemplateId()});
        }

        final TemplateDTO template = this.templateService.getTemplateByNameAndProgramUUID(templateDTO.getTemplateName(), templateDTO.getProgramUUID());
        if (existingTemplate.getTemplateId() != template.getTemplateId()) {
            throw new ApiRequestValidationException("template.name.invalid", new Object[] {templateDTO.getTemplateName()});
        }

    }
}
