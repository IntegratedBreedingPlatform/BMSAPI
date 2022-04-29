package org.ibp.api.java.impl.middleware.feedback;

import org.generationcp.middleware.api.feedback.FeedbackDto;
import org.generationcp.middleware.pojos.workbench.feedback.Feedback;
import org.generationcp.middleware.pojos.workbench.feedback.FeedbackFeature;
import org.ibp.api.java.feedback.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class FeedbackServiceImpl implements FeedbackService {

	@Autowired
	private org.generationcp.middleware.service.api.feedback.FeedbackService feedbackService;

	@Override
	public boolean shouldShowFeedback(final FeedbackFeature feature) {
		return this.feedbackService.shouldShowFeedback(feature);
	}

	@Override
	public void dontShowAgain(final FeedbackFeature feature) {
		this.feedbackService.dontShowAgain(feature);
	}

	@Override
	public Optional<FeedbackDto> getFeedback(final FeedbackFeature feature) {
		Optional<Feedback> feedback = this.feedbackService.getFeedback(feature);
		return Optional.ofNullable(new FeedbackDto(feedback.get()));
	}

}
