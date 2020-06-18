package org.ibp.api.rest.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.java.study.StudyGermplasmService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(value = "Study Germplasm Services")
@Controller
@RequestMapping("/crops")
public class StudyGermplasmResource {

    @Resource
    private StudyGermplasmService studyGermplasmService;

    @ApiOperation(value = "Replace germplasm entry in study",
            notes = "Replace germplasm entry in study")
    @RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/germplasm/{entryId}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<StudyGermplasmDto> replaceStudyGermplasm(final @PathVariable String cropname, @PathVariable final String programUUID,
                                                                   @PathVariable final Integer studyId, @PathVariable final Integer entryId, @RequestBody final StudyGermplasmDto studyGermplasmDto) {
        return new ResponseEntity<>(this.studyGermplasmService.replaceStudyGermplasm(studyId, entryId, studyGermplasmDto),
                HttpStatus.OK);

    }
}
