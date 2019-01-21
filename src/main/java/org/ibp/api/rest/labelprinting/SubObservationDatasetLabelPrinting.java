package org.ibp.api.rest.labelprinting;

import org.apache.commons.lang3.tuple.Pair;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class SubObservationDatasetLabelPrinting implements LabelPrintingStrategy {

	@Autowired
	private ResourceBundleMessageSource messageSource;

	@Autowired
	private DatasetService middlewareDatasetService;

	@Override
	public void validateInputData(final LabelsNeededSummaryInput labelsNeededSummaryInput) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final DatasetDTO dataSet = this.middlewareDatasetService.getDataset(labelsNeededSummaryInput.getDatasetId());
		if (dataSet == null) {
			errors.reject("dataset.does.not.exist", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	@Override
	public LabelsNeededSummary getSummaryOfLabelsNeeded(final LabelsNeededSummaryInput labelsNeededSummaryInput) {
		final LabelsNeededSummary labelsNeededSummary = new LabelsNeededSummary();
		final List<Pair<String, Long>> observationsByInstance = middlewareDatasetService.countObservationsGroupedByInstance(labelsNeededSummaryInput.getDatasetId());
		long totalNumberOfLabelsNeeded = 0;
		for (Pair<String, Long> pair: observationsByInstance) {
			final LabelsNeededSummary.Row row = new LabelsNeededSummary.Row();
			row.setInstanceNumber(pair.getLeft());
			row.setLabelsNeeded(pair.getRight());
			row.setSubObservationNumber(pair.getRight());
			labelsNeededSummary.addRow(row);
			totalNumberOfLabelsNeeded += pair.getRight();
		}
		labelsNeededSummary.setTotalNumberOfLabelsNeeded(totalNumberOfLabelsNeeded);
		return labelsNeededSummary;
	}

	@Override
	public LabelsNeededSummaryResponse transformLabelsNeededSummary(
		final LabelsNeededSummary labelsNeededSummary) {
		final String labelsNeededText = messageSource.getMessage("label.printing.labels.needed", null, LocaleContextHolder.getLocale());
		final String environmentText = messageSource.getMessage( "label.printing.environment", null, LocaleContextHolder.getLocale());
		final String numberOfSubObsNeededText = messageSource.getMessage("label.printing.number.of.subobservations.needed", null, LocaleContextHolder.getLocale());
		final LabelsNeededSummaryResponse response = new LabelsNeededSummaryResponse();
		final List<String> headers = new LinkedList<>();
		headers.add(environmentText);
		headers.add(numberOfSubObsNeededText);
		headers.add(labelsNeededText);
		response.setHeaders(headers);
		final List<Map<String, String>> values = new LinkedList<>();
		for (LabelsNeededSummary.Row row : labelsNeededSummary.getRows()) {
			final Map<String, String> valuesMap = new LinkedHashMap<>();
			valuesMap.put(environmentText, row.getInstanceNumber());
			valuesMap.put(numberOfSubObsNeededText, String.valueOf(row.getSubObservationNumber()));
			valuesMap.put(labelsNeededText, String.valueOf(row.getLabelsNeeded()));
			values.add(valuesMap);
		}
		response.setValues(values);
		response.setTotalNumberOfLabelsNeeded(labelsNeededSummary.getTotalNumberOfLabelsNeeded());
		return response;
	}
}
