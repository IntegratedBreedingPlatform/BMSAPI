package org.ibp.api.java.impl.middleware;

import org.generationcp.middleware.api.template.TemplateDTO;
import org.ibp.api.java.template.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private org.generationcp.middleware.api.template.TemplateService templateServiceMw;

    @Override
    public TemplateDTO saveTemplate(String crop, TemplateDTO templateDTO) {
        return this.templateServiceMw.saveTemplate(templateDTO);
    }

    @Override
    public List<TemplateDTO> getTemplates (String programUUID, String templateType) {
        return this.templateServiceMw.getTemplateDTOsByType(programUUID, templateType);
    }

    @Override
    public void deleteTemplate(String crop, Integer templateId) {
        this.templateServiceMw.deleteTemplate(templateId);
    }

    @Override
    public void updateTemplate(String crop, Integer templateId, TemplateDTO templateDTO) {
        this.templateServiceMw.updateTemplate(templateDTO);
    }
}
