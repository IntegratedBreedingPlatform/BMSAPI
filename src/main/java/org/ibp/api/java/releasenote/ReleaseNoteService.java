package org.ibp.api.java.releasenote;

import org.generationcp.middleware.domain.releasenote.ReleaseNoteDTO;

public interface ReleaseNoteService {

	ReleaseNoteDTO getLatestReleaseNote();

	void dontShowAgain();

}
