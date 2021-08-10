package org.ibp.api.java.impl.middleware.study.brapi;

import org.generationcp.middleware.api.brapi.v2.study.StudyImportRequestDTO;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.ibp.api.brapi.StudyServiceBrapi;
import org.ibp.api.brapi.v2.study.StudyImportResponse;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.impl.middleware.study.validator.StudyImportRequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudyServiceBrapiImpl implements StudyServiceBrapi {

	@Resource
	private org.generationcp.middleware.api.brapi.StudyServiceBrapi middlewareStudyServiceBrapi;

	@Autowired
	private StudyImportRequestValidator studyImportRequestValidator;

	@Autowired
	private SecurityService securityService;

	@Override
	public Optional<StudyDetailsDto> getStudyDetailsByInstance(final Integer instanceId) {
		return this.middlewareStudyServiceBrapi.getStudyDetailsByInstance(instanceId);
	}

	@Override
	public long countStudyInstances(final StudySearchFilter studySearchFilter) {
		return this.middlewareStudyServiceBrapi.countStudyInstances(studySearchFilter);
	}

	@Override
	public List<StudyInstanceDto> getStudyInstances(final StudySearchFilter studySearchFilter, final Pageable pageable) {
		return this.middlewareStudyServiceBrapi.getStudyInstances(studySearchFilter, pageable);
	}

	@Override
	public List<StudyInstanceDto> getStudyInstancesWithMetadata(final StudySearchFilter studySearchFilter, final Pageable pageable) {
		return this.middlewareStudyServiceBrapi.getStudyInstancesWithMetadata(studySearchFilter, pageable);
	}

	@Override
	public StudyImportResponse createStudies(final String cropName, final List<StudyImportRequestDTO> studyImportRequestDTOS) {
		final StudyImportResponse response = new StudyImportResponse();
		final int originalListSize = studyImportRequestDTOS.size();
		int noOfCreatedStudies = 0;

		// Remove studies that fails any validation. They will be excluded from creation
		final BindingResult bindingResult = this.studyImportRequestValidator.pruneStudiesInvalidForImport(studyImportRequestDTOS);
		if (bindingResult.hasErrors()) {
			response.setErrors(bindingResult.getAllErrors());
		}
		if (!CollectionUtils.isEmpty(studyImportRequestDTOS)) {

			final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();
			final List<StudyInstanceDto> instances =
				this.middlewareStudyServiceBrapi.saveStudyInstances(cropName, studyImportRequestDTOS, user.getUserid());
			if (!CollectionUtils.isEmpty(instances)) {
				noOfCreatedStudies = instances.size();
			}
			response.setEntityList(instances);
		}
		response.setCreatedSize(noOfCreatedStudies);
		response.setImportListSize(originalListSize);
		return response;
	}
}
