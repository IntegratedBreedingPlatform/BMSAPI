package org.ibp.api.java.feedback;

import org.generationcp.middleware.api.feedback.FeedbackDto;
import org.generationcp.middleware.pojos.workbench.feedback.FeedbackFeature;

import java.util.Optional;

public interface FeedbackService {

	boolean shouldShowFeedback(FeedbackFeature feature);

	void dontShowAgain(FeedbackFeature feature);

	Optional<FeedbackDto> getFeedback(FeedbackFeature feature);
}
