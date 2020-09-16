package org.ibp.api.rest.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.java.study.StudyGermplasmService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

// TODO: Move these services to StudyResource
@Api(value = "Study Germplasm Services")
@Controller
@RequestMapping("/crops")
public class StudyGermplasmResource {

	@Resource
	private StudyGermplasmService studyGermplasmService;

	@ApiOperation(value = "Replace germplasm entry in study",
		notes = "Replace germplasm entry in study")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/entries/{entryId}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<StudyGermplasmDto> replaceStudyGermplasm(final @PathVariable String cropname,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId, @PathVariable final Integer entryId, @RequestBody final StudyGermplasmDto studyGermplasmDto) {
		return new ResponseEntity<>(this.studyGermplasmService.replaceStudyEntry(studyId, entryId, studyGermplasmDto),
			HttpStatus.OK);

	}

	@ApiOperation(value = "Create germplasm entries in study based on the specified germplasm list",
		notes = "Create germplasm entries in study based on the specified germplasm list")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/entries/generation", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<List<StudyGermplasmDto>> createStudyEntries(final @PathVariable String cropname,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId, @RequestBody final GermplasmEntryRequestDto germplasmEntryRequestDto) {

		return new ResponseEntity<>(
			this.studyGermplasmService.createStudyEntries(studyId, germplasmEntryRequestDto.getGermplasmListId()),
			HttpStatus.OK);

	}

	@ApiOperation(value = "Delete germplasm entries in study",
		notes = "Delete germplasm entries in study")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/entries", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteStudyEntries(final @PathVariable String cropname,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId) {

		this.studyGermplasmService.deleteStudyEntries(studyId);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);

	}

	@ApiOperation(value = "Update germplasm entry property",
		notes = "Update germplasm entry property")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/entries/{entryId}/properties/{propertyId}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateStudyEntryProperty(final @PathVariable String cropname,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId, @PathVariable final Integer entryId, @PathVariable final Integer propertyId,
		@RequestBody StudyEntryPropertyData studyEntryPropertyData) {

		this.studyGermplasmService.updateStudyEntryProperty(studyId, entryId, studyEntryPropertyData);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);

	}

}
