package org.ibp.api.java.impl.middleware.dataset;

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
import java.util.Set;

@Service
@Transactional
public class DatasetKsuExcelExportServiceImpl extends BaseDatasetKsuExportService implements DatasetExportService {

	@Resource
	private DatasetKsuExcelGenerator datasetKsuExcelGenerator;

	@Override
	public File export(final int studyId, final int datasetId, final Set<Integer> instanceIds, final int collectionOrderId,
		final boolean singleFile, final boolean includeSampleGenotypeValues) {

		this.validate(studyId, datasetId, instanceIds);

		try {
			//TODO: use the singleFile boolean after implementing singleFile download for KSU Excel option
			return this.generate(studyId, datasetId, instanceIds, collectionOrderId, this.datasetKsuExcelGenerator, false, XLS,
					includeSampleGenotypeValues);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("cannot.exportAsXLS.dataset", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}
}
