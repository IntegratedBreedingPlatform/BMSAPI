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
import org.ibp.api.java.dataset.DatasetCollectionOrderService;
import org.ibp.api.java.dataset.DatasetFileGenerator;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class AbstractDatasetExportService {

	public static final String XLS = "xls";
	public static final String CSV = "csv";

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private DatasetValidator datasetValidator;

	@Autowired
	private InstanceValidator instanceValidator;

	@Autowired
	protected DatasetService studyDatasetService;

	@Autowired
	private DatasetCollectionOrderService datasetCollectionOrderService;

	@Resource
	protected StudyDataManager studyDataManager;

	protected ZipUtil zipUtil = new ZipUtil();

	protected void validate(final int studyId, final int datasetId, final Set<Integer> instanceIds) {
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

	protected List<ObservationUnitRow> getObservationUnitRows(
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

	protected File generateFiles(
		final Study study, final DatasetDTO dataSetDto, final List<StudyInstance> studyInstances, final int collectionOrderId,
		final DatasetFileGenerator generator, final String fileExtension)
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
					"%s_%s_%s_%s." + fileExtension, study.getName(), studyInstance.getLocationAbbreviation(),
					DataSetType.findById(dataSetDto.getDatasetTypeId()).name(), dataSetDto.getName()));

			final String fileNamePath = temporaryFolder.getAbsolutePath() + File.separator + sanitizedFileName;

			files.add(
				generator.generateFile(study.getId(), dataSetDto, columns, reorderedObservationUnitRows, fileNamePath));
		}

		if (files.size() == 1) {
			return files.get(0);
		} else {
			return this.zipUtil.zipFiles(study.getName(), files);
		}
	}

	public void setZipUtil(final ZipUtil zipUtil) {
		this.zipUtil = zipUtil;
	}
}
