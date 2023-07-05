package org.ibp.api.java.impl.middleware.dataset;

import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
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
            // Single File Export for Transposed Excel is not implemented, so it's always set to false
            return this.generate(studyId, datasetId, instanceIds, collectionOrderId, this.datasetTransposedExcelGenerator, false, XLS,
                    includeSampleGenotypeValues);
        } catch (final IOException e) {
            final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
            errors.reject("cannot.exportAsXLS.dataset", "");
            throw new ResourceNotFoundException(errors.getAllErrors().get(0));
        }
    }

    @Override
    public List<MeasurementVariable> getColumns(final int studyId, final DatasetDTO dataSet, final boolean isIncludeSampleGenotypeValues) {
        List<MeasurementVariable> columns = this.studyDatasetService.getSubObservationSetVariables(studyId, dataSet.getDatasetId());
        // remove OBS_UNIT_ID column for Transposed Excel
        columns = columns.stream().filter(variable -> variable.getTermId() != TermId.OBS_UNIT_ID.getId())
                .collect(Collectors.toList());
        final MeasurementVariable trialInstanceVariable = new MeasurementVariable();
        trialInstanceVariable.setTermId(TermId.TRIAL_INSTANCE_FACTOR.getId());
        trialInstanceVariable.setVariableType(VariableType.ENVIRONMENT_DETAIL);
        trialInstanceVariable.setFactor(true);
        trialInstanceVariable.setName("TRIAL_INSTANCE");
        columns.add(0, trialInstanceVariable);
        this.includeSampleGenotypeValues(studyId, dataSet, isIncludeSampleGenotypeValues, columns);

        return columns;
    }
}
