package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.java.study.StudyInstanceService;

import javax.annotation.Resource;

public class StudyInstanceServiceImpl implements StudyInstanceService {

	@Resource
	private org.generationcp.middleware.service.api.study.StudyInstanceService studyInstanceService;

	@Resource
	private WorkbenchDataManager workbenchDataManager;

	@Resource
	private StudyValidator studyValidator;

	@Resource
	private DatasetValidator datasetValidator;

	@Override
	public void createStudyInstance(final String cropName, final Integer studyId, final Integer datasetId, final String instanceNumber) {

		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDatasetBelongsToStudy(studyId, datasetId);

		final CropType cropType = this.workbenchDataManager.getCropTypeByName(cropName);

		this.studyInstanceService.createStudyInstance(cropType, datasetId, instanceNumber);
	}

	@Override
	public void removeStudyInstance(final String cropName, final Integer studyId, final Integer datasetId, final String instanceNumber) {

		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDatasetBelongsToStudy(studyId, datasetId);

		final CropType cropType = this.workbenchDataManager.getCropTypeByName(cropName);

		this.studyInstanceService.removeStudyInstance(cropType, datasetId, instanceNumber);

	}
}
