package org.ibp.api.java.study;

import org.generationcp.middleware.api.study.AdvanceSamplesRequest;
import org.generationcp.middleware.api.study.AdvanceStudyRequest;
import org.generationcp.middleware.ruleengine.pojo.AdvanceGermplasmPreview;

import java.util.List;

public interface AdvanceService {

	/**
	 * @param studyId
	 * @param request
	 * @return a {@link List} of the advanced gids
	 */
	List<Integer> advanceStudy(Integer studyId, AdvanceStudyRequest request);

	/**
	 * @param studyId
	 * @param request
	 * @return a {@link List} of the advanced gids
	 */
	List<AdvanceGermplasmPreview> advanceStudyPreview(Integer studyId, AdvanceStudyRequest request);

	/**
	 * @param studyId
	 * @param request
	 * @return a {@link List} of the advanced gids
	 */
	List<Integer> advanceSamples(Integer studyId, AdvanceSamplesRequest request);

}
