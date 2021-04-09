package org.ibp.api.java.impl.middleware.releasenote;

import org.generationcp.middleware.domain.releasenote.ReleaseNoteDTO;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.releasenote.ReleaseNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReleaseNoteServiceImpl implements ReleaseNoteService {

	@Autowired
	private org.generationcp.middleware.service.api.releasenote.ReleaseNoteService releaseNoteService;

	@Autowired
	private SecurityService securityService;

	@Override
	public ReleaseNoteDTO getLatestReleaseNote() {
		return this.releaseNoteService.getLatestReleaseNote()
			.map(releaseNote -> new ReleaseNoteDTO(releaseNote.getId(), releaseNote.getVersion(), releaseNote.getReleaseDate()))
			.orElseGet(ReleaseNoteDTO::new);
	}

	@Override
	public void dontShowAgain() {
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		this.releaseNoteService.dontShowAgain(loggedInUser.getUserid());
	}

}
