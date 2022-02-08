package org.ibp.api.java.feedback;

import org.generationcp.middleware.pojos.workbench.feedback.FeedbackFeature;

public interface FeedbackService {

	boolean shouldShowFeedback(FeedbackFeature feature);

	void dontShowAgain(FeedbackFeature feature);

}
