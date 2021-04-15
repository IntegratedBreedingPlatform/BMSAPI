package org.ibp.api.rest.releasenote;

import org.generationcp.middleware.domain.releasenote.ReleaseNoteDTO;
import org.ibp.api.java.releasenote.ReleaseNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/release-notes")
public class ReleaseNoteResource {

	@Autowired
	private ReleaseNoteService releaseNoteService;

	@RequestMapping(
		value = "/latest",
		method = RequestMethod.GET)
	public ResponseEntity<ReleaseNoteDTO> getLatest() {
		final ReleaseNoteDTO latestReleaseNote = this.releaseNoteService.getLatestReleaseNote();
		return new ResponseEntity<>(latestReleaseNote, HttpStatus.OK);
	}

	@RequestMapping(
		value = "/dont-show-again",
		method = RequestMethod.PUT
	)
	public ResponseEntity<Void> dontShowAgain() {
		this.releaseNoteService.dontShowAgain();
		return new ResponseEntity<>(HttpStatus.OK);
	}

}