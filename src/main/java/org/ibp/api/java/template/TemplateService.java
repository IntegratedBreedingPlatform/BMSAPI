package org.ibp.api.java.template;

import org.generationcp.middleware.api.template.TemplateDTO;

import java.util.List;

public interface TemplateService {

    TemplateDTO saveTemplate(String crop, TemplateDTO templateDTO);

    List<TemplateDTO> getTemplates (String programUUID, String templateType);

    void deleteTemplate(String crop, Integer templateId);

    void updateTemplate(String crop, Integer templateId, TemplateDTO templateDTO);
}
