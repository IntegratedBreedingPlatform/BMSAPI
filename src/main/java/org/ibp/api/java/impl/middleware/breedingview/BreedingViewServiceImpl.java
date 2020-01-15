/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.ibp.api.java.impl.middleware.breedingview;

import com.rits.cloning.Cloner;
import org.generationcp.commons.service.BreedingViewImportService;
import org.generationcp.middleware.domain.dms.ExperimentValues;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.ibp.api.exception.IBPWebServiceException;
import org.ibp.api.java.breedingview.BreedingViewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.Map;

@Component
public class BreedingViewServiceImpl implements BreedingViewService {

	@Autowired
	private BreedingViewImportService breedingViewImportService;

	@Autowired
	private Cloner cloner;

	private VariableTypeList variableTypeListSummaryStats;

	private List<ExperimentValues> experimentValuesList;
	private List<ExperimentValues> summaryStatsExperimentValuesList;

	private static final Logger LOG = LoggerFactory.getLogger(BreedingViewServiceImpl.class);

	@Override
	@Transactional
	public void execute(final Map<String, String> params, final List<String> errors) throws IBPWebServiceException {

		try {

			final String mainOutputFilePath = params.get(BreedingViewParameter.MAIN_OUTPUT_FILE_PATH.getParamValue());
			final String summaryOutputFilePath = params.get(BreedingViewParameter.SUMMARY_OUTPUT_FILE_PATH.getParamValue());
			final String outlierOutputFilePath = params.get(BreedingViewParameter.OUTLIER_OUTPUT_FILE_PATH.getParamValue());
			final int studyId = Integer.parseInt(params.get(BreedingViewParameter.STUDY_ID.getParamValue()));

			this.breedingViewImportService.importMeansData(new File(mainOutputFilePath), studyId);

			if (outlierOutputFilePath != null && !"".equals(outlierOutputFilePath)) {
				this.breedingViewImportService.importOutlierData(new File(outlierOutputFilePath), studyId);
			}

			if (summaryOutputFilePath != null && !"".equals(summaryOutputFilePath)) {
				this.breedingViewImportService.importSummaryStatsData(new File(summaryOutputFilePath), studyId);
			}

		} catch (final Exception e) {
			BreedingViewServiceImpl.LOG.error("ERROR:", e);
			throw new IBPWebServiceException(e.getMessage());
		}

	}

	protected Cloner getCloner() {
		return this.cloner;
	}

	protected void setCloner(final Cloner cloner) {
		this.cloner = cloner;
	}

	protected VariableTypeList getMeansVariableTypeList() {
		return new VariableTypeList();
	}

	protected List<ExperimentValues> getExperimentValuesList() {
		return this.experimentValuesList;
	}

	protected List<ExperimentValues> getSummaryStatsExperimentValuesList() {
		return this.summaryStatsExperimentValuesList;
	}

	protected VariableTypeList getVariableTypeListSummaryStats() {
		return this.variableTypeListSummaryStats;
	}

	public void setBreedingViewImportService(final BreedingViewImportService breedingViewImportService) {
		this.breedingViewImportService = breedingViewImportService;
	}

}
