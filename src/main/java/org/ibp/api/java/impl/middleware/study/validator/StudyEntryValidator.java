package org.ibp.api.java.impl.middleware.study.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.study.StudyEntryPropertyDataUpdateRequestDto;
import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.service.api.SampleService;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryService;
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

@Component
public class StudyEntryValidator {

	@Resource
	private GermplasmValidator germplasmValidator;

	@Resource
	private PlantingServiceImpl plantingService;

	@Autowired
	private SampleService sampleService;

	@Autowired
	private StudyValidator studyValidator;

	@Resource
	private StudyEntryService middlewareStudyEntryService;

	private BindingResult errors;

	public void validate(final Integer studyId, final Integer entryId, final Integer newGid) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		if (newGid == null) {
			this.errors.reject("gid.is.required");
		}

		final StudyEntrySearchDto.Filter filter = new StudyEntrySearchDto.Filter();
		filter.setEntryIds(Collections.singletonList(entryId));
		final List<StudyEntryDto> studyEntries =
			this.middlewareStudyEntryService.getStudyEntries(studyId, filter, new PageRequest(0, Integer.MAX_VALUE));

		if (studyEntries.isEmpty()) {
			this.errors.reject("invalid.entryid");
		}

		this.germplasmValidator.validateGermplasmId(this.errors, newGid);

		this.studyValidator.validateStudyHasNoMeansDataset(studyId);

		this.studyValidator.validateHasNoCrossesOrSelections(studyId);

		final Boolean entryHasSamples = this.sampleService.studyEntryHasSamples(studyId, entryId);
		if (entryHasSamples) {
			this.errors.reject("study.entry.has.samples");
		}

		// Check that study has no confirmed or pending transactions for given entry
		final int pendingTransactions =
			this.plantingService.getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.PENDING).size();
		final int confirmedTransactions =
			this.plantingService.getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.CONFIRMED).size();
		if (pendingTransactions > 0 || confirmedTransactions > 0) {
			this.errors.reject("entry.has.pending.or.confirmed.transactions");
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

	public void validateStudyAlreadyHasStudyEntries(final Integer studyId) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (this.middlewareStudyEntryService.countStudyEntries(studyId) > 0) {
			this.errors.reject("study.has.existing.study.entries");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

	public void validateStudyEntriesForUpdate(final StudyEntryPropertyDataUpdateRequestDto requestDto) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		final List<Integer> entriesWithPlot = this.middlewareStudyEntryService.hasPlotEntries(requestDto.getEntryIds());
		if(!CollectionUtils.isEmpty(entriesWithPlot)) {
			this.errors.reject("study.entry.existing.plot.error", new String[] {StringUtils.join(entriesWithPlot, ", ")}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

}
