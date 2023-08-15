package org.ibp.api.java.impl.middleware.template;

import org.generationcp.middleware.api.template.TemplateDTO;
import org.ibp.api.java.template.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private TemplateDTOValidator templateDTOValidator;

    @Autowired
    private org.generationcp.middleware.api.template.TemplateService templateServiceMw;

    @Override
    public TemplateDTO saveTemplate(final String crop, final TemplateDTO templateDTO) {
        this.templateDTOValidator.validateSaveTemplateDTO(templateDTO);
        return this.templateServiceMw.saveTemplate(templateDTO);
    }

    @Override
    public List<TemplateDTO> getTemplates (final String programUUID, final String templateType) {
        return this.templateServiceMw.getTemplateDTOsByType(programUUID, templateType);
    }

    @Override
    public void deleteTemplate(final String crop, final Integer templateId) {
        this.templateServiceMw.deleteTemplate(templateId);
    }

    @Override
    public void updateTemplate(final String crop, final Integer templateId, final TemplateDTO templateDTO) {
        this.templateDTOValidator.validateUpdateTemplateDTO(templateDTO);
        this.templateServiceMw.updateTemplate(templateDTO);
    }
}
