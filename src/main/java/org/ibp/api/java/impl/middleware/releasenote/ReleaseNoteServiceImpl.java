package org.ibp.api.java.impl.middleware.releasenote;

import org.generationcp.middleware.domain.releasenote.ReleaseNoteDTO;
import org.ibp.api.java.releasenote.ReleaseNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReleaseNoteServiceImpl implements ReleaseNoteService {

	@Autowired
	private org.generationcp.middleware.service.api.releasenote.ReleaseNoteService releaseNoteService;

	@Override
	public ReleaseNoteDTO getLatestReleaseNote() {
		return this.releaseNoteService.getLatestReleaseNote()
			.map(releaseNote -> new ReleaseNoteDTO(releaseNote.getId(), releaseNote.getVersion(), releaseNote.getReleaseDate()))
			.orElseGet(ReleaseNoteDTO::new);
	}

}
