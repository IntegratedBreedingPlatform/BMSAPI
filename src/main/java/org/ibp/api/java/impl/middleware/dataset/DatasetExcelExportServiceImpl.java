package org.ibp.api.java.impl.middleware.dataset;

import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetExportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class DatasetExcelExportServiceImpl extends AbstractDatasetExportService implements DatasetExportService {

	@Resource
	private DatasetXLSGenerator datasetXLSGenerator;

	@Resource
	private org.generationcp.middleware.service.api.dataset.DatasetService datasetService;

	@Override
	public File export(final int studyId, final int datasetId, final Set<Integer> instanceIds, final int collectionOrderId, final boolean singleFile) {

		this.validate(studyId, datasetId, instanceIds);

		final Study study = this.studyDataManager.getStudy(studyId);
		final DatasetDTO dataSet = this.datasetService.getDataset(datasetId);
		final List<StudyInstance> selectedDatasetInstances = this.getSelectedDatasetInstances(dataSet.getInstances(), instanceIds);

		try {
			return this.generateFiles(study, dataSet, selectedDatasetInstances, collectionOrderId, this.datasetXLSGenerator, XLS);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("cannot.exportAsXLS.dataset", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}
}
