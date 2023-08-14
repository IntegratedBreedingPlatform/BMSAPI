package org.ibp.api.rest.template;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.template.TemplateDTO;
import org.ibp.api.java.template.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "Template Services")
@PreAuthorize("hasAnyAuthority("
        + "'ADMIN',"
        + "'STUDIES',"
        + "'MANAGE_STUDIES',"
        + "'MS_STUDY_ACTIONS',"
        + "'MS_ADVANCES',"
        + "'MS_ADVANCE_STUDY',"
        + "'MS_ADVANCE_STUDY_FOR_PLANTS'"
        + ")")
@RestController
public class TemplateResource {

    @Autowired
    private TemplateService templateService;

    @RequestMapping(value = "/crops/{cropname}/programs/{programUUID}/templates", method = RequestMethod.PUT)
    @ApiOperation(value = "Create a new Template",
            notes = "Create a new Template.")
    @ResponseBody
    public ResponseEntity<TemplateDTO> createTemplate(
            @PathVariable final String cropname, @PathVariable final String programUUID,
            @RequestBody TemplateDTO templateDTO) {
        templateDTO = this.templateService.saveTemplate(cropname, templateDTO);
        return new ResponseEntity<>(templateDTO, HttpStatus.OK);
    }

    @RequestMapping(value = "/crops/{cropname}/programs/{programUUID}/templates/{templateId}", method = RequestMethod.PUT)
    @ApiOperation(value = "Update a existing Template",
            notes = "Update a existing Template.")
    @ResponseBody
    public ResponseEntity<Void> updateTemplate(
            @PathVariable final String cropname, @PathVariable final String programUUID, @PathVariable final Integer templateId,
            @RequestBody final TemplateDTO templateDTO) {
        this.templateService.updateTemplate(cropname, templateId, templateDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/crops/{cropname}/programs/{programUUID}/templates", method = RequestMethod.GET)
    @ApiOperation(value = "Get templates",
            notes = "Get templates.")
    @ResponseBody
    public ResponseEntity<List<TemplateDTO>> getTemplates(
            @PathVariable final String cropname,
            @PathVariable final String programUUID,
            @RequestParam final String templateType) {
        final List<TemplateDTO> templateDTOS = this.templateService.getTemplates(programUUID, templateType);
        return new ResponseEntity<>(templateDTOS, HttpStatus.OK);
    }

    @RequestMapping(value = "/crops/{cropname}/programs/{programUUID}/templates/{templateId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete Template",
            notes = "Delete Template.")
    @ResponseBody
    public ResponseEntity<Void> deleteTemplate(
            @PathVariable final String cropname,
            @PathVariable final String programUUID,
            @PathVariable final Integer templateId) {
        this.templateService.deleteTemplate(cropname, templateId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
