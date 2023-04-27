package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.genotype.SampleGenotypeDTO;
import org.generationcp.middleware.domain.genotype.SampleGenotypeSearchRequestDTO;
import org.generationcp.middleware.domain.genotype.SampleGenotypeVariablesSearchFilter;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsSearchDTO;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetExportService;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@Transactional
public class DatasetExcelExportServiceImpl extends AbstractDatasetExportService implements DatasetExportService {

	@Resource
	private DatasetExcelGenerator datasetExcelGenerator;

	@Override
	public File export(final int studyId, final int datasetId, final Set<Integer> instanceIds, final int collectionOrderId,
		final boolean singleFile, final boolean includeSampleGenotypeValues) {

		this.validate(studyId, datasetId, instanceIds);

		try {
			//TODO: use the singleFile boolean after implementing singleFile download for XLS option
			this.datasetExcelGenerator.setIncludeSampleGenotypeValues(includeSampleGenotypeValues);
			return this.generate(studyId, datasetId, instanceIds, collectionOrderId, this.datasetExcelGenerator, singleFile, XLS,
					includeSampleGenotypeValues);
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
			errors.reject("cannot.exportAsXLS.dataset", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	@Override
	public List<MeasurementVariable> getColumns(final int studyId, final DatasetDTO dataSet, final boolean includeSampleGenotypeValues) {
		final List<MeasurementVariable> columns;
		if (DatasetTypeEnum.SUMMARY_DATA.getId() == dataSet.getDatasetTypeId() || DatasetTypeEnum.SUMMARY_STATISTICS_DATA.getId() == dataSet.getDatasetTypeId()) {
			columns = this.studyDatasetService.getObservationSetColumns(studyId, dataSet.getDatasetId(), false);
		} else {
			columns = this.studyDatasetService.getSubObservationSetVariables(studyId, dataSet.getDatasetId());
		}

		if (includeSampleGenotypeValues) {
			// Add Genotype Marker variables to the list of columns
			final SampleGenotypeVariablesSearchFilter filter = new SampleGenotypeVariablesSearchFilter();
			filter.setStudyId(studyId);
			filter.setDatasetIds(Arrays.asList(dataSet.getDatasetId()));
			final Map<Integer, MeasurementVariable> genotypeVariablesMap =
				this.sampleGenotypeService.getSampleGenotypeVariables(filter);
			columns.addAll(genotypeVariablesMap.values());
		}

		return columns;
	}

	@Override
	public Map<Integer, List<ObservationUnitRow>> getObservationUnitRowMap(final Study study, final DatasetDTO dataset,
		final Map<Integer, StudyInstance> selectedDatasetInstancesMap) {
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap = new HashMap<>();
		final ObservationUnitsSearchDTO searchDTO = new ObservationUnitsSearchDTO();
		this.updateSearchDTOForSummaryData(dataset, searchDTO);
		for (final Integer instanceDBID : selectedDatasetInstancesMap.keySet()) {
			searchDTO.setInstanceIds(Arrays.asList(selectedDatasetInstancesMap.get(instanceDBID).getInstanceId()));
			final PageRequest pageRequest = new PageRequest(0, Integer.MAX_VALUE);
			final List<ObservationUnitRow> observationUnitRows = this.studyDatasetService
				.getObservationUnitRows(study.getId(), dataset.getDatasetId(), searchDTO, pageRequest);
			observationUnitRowMap.put(instanceDBID, observationUnitRows);
		}
		return observationUnitRowMap;
	}

	private void updateSearchDTOForSummaryData(final DatasetDTO dataset, final ObservationUnitsSearchDTO searchDTO) {
		if (DatasetTypeEnum.SUMMARY_DATA.getId() == dataset.getDatasetTypeId()) {
			final List<MeasurementVariableDto> environmentDetails = new ArrayList<>();
			final List<MeasurementVariableDto> environmentConditions = new ArrayList<>();
			final List<MeasurementVariable> environmentVariables = this.studyDatasetService.getMeasurementVariables(
					dataset.getDatasetId(), Lists.newArrayList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.ENVIRONMENT_CONDITION.getId()));
			for (final MeasurementVariable variable: environmentVariables) {
				if (VariableType.ENVIRONMENT_DETAIL.getId().equals(variable.getVariableType().getId())) {
					environmentDetails.add(new MeasurementVariableDto(variable.getTermId(), variable.getName()));
				} else if (VariableType.ENVIRONMENT_CONDITION.getId().equals(variable.getVariableType().getId())) {
					environmentConditions.add(new MeasurementVariableDto(variable.getTermId(), variable.getName()));
				}
			}
			searchDTO.setEnvironmentDetails(environmentDetails);
			searchDTO.setEnvironmentConditions(environmentConditions);
			searchDTO.setEnvironmentDatasetId(dataset.getDatasetId());
		}
	}

	@Override
	protected Map<Integer, List<SampleGenotypeDTO>> getSampleGenotypeRowMap(final Study study, final DatasetDTO dataset,
		final Map<Integer, StudyInstance> selectedDatasetInstancesMap, final boolean includeSampleGenotypeValues) {

		if (includeSampleGenotypeValues) {
			final SampleGenotypeSearchRequestDTO sampleGenotypeSearchRequestDTO = new SampleGenotypeSearchRequestDTO();
			sampleGenotypeSearchRequestDTO.setStudyId(study.getId());
			final SampleGenotypeSearchRequestDTO.GenotypeFilter filter = new SampleGenotypeSearchRequestDTO.GenotypeFilter();
			filter.setDatasetId(dataset.getDatasetId());
			filter.setInstanceIds(
				selectedDatasetInstancesMap.values().stream().map(StudyInstance::getInstanceId).collect(Collectors.toList()));
			sampleGenotypeSearchRequestDTO.setFilter(filter);
			return this.sampleGenotypeService.searchSampleGenotypes(sampleGenotypeSearchRequestDTO, null).stream()
				.collect(groupingBy(SampleGenotypeDTO::getObservationUnitId));

		}
		return new HashMap<>();

	}
}
