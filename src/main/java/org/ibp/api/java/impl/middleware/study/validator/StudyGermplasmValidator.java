package org.ibp.api.java.impl.middleware.study.validator;

import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.service.api.SampleService;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.generationcp.middleware.service.impl.inventory.PlantingServiceImpl;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
public class StudyGermplasmValidator {

	@Resource
	private GermplasmValidator germplasmValidator;

	@Resource
	private PlantingServiceImpl plantingService;

	@Autowired
	private SampleService sampleService;

	@Autowired
	private StudyValidator studyValidator;

	@Resource
	private org.generationcp.middleware.service.api.study.StudyGermplasmService middlewareStudyGermplasmService;

	private BindingResult errors;

	public void validate(final Integer studyId, final Integer entryId, final Integer newGid) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		if (newGid == null) {
			errors.reject("gid.is.required");
		}

		final StudyEntrySearchDto.Filter filter = new StudyEntrySearchDto.Filter();
		filter.setEntryIds(Collections.singletonList(entryId));
		final List<StudyEntryDto> studyEntries =
			this.middlewareStudyGermplasmService.getStudyEntries(studyId, filter, new PageRequest(0, Integer.MAX_VALUE));

		if (studyEntries.isEmpty()) {
			errors.reject("invalid.entryid");
		}

		this.germplasmValidator.validateGermplasmId(this.errors, newGid);

		studyValidator.validateStudyHasNoMeansDataset(studyId);

		studyValidator.validateHasNoCrossesOrSelections(studyId);

		Boolean entryHasSamples = this.sampleService.studyEntryHasSamples(studyId, entryId);
		if (entryHasSamples) {
			errors.reject("study.entry.has.samples");
		}

		// Check that study has no confirmed or pending transactions for given entry
		final Integer pendingTransactions =
			this.plantingService.getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.PENDING).size();
		final Integer confirmedTransactions =
			this.plantingService.getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.CONFIRMED).size();
		if (pendingTransactions > 0 || confirmedTransactions > 0) {
			errors.reject("entry.has.pending.or.confirmed.transactions");
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

	public void validateStudyAlreadyHasStudyEntries(final Integer studyId) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (this.middlewareStudyGermplasmService.countStudyEntries(studyId) > 0) {
			errors.reject("study.has.existing.study.entries");
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

	public void validateStudyEntryProperty(final Integer studyEntryPropertyDataId) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final Optional<StudyEntryPropertyData> studyEntryPropertyData =
			this.middlewareStudyGermplasmService.getStudyEntryPropertyData(studyEntryPropertyDataId);
		if (!studyEntryPropertyData.isPresent()) {
			errors.reject("invalid.study.entry.property.data.id");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

}
