package org.ibp.api.java.impl.middleware.study.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.domain.dataset.PlotDatasetPropertiesDTO;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.study.StudyInstanceService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.api.user.UserService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ForbiddenException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class StudyValidator {

	public final static long MAX_NUMBER_OF_COLUMNS_ALLOWED = 30;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyInstanceService studyInstanceService;

	@Autowired
	private StudyService studyService;

	@Autowired
	private UserService userService;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private DatasetService datasetService;

	private BindingResult errors;

	public void validate(final Integer studyId, final Boolean shouldBeUnlocked) {

		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

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

	public void checkIfStudyIsLockedForCurrentUser(final Integer studyId) {
		final Study study = this.studyDataManager.getStudy(studyId);
		this.checkIfStudyIsLockedForCurrentUser(study);
	}

	private void checkIfStudyIsLockedForCurrentUser(final Study study) {

		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
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
					.map(StudyInstance::getInstanceNumber).collect(Collectors.toList());
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
				.map(StudyInstance::getInstanceNumber).collect(Collectors.toList());
		if (!restrictedInstances.isEmpty()) {
			this.errors.reject("study.must.not.have.observation");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

	public void validateHasNoCrossesOrSelections(final Integer studyId) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		if (this.studyService.hasCrossesOrSelections(studyId)) {
			this.errors.reject("study.has.crosses.or.selections");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateStudyHasNoMeansDataset(final Integer studyId) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		if (this.studyService.studyHasGivenDatasetType(studyId, DatasetTypeEnum.MEANS_DATA.getId())) {
			this.errors.reject("study.has.means.dataset");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateStudyHasNoSummaryStatisticsDataset(final Integer studyId) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		if (this.studyService.studyHasGivenDatasetType(studyId, DatasetTypeEnum.SUMMARY_STATISTICS_DATA.getId())) {
			this.errors.reject("study.has.summary.statistics.dataset");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateDeleteStudy(final Integer studyId) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		final Study study = this.studyDataManager.getStudy(studyId, false);

		if (study == null) {
			this.errors.reject("study.not.exist", "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}

		if (StringUtils.isBlank(study.getProgramUUID())) {
			this.errors.reject("study.template.delete.not.permitted");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final Integer studyUserId = study.getUser();
		final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();
		if (!user.getUserid().equals(studyUserId)) {
			final WorkbenchUser workbenchUser = this.userService.getUserById(studyUserId);
			this.errors.reject("study.delete.not.permitted", new String[] {workbenchUser.getPerson().getDisplayName()}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());

		}
	}

	public void validateStudyInstanceNumbers(final Integer studyId, final Set<Integer> instanceNumbers) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		final Set<Integer>
			existingInstanceNumbers =
			this.studyDataManager.getInstanceGeolocationIdsMap(studyId).keySet().stream().map(Integer::valueOf)
				.collect(Collectors.toSet());
		final Collection<Integer> nonExistingInstanceNumbers =
			CollectionUtils.subtract(instanceNumbers, existingInstanceNumbers);
		if (CollectionUtils.isNotEmpty(nonExistingInstanceNumbers)) {
			this.errors.reject("study.trial.instances.do.not.exist",
				new Object[] {org.apache.commons.lang.StringUtils.join(nonExistingInstanceNumbers, ", ")}, "");
		}
		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public DatasetDTO validateStudyHasPlotDataset(final Integer studyId) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

		final List<DatasetDTO> dataSets =
			this.datasetService.getDatasets(studyId, Collections.singleton(DatasetTypeEnum.PLOT_DATA.getId()));
		if (CollectionUtils.isEmpty(dataSets)) {
			this.errors.reject("study.not.has.plot.dataset", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		return dataSets.get(0);
	}

	public void validateUpdateStudyEntryColumnsWithSupportedVariableTypes(final List<Integer> variableIds, final String programUUID) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

		final List<String> unsupportedVariableIds = this.ontologyDataManager.getStandardVariables(variableIds, programUUID)
			.stream()
			.filter(standardVariable -> standardVariable.getVariableTypes().stream().noneMatch(variableType ->
				VariableType.GERMPLASM_DESCRIPTOR == variableType ||
					VariableType.GERMPLASM_ATTRIBUTE == variableType ||
					VariableType.GERMPLASM_PASSPORT == variableType)
			)
			.map(standardVariable -> String.valueOf(standardVariable.getId()))
			.collect(Collectors.toList());
		if (!org.springframework.util.CollectionUtils.isEmpty(unsupportedVariableIds)) {
			this.errors.reject("study.entry.update.columns.unsupported.variables", new String[] {String.join(", ", unsupportedVariableIds)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateMaxStudyEntryColumnsAllowed(final PlotDatasetPropertiesDTO plotDatasetPropertiesDTO, final String programUUID) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

		final List<StandardVariable> variables = this.ontologyDataManager.getStandardVariables(plotDatasetPropertiesDTO.getVariableIds(), programUUID)
			.stream()
			.filter(standardVariable -> standardVariable.getVariableTypes().stream().anyMatch(variableType ->
				VariableType.GERMPLASM_ATTRIBUTE == variableType ||
					VariableType.GERMPLASM_PASSPORT == variableType)
			)
			.collect(Collectors.toList());

		final int total = variables.size() + plotDatasetPropertiesDTO.getNameTypeIds().size();
		if (total > MAX_NUMBER_OF_COLUMNS_ALLOWED) {
			this.errors.reject("study.entry.update.columns.exceeded.maximum.allowed",
				new String[] { String.valueOf(variables.size()), String.valueOf(MAX_NUMBER_OF_COLUMNS_ALLOWED) },"");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
