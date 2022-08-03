package org.ibp.api.java.impl.middleware.observationunits;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.StudyServiceBrapi;
import org.generationcp.middleware.api.brapi.TrialServiceBrapi;
import org.generationcp.middleware.api.brapi.v2.observationlevel.ObservationLevel;
import org.generationcp.middleware.api.brapi.v2.observationlevel.ObservationLevelFilter;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitImportRequestDto;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitDto;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitSearchRequestDTO;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.ibp.api.brapi.v2.observationunits.ObservationUnitImportResponse;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.observationunits.ObservationUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Transactional
@Service
public class ObservationUnitServiceImpl implements ObservationUnitService {

	@Autowired
	private ObservationUnitImportRequestValidator observationUnitImportRequestValidator;

	@Autowired
	private org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitService middlewareObservationUnitService;

	@Autowired
	private ProgramService programService;

	@Autowired
	private StudyServiceBrapi studyServiceBrapi;

	@Autowired
	private TrialServiceBrapi trialServiceBrapi;

	@Override
	public ObservationUnitImportResponse createObservationUnits(final String cropName,
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos) {
		final ObservationUnitImportResponse response = new ObservationUnitImportResponse();
		final int originalListSize = observationUnitImportRequestDtos.size();
		int noOfCreatedObservationUnits = 0;

		// Remove observation units that fails any validation. They will be excluded from creation
		final BindingResult bindingResult =
			this.observationUnitImportRequestValidator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		if (bindingResult.hasErrors()) {
			response.setErrors(bindingResult.getAllErrors());
		}

		if (!CollectionUtils.isEmpty(observationUnitImportRequestDtos)) {
			final List<String> observationUnitDbIds =
				this.middlewareObservationUnitService.importObservationUnits(cropName, observationUnitImportRequestDtos);
			List<ObservationUnitDto> observationUnitDtos = new ArrayList<>();
			if (!CollectionUtils.isEmpty(observationUnitDbIds)) {
				noOfCreatedObservationUnits = observationUnitDbIds.size();
				final ObservationUnitSearchRequestDTO searchRequestDTO = new ObservationUnitSearchRequestDTO();
				searchRequestDTO.setObservationUnitDbIds(observationUnitDbIds);
				observationUnitDtos = this.middlewareObservationUnitService.searchObservationUnits(null, null, searchRequestDTO);

			}
			response.setEntityList(observationUnitDtos);
		}

		response.setImportListSize(originalListSize);
		response.setCreatedSize(noOfCreatedObservationUnits);
		return response;
	}

	@Override
	public List<ObservationUnitDto> searchObservationUnits(final Integer pageSize, final Integer pageNumber,
		final ObservationUnitSearchRequestDTO requestDTO) {
		return this.middlewareObservationUnitService.searchObservationUnits(pageSize, pageNumber, requestDTO);
	}

	@Override
	public long countObservationUnits(final ObservationUnitSearchRequestDTO requestDTO) {
		return this.middlewareObservationUnitService.countObservationUnits(requestDTO);
	}

	@Override
	public List<ObservationLevel> getObservationLevels(ObservationLevelFilter observationLevelFilter, final String crop) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		if (StringUtils.isNotEmpty(observationLevelFilter.getProgramDbId())) {
			final Project project = this.programService.getProjectByUuidAndCrop(observationLevelFilter.getProgramDbId(), crop);
			if (project == null) {
				errors.reject("observation.level.invalid.programdbid", new String[] {}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}
		if (StringUtils.isNotEmpty(observationLevelFilter.getStudyDbId())) {
			final StudySearchFilter studySearchFilter = new StudySearchFilter();
			studySearchFilter.setStudyDbIds(Collections.singletonList(observationLevelFilter.getStudyDbId()));
			final List<StudyInstanceDto> studyInstances = this.studyServiceBrapi.getStudyInstances(studySearchFilter, null);
			if (CollectionUtils.isEmpty(studyInstances)) {
				errors.reject("studydbid.invalid", new String[] {}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
			final StudyInstanceDto instanceDto = studyInstances.get(0);
			if ((StringUtils.isNotEmpty(observationLevelFilter.getTrialDbId()) && !instanceDto.getTrialDbId().equals(observationLevelFilter.getTrialDbId()))
				|| (StringUtils.isNotEmpty(observationLevelFilter.getProgramDbId()) && !instanceDto.getProgramDbId().equals(observationLevelFilter.getProgramDbId()))) {
				errors.reject("observation.level.invalid", new String[] {}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
			//Set trialDbId if null
			if(StringUtils.isEmpty(observationLevelFilter.getTrialDbId())) {
				observationLevelFilter.setTrialDbId(instanceDto.getTrialDbId());
			}
		} else if (StringUtils.isNotEmpty(observationLevelFilter.getTrialDbId())) {
			final StudySearchFilter studySearchFilter = new StudySearchFilter();
			studySearchFilter.setTrialDbIds(Collections.singletonList(observationLevelFilter.getTrialDbId()));
			final List<StudySummary> studySummaries = this.trialServiceBrapi.getStudies(studySearchFilter, null);
			if (CollectionUtils.isEmpty(studySummaries)) {
				errors.reject("observation.level.invalid.trialdbid", new String[] {}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
			final StudySummary studySummary = studySummaries.get(0);
			if ((StringUtils.isNotEmpty(observationLevelFilter.getProgramDbId()) && !studySummary.getProgramDbId().equals(observationLevelFilter.getProgramDbId()))) {
				errors.reject("observation.level.invalid", new String[] {}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}
		return this.middlewareObservationUnitService.getObservationLevels(observationLevelFilter);
	}

}
