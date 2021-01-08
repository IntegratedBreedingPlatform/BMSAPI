
package org.ibp.api.java.impl.middleware.study;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.util.TreeViewUtil;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchDTO;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchRequestDTO;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.study.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class StudyServiceImpl implements StudyService {

	@Autowired
	private DatasetService middlewareDatasetService;

	@Autowired
	private org.generationcp.middleware.service.api.study.StudyService middlewareStudyService;

	@Autowired
	org.generationcp.middleware.service.api.SampleService sampleService;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyValidator studyValidator;

	public TrialObservationTable getTrialObservationTable(final int studyIdentifier) {
		return this.middlewareStudyService.getTrialObservationTable(studyIdentifier);
	}

	@Override
	public List<Observation> getObservations(final Integer studyId, final int instanceId, final int pageNumber, final int pageSize,
		final String sortBy, final String sortOrder) {
		final List<ObservationDto> studyMeasurements =
			this.middlewareStudyService.getObservations(studyId, instanceId, pageNumber, pageSize, sortBy, sortOrder);
		final List<Observation> observations = new ArrayList<>();
		for (final ObservationDto measurement : studyMeasurements) {
			observations.add(this.mapObservationDtoToObservation(measurement));
		}
		return observations;
	}

	@Override
	public Observation getSingleObservation(final Integer studyId, final Integer obeservationId) {
		final List<ObservationDto> singleObservation = this.middlewareStudyService.getSingleObservation(studyId, obeservationId);
		if (!singleObservation.isEmpty()) {
			return this.mapObservationDtoToObservation(singleObservation.get(0));
		}
		return new Observation();
	}

	@Override
	public String getProgramUUID(final Integer studyIdentifier) {
		return this.middlewareStudyService.getProgramUUID(studyIdentifier);
	}

	@Override
	public TrialObservationTable getTrialObservationTable(final int studyIdentifier, final Integer studyDbId) {
		return this.middlewareStudyService.getTrialObservationTable(studyIdentifier, studyDbId);
	}

	@Override
	public StudyDetailsDto getStudyDetailsByGeolocation(final Integer geolocationId) {
		return this.middlewareStudyService.getStudyDetailsByInstance(geolocationId);
	}

	@Override
	public List<org.generationcp.middleware.domain.dms.StudySummary> getStudies(final StudySearchFilter studySearchFilter,
		final Pageable pageable) {
		return this.middlewareStudyService.getStudies(studySearchFilter, pageable);
	}

	@Override
	public long countStudies(final StudySearchFilter studySearchFilter) {
		return this.middlewareStudyService.countStudies(studySearchFilter);
	}

	@Override
	public List<PhenotypeSearchDTO> searchPhenotypes(final Integer pageSize, final Integer pageNumber,
		final PhenotypeSearchRequestDTO requestDTO) {
		return this.middlewareStudyService.searchPhenotypes(pageSize, pageNumber, requestDTO);
	}

	@Override
	public long countPhenotypes(final PhenotypeSearchRequestDTO requestDTO) {
		return this.middlewareStudyService.countPhenotypes(requestDTO);
	}

	@Override
	public Boolean isSampled(final Integer studyId) {
		try {
			this.studyValidator.validate(studyId, false);
			return this.sampleService.studyHasSamples(studyId);
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException("an error happened when trying to check if a study is sampled", e);
		}
	}

	@Override
	public List<StudyTypeDto> getStudyTypes() {
		try {
			return this.studyDataManager.getAllVisibleStudyTypes();
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException("an error happened when trying to check if a study is sampled", e);
		}
	}

	@Override
	public StudyReference getStudyReference(final Integer studyId) {
		this.studyValidator.validate(studyId, false);
		return this.studyDataManager.getStudyReference(studyId);
	}

	@Override
	public void updateStudy(final Study study) {
		final int studyId = study.getId();
		this.studyValidator.validate(studyId, false);
		this.studyDataManager.updateStudyLockedStatus(studyId, study.isLocked());
	}

	@Override
	public long countStudyInstances(final StudySearchFilter studySearchFilter) {
		return this.middlewareStudyService.countStudyInstances(studySearchFilter);
	}

	@Override
	public List<StudyInstanceDto> getStudyInstances(final StudySearchFilter studySearchFilter, final Pageable pageable) {
		return this.middlewareStudyService.getStudyInstances(studySearchFilter, pageable);
	}

	@Override
	public List<StudyInstanceDto> getStudyInstancesWithMetadata(final StudySearchFilter studySearchFilter, final Pageable pageable) {
		return this.middlewareStudyService.getStudyInstancesWithMetadata(studySearchFilter, pageable);
	}

	@Override
	public List<TreeNode> getStudyTree(final String parentKey, final String programUUID) {
		List<TreeNode> nodes = new ArrayList<>();
		if (StringUtils.isBlank(parentKey)) {
			final TreeNode rootNode = new TreeNode(AppConstants.STUDIES.name(), AppConstants.STUDIES.getString(), true, null);
			nodes.add(rootNode);
		} else if (parentKey.equals(AppConstants.STUDIES.name())) {
			final List<Reference> children = this.studyDataManager.getRootFolders(programUUID);
			nodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(children, true);
		} else if (NumberUtils.isNumber(parentKey)) {
			final List<Reference> folders = this.studyDataManager.getChildrenOfFolder(Integer.valueOf(parentKey), programUUID);
			nodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(folders, true);
		}
		return nodes;
	}

	@Override
	public Integer getEnvironmentDatasetId(final Integer studyId) {
		final List<DatasetDTO> datasets =
			this.middlewareDatasetService.getDatasets(studyId, Collections.singleton(DatasetTypeEnum.SUMMARY_DATA.getId()));
		if (!CollectionUtils.isEmpty(datasets)) {
			return datasets.get(0).getDatasetId();
		} else {
			throw new ApiRuntimeException("No Environment Dataset by the supplied studyId [" + studyId + "] was found.");
		}
	}

	private Observation mapObservationDtoToObservation(final ObservationDto measurement) {
		return StudyMapper.getInstance()
			.map(measurement, Observation.class);
	}

	public void setStudyDataManager(final StudyDataManager studyDataManager) {
		this.studyDataManager = studyDataManager;
	}


	public void setStudyValidator(final StudyValidator studyValidator) {
		this.studyValidator = studyValidator;
	}

	public void setMiddlewareStudyService(final org.generationcp.middleware.service.api.study.StudyService middlewareStudyService) {
		this.middlewareStudyService = middlewareStudyService;
	}

}
