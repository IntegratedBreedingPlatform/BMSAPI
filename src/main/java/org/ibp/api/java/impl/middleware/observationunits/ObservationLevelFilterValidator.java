package org.ibp.api.java.impl.middleware.observationunits;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.StudyServiceBrapi;
import org.generationcp.middleware.api.brapi.TrialServiceBrapi;
import org.generationcp.middleware.api.brapi.v2.observationlevel.ObservationLevelFilter;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Component
public class ObservationLevelFilterValidator {

	@Autowired
	private ProgramService programService;

	@Autowired
	private StudyServiceBrapi studyServiceBrapi;

	@Autowired
	private TrialServiceBrapi trialServiceBrapi;

	public void validate(final ObservationLevelFilter filter, final String crop) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		if (StringUtils.isNotEmpty(filter.getProgramDbId())) {
			final Project project = this.programService.getProjectByUuidAndCrop(filter.getProgramDbId(), crop);
			if (project == null) {
				errors.reject("observation.level.invalid.programdbid", new String[] {}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}
		if (StringUtils.isNotEmpty(filter.getStudyDbId())) {
			final StudySearchFilter studySearchFilter = new StudySearchFilter();
			studySearchFilter.setStudyDbIds(Collections.singletonList(filter.getStudyDbId()));
			final List<StudyInstanceDto> studyInstances = this.studyServiceBrapi.getStudyInstances(studySearchFilter, null);
			if (CollectionUtils.isEmpty(studyInstances)) {
				errors.reject("studydbid.invalid", new String[] {}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
			final StudyInstanceDto instanceDto = studyInstances.get(0);
			if ((StringUtils.isNotEmpty(filter.getTrialDbId()) && !instanceDto.getTrialDbId().equals(filter.getTrialDbId()))
				|| (StringUtils.isNotEmpty(filter.getProgramDbId()) && !instanceDto.getProgramDbId().equals(filter.getProgramDbId()))) {
				errors.reject("observation.level.invalid", new String[] {}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
			//Set trialDbId if null
			if(StringUtils.isEmpty(filter.getTrialDbId())) {
				filter.setTrialDbId(instanceDto.getTrialDbId());
			}
		} else if (StringUtils.isNotEmpty(filter.getTrialDbId())) {
			final StudySearchFilter studySearchFilter = new StudySearchFilter();
			studySearchFilter.setTrialDbIds(Collections.singletonList(filter.getTrialDbId()));
			final List<StudySummary> studySummaries = this.trialServiceBrapi.getStudies(studySearchFilter, null);
			if (CollectionUtils.isEmpty(studySummaries)) {
				errors.reject("observation.level.invalid.trialdbid", new String[] {}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
			final StudySummary studySummary = studySummaries.get(0);
			if ((StringUtils.isNotEmpty(filter.getProgramDbId()) && !studySummary.getProgramDbId().equals(filter.getProgramDbId()))) {
				errors.reject("observation.level.invalid", new String[] {}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}
	}

}
