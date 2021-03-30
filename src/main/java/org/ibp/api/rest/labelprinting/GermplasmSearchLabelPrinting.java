package org.ibp.api.rest.labelprinting;

import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.domain.LabelsInfoInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummary;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummaryResponse;
import org.ibp.api.rest.labelprinting.domain.OriginResourceMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Component
@Transactional
public class GermplasmSearchLabelPrinting extends LabelPrintingStrategy {

	@Autowired
	private ResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private SearchRequestService searchRequestService;


	public static List<FileType> SUPPORTED_FILE_TYPES = Arrays.asList(FileType.CSV, FileType.PDF, FileType.XLS);

	@Override
	void validateLabelsInfoInputData(final LabelsInfoInput labelsInfoInput) {

	}

	@Override
	LabelsNeededSummary getSummaryOfLabelsNeeded(
		final LabelsInfoInput labelsInfoInput) {
		return null;
	}

	@Override
	LabelsNeededSummaryResponse transformLabelsNeededSummary(
		final LabelsNeededSummary labelsNeededSummary) {
		return null;
	}

	@Override
	OriginResourceMetadata getOriginResourceMetadata(
		final LabelsInfoInput labelsInfoInput) {
		final String fileName = FileNameGenerator.generateFileName("germplasm-labels");
		return new OriginResourceMetadata(FileUtils.cleanFileName(fileName), new HashMap<>());	}

	@Override
	List<LabelType> getAvailableLabelTypes(
		final LabelsInfoInput labelsInfoInput) {
		return null;
	}

	@Override
	LabelsData getLabelsData(
		final LabelsGeneratorInput labelsGeneratorInput) {
		return null;
	}

	@Override
	List<FileType> getSupportedFileTypes() {
		return null;
	}
}
