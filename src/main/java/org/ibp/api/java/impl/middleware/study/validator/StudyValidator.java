package org.ibp.api.java.impl.middleware.study.validator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.generationcp.middleware.service.api.study.StudyInstanceService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ForbiddenException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StudyValidator {
	@Autowired
	private SecurityService securityService;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyInstanceService studyInstanceService;

	@Autowired
	private StudyEntryService studyEntryService;

	@Autowired
	private StudyService studyService;

	private BindingResult errors;

	public void validate(final Integer studyId, final Boolean shouldBeUnlocked) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (studyId == null) {
			this.errors.reject("study.required", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final Study study = this.studyDataManager.getStudy(studyId);

		if (study == null || study.getType() == null) {
			this.errors.reject("study.not.exist", "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}

		if (shouldBeUnlocked) {
			this.checkIfStudyIsLockedForCurrentUser(study);
		}

		// It is assumed that program UUID is always set in ContextHolder beforehand
		final String programUUID = ContextHolder.getCurrentProgram();
		if (programUUID == null) {
			this.errors.reject("study.required", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		if (!programUUID.equals(study.getProgramUUID())) {
			this.errors.reject("invalid.program.uuid.study", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateStudyContainsEntry(final Integer studyId, final Integer entryId) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		final StudyEntrySearchDto.Filter filter = new StudyEntrySearchDto.Filter();
		filter.setEntryIds(Collections.singletonList(entryId));
		final List<StudyEntryDto> studyEntries =
			this.studyEntryService.getStudyEntries(studyId, filter, new PageRequest(0, Integer.MAX_VALUE));

		if (studyEntries.isEmpty()) {
			errors.reject("invalid.entryid");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateStudyContainsEntries(final Integer studyId, final List<Integer> entryIds) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		final StudyEntrySearchDto.Filter filter = new StudyEntrySearchDto.Filter();
		filter.setEntryIds(entryIds);
		final List<StudyEntryDto> studyEntries =
			this.studyEntryService.getStudyEntries(studyId, filter, new PageRequest(0, Integer.MAX_VALUE));

		if (studyEntries.size() != entryIds.size()) {
			final List<Integer> studyEntryIds = studyEntries.stream().map(studyEntry -> studyEntry.getEntryId())
				.collect(Collectors.toList());
			final List<Integer> invalidEntryIds = entryIds.stream().filter(entryId -> !studyEntryIds.contains(entryId))
				.collect(Collectors.toList());
			errors.reject("invalid.entryids", new String[]{StringUtils.join(invalidEntryIds, ", ")}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void checkIfStudyIsLockedForCurrentUser(final Integer studyId) {
		final Study study = this.studyDataManager.getStudy(studyId);
		this.checkIfStudyIsLockedForCurrentUser(study);
	}

	private void checkIfStudyIsLockedForCurrentUser(final Study study) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();

		if (study.isLocked()
			&& !study.getCreatedBy().equals(loggedInUser.getUserid().toString())
			&& !loggedInUser.isSuperAdmin()) {
			this.errors.reject("study.is.locked", "");
			throw new ForbiddenException(this.errors.getAllErrors().get(0));
		}

	}

	public void validate(final Integer studyId, final Boolean shouldBeUnlocked, final Boolean allInstancesShouldBeDeletable) {
		this.validate(studyId, shouldBeUnlocked);

		if (allInstancesShouldBeDeletable) {
			final List<StudyInstance> studyInstances = this.studyInstanceService.getStudyInstances(studyId);
			final List<Integer> restrictedInstances =
				studyInstances.stream().filter(instance -> BooleanUtils.isFalse(instance.getCanBeDeleted()))
					.map(instance -> instance.getInstanceNumber()).collect(Collectors.toList());
			if (!restrictedInstances.isEmpty()) {
				this.errors.reject("at.least.one.instance.cannot.be.deleted");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}
	}

	public void validateStudyShouldNotHaveObservation(final Integer studyId) {
		final List<StudyInstance> studyInstances = this.studyInstanceService.getStudyInstances(studyId);
		final List<Integer> restrictedInstances =
			studyInstances.stream().filter(instance -> BooleanUtils.isTrue(instance.isHasExperimentalDesign()))
				.map(instance -> instance.getInstanceNumber()).collect(Collectors.toList());
		if (!restrictedInstances.isEmpty()) {
			this.errors.reject("study.must.not.have.observation");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

	public void validateHasNoCrossesOrSelections(final Integer studyId) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		if (this.studyService.hasCrossesOrSelections(studyId)) {
			errors.reject("study.has.crosses.or.selections");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateStudyHasNoMeansDataset(final Integer studyId) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		if (this.studyService.studyHasGivenDatasetType(studyId, DatasetTypeEnum.MEANS_DATA.getId())) {
			errors.reject("study.has.means.dataset");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

}
