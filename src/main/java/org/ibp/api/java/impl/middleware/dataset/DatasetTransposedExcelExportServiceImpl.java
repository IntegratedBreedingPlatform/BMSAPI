package org.ibp.api.java.impl.middleware.dataset;

import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
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
import java.util.stream.Collectors;


@Service
@Transactional
public class DatasetTransposedExcelExportServiceImpl extends DatasetExcelExportServiceImpl implements DatasetExportService {
    @Resource
    private DatasetTransposedExcelGenerator datasetTransposedExcelGenerator;

    @Override
    public File export(final int studyId, final int datasetId, final Set<Integer> instanceIds, final int collectionOrderId,
                       final boolean singleFile, final boolean includeSampleGenotypeValues) {

        this.validate(studyId, datasetId, instanceIds);

        try {
            this.datasetTransposedExcelGenerator.setIncludeSampleGenotypeValues(includeSampleGenotypeValues);
            return this.generate(studyId, datasetId, instanceIds, collectionOrderId, this.datasetTransposedExcelGenerator, false, XLS,
                    includeSampleGenotypeValues);
        } catch (final IOException e) {
            final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
            errors.reject("cannot.exportAsXLS.dataset", "");
            throw new ResourceNotFoundException(errors.getAllErrors().get(0));
        }
    }

    @Override
    public List<MeasurementVariable> getColumns(final int studyId, final DatasetDTO dataSet, final boolean includeSampleGenotypeValues) {
        List<MeasurementVariable> columns = this.studyDatasetService.getSubObservationSetVariables(studyId, dataSet.getDatasetId());
        // remove OBS_UNIT_ID column for Transposed Excel
        columns = columns.stream().filter(variable -> variable.getTermId() != TermId.OBS_UNIT_ID.getId())
                .collect(Collectors.toList());
        this.includeSampleGenotypeValues(studyId, dataSet, includeSampleGenotypeValues, columns);

        return columns;
    }
}
