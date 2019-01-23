package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.io.Files;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.ZipUtil;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetCollectionOrderService;
import org.ibp.api.java.dataset.DatasetExportService;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class DatasetExcelExportServiceImpl implements DatasetExportService {

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private DatasetValidator datasetValidator;

	@Autowired
	private InstanceValidator instanceValidator;

	@Autowired
	private DatasetService studyDatasetService;

	@Autowired
	private DatasetCollectionOrderService datasetCollectionOrderService;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private DatasetXLSGenerator datasetXLSGenerator;

	@Resource
	private org.generationcp.middleware.service.api.dataset.DatasetService datasetService;

	private final ZipUtil zipUtil = new ZipUtil();

	private void validate(final int studyId, final int datasetId, final Set<Integer> instanceIds) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		this.instanceValidator.validate(datasetId, instanceIds);
	}

	protected List<StudyInstance> getSelectedDatasetInstances(final List<StudyInstance> studyInstances, final Set<Integer> instanceIds) {
		final Iterator<StudyInstance> iterator = studyInstances.iterator();
		while (iterator.hasNext()) {
			final StudyInstance studyInstance = iterator.next();
			if (!instanceIds.contains(studyInstance.getInstanceDbId())) {
				iterator.remove();
			}
		}
		return studyInstances;
	}

	@Override
	public File export(final int studyId, final int datasetId, final Set<Integer> instanceIds, final int collectionOrderId) {

		this.validate(studyId, datasetId, instanceIds);

		final Study study = this.studyDataManager.getStudy(studyId);
		final DatasetDTO dataSet = this.datasetService.getDataset(datasetId);
		final List<StudyInstance> selectedDatasetInstances = this.getSelectedDatasetInstances(dataSet.getInstances(), instanceIds);

		try {
			return this.generateExcelFiles(study, dataSet, selectedDatasetInstances, collectionOrderId);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("cannot.exportAsXLS.dataset", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	protected File generateExcelFiles(
		final Study study, final DatasetDTO dataSetDto, final List<StudyInstance> studyInstances, final int collectionOrderId)
		throws IOException {
		final List<File> files = new ArrayList<>();

		// Get the visible variables in SubObservation table
		final List<MeasurementVariable> columns =
			this.studyDatasetService.getSubObservationSetColumns(study.getId(), dataSetDto.getDatasetId());

		final int trialDatasetId = this.studyDataManager.getDataSetsByType(study.getId(), DataSetType.SUMMARY_DATA).get(0).getId();
		final File temporaryFolder = Files.createTempDir();

		for (final StudyInstance studyInstance : studyInstances) {
			final List<ObservationUnitRow> reorderedObservationUnitRows =
				this.getObservationUnitRows(study, dataSetDto, collectionOrderId, trialDatasetId, studyInstance);

			// Build the filename with the following format:
			// 'study_name'-'location_abbr'-'dataset_type'-'dataset_name'
			final String sanitizedFileName = FileUtils.sanitizeFileName(String
				.format(
					"%s_%s_%s_%s.xls", study.getName(), studyInstance.getLocationAbbreviation(),
					DataSetType.findById(dataSetDto.getDatasetTypeId()).name(), dataSetDto.getName()));

			final String fileNamePath = temporaryFolder.getAbsolutePath() + File.separator + sanitizedFileName;

			files.add(
				this.datasetXLSGenerator.generateXLSFile(study.getId(), dataSetDto, columns, reorderedObservationUnitRows, fileNamePath));
		}

		if (files.size() == 1) {
			return files.get(0);
		} else {
			return this.zipUtil.zipFiles(study.getName(), files);
		}
	}

	private List<ObservationUnitRow> getObservationUnitRows(
		final Study study, final DatasetDTO dataSetDto, final int collectionOrderId, final int trialDatasetId,
		final StudyInstance studyInstance) {
		final List<ObservationUnitRow> observationUnitRows =
			this.studyDatasetService
				.getObservationUnitRows(study.getId(), dataSetDto.getDatasetId(), studyInstance.getInstanceDbId(), Integer.MAX_VALUE,
					Integer.MAX_VALUE, null,
					"");

		final DatasetCollectionOrderServiceImpl.CollectionOrder collectionOrder =
			DatasetCollectionOrderServiceImpl.CollectionOrder.findById(collectionOrderId);
		return this.datasetCollectionOrderService
			.reorder(collectionOrder, trialDatasetId, String.valueOf(studyInstance.getInstanceNumber()), observationUnitRows);
	}
}
