package org.ibp.api.rest.feedback;

import org.generationcp.middleware.api.feedback.FeedbackDto;
import org.generationcp.middleware.pojos.workbench.feedback.FeedbackFeature;
import org.ibp.api.java.feedback.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/feedback")
public class FeedbackResource {

	@Autowired
	private FeedbackService feedbackService;

	@ApiIgnore
	@RequestMapping(value = "/{feature}/should-show", method = RequestMethod.GET)
	public ResponseEntity<Boolean> shouldShowFeedback(@PathVariable final FeedbackFeature feature) {
		final boolean shouldShowFeedback = this.feedbackService.shouldShowFeedback(feature);
		return new ResponseEntity<>(shouldShowFeedback, HttpStatus.OK);
	}

	@ApiIgnore
	@RequestMapping(value = "/{feature}/dont-show-again", method = RequestMethod.PUT)
	public ResponseEntity<Void> dontShowAgain(@PathVariable final FeedbackFeature feature) {
		this.feedbackService.dontShowAgain(feature);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiIgnore
	@RequestMapping(value = "/{feature}", method = RequestMethod.GET)
	public ResponseEntity<FeedbackDto> getFeedBack(@PathVariable final FeedbackFeature feature) {
		final FeedbackDto feebackDto = this.feedbackService.getFeedback(feature);
		if (feebackDto != null) {
			return new ResponseEntity<>(feebackDto, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

}
