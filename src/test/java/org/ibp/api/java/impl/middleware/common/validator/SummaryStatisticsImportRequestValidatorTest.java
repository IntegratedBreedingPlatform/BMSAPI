package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.service.impl.analysis.SummaryStatisticsImportRequest;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SummaryStatisticsImportRequestValidatorTest {

	public static final String EDIA_M_CM_Heritability = "EDIA_M_CM_Heritability";
	public static final String EDIA_M_CM_PValue = "EDIA_M_CM_PValue";
	public static final String PH_M_CM_Heritability = "PH_M_CM_Heritability";
	public static final String PH_M_CM_PValue = "PH_M_CM_PValue";

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@InjectMocks
	private SummaryStatisticsImportRequestValidator summaryStatisticsImportRequestValidator;

	@Test
	public void testValidateMeansDataIsNotEmpty() {
		final SummaryStatisticsImportRequest summaryStatisticsImportRequest = this.createSummaryStatisticsImportRequest();
		summaryStatisticsImportRequest.setData(new ArrayList<>());

		try {
			this.summaryStatisticsImportRequestValidator.validateSummaryDataIsNotEmpty(summaryStatisticsImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("summary.statistics.import.data.required", e.getErrors().get(0).getCode());
		}

	}

	@Test
	public void testValidateDataValuesIsNotEmpty() {
		final SummaryStatisticsImportRequest SummaryStatisticsImportRequest = this.createSummaryStatisticsImportRequest();
		SummaryStatisticsImportRequest.getData().get(0).setValues(null);
		try {
			this.summaryStatisticsImportRequestValidator.validateDataValuesIsNotEmpty(SummaryStatisticsImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("summary.statistics.import.data.values.required", e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateEnvironmentNumberIsNotEmpty_HasBlankEnvironmentId() {
		final SummaryStatisticsImportRequest SummaryStatisticsImportRequest = this.createSummaryStatisticsImportRequest();
		SummaryStatisticsImportRequest.getData().get(0).setEnvironmentNumber(null);

		try {
			this.summaryStatisticsImportRequestValidator.validateEnvironmentNumberIsNotEmpty(SummaryStatisticsImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("summary.statistics.import.environment.numbers.required", e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateAnalysisVariableNames_VariableNamesDoNotExist() {
		when(this.ontologyVariableDataManager.getWithFilter(any())).thenReturn(new ArrayList<>());

		final SummaryStatisticsImportRequest SummaryStatisticsImportRequest = this.createSummaryStatisticsImportRequest();

		try {
			this.summaryStatisticsImportRequestValidator.validateAnalysisVariableNames(SummaryStatisticsImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("summary.statistics.import.analysis.summary.variable.names.do.not.exist", e.getErrors().get(0).getCode());
			assertEquals(
				new Object[] {EDIA_M_CM_Heritability + ", " + EDIA_M_CM_PValue + ", " + PH_M_CM_Heritability + ", " + PH_M_CM_PValue},
				e.getErrors().get(0).getArguments());
		}
	}

	@Test
	public void testValidateAnalysisVariableNames_VariablesHaveInvalidVariableType() {
		when(this.ontologyVariableDataManager.getWithFilter(any())).thenReturn(
			Arrays.asList(this.createVariable(EDIA_M_CM_PValue, VariableType.TRAIT),
				this.createVariable(PH_M_CM_PValue, VariableType.TRAIT),
				this.createVariable(EDIA_M_CM_Heritability, VariableType.TRAIT),
				this.createVariable(PH_M_CM_Heritability, VariableType.TRAIT)));

		final SummaryStatisticsImportRequest SummaryStatisticsImportRequest = this.createSummaryStatisticsImportRequest();

		try {
			this.summaryStatisticsImportRequestValidator.validateAnalysisVariableNames(SummaryStatisticsImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("summary.statistics.import.variables.must.be.analysis.summary.type", e.getErrors().get(0).getCode());
			assertEquals(
				new Object[] {EDIA_M_CM_Heritability + ", " + PH_M_CM_Heritability + ", " + EDIA_M_CM_PValue + ", " + PH_M_CM_PValue},
				e.getErrors().get(0).getArguments());
		}
	}

	@Test
	public void testValidateEnvironmentNumberIsDistinct() {
		final SummaryStatisticsImportRequest summaryStatisticsImportRequest = this.createSummaryStatisticsImportRequest();
		summaryStatisticsImportRequest.getData().get(1).setEnvironmentNumber(1);

		try {
			this.summaryStatisticsImportRequestValidator.validateEnvironmentNumberIsDistinct(summaryStatisticsImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("summary.statistics.import.duplicate.environment.number", e.getErrors().get(0).getCode());
		}

	}

	private SummaryStatisticsImportRequest createSummaryStatisticsImportRequest() {

		final Random random = new Random();
		final SummaryStatisticsImportRequest summaryStatisticsImportRequest = new SummaryStatisticsImportRequest();

		final SummaryStatisticsImportRequest.SummaryData summaryData1 = new SummaryStatisticsImportRequest.SummaryData();
		summaryData1.setEnvironmentNumber(1);
		final Map<String, Double> values1 = new HashMap<>();
		values1.put(EDIA_M_CM_Heritability, random.nextDouble());
		values1.put(EDIA_M_CM_PValue, random.nextDouble());
		values1.put(PH_M_CM_Heritability, random.nextDouble());
		values1.put(PH_M_CM_PValue, random.nextDouble());
		summaryData1.setValues(values1);

		final SummaryStatisticsImportRequest.SummaryData summaryData2 = new SummaryStatisticsImportRequest.SummaryData();
		summaryData2.setEnvironmentNumber(2);
		final Map<String, Double> values2 = new HashMap<>();
		values2.put(EDIA_M_CM_Heritability, random.nextDouble());
		values2.put(EDIA_M_CM_PValue, random.nextDouble());
		values2.put(PH_M_CM_Heritability, random.nextDouble());
		values2.put(PH_M_CM_PValue, random.nextDouble());
		summaryData2.setValues(values2);

		final List<SummaryStatisticsImportRequest.SummaryData> summaryDataList = new ArrayList<>();
		summaryDataList.add(summaryData1);
		summaryDataList.add(summaryData2);
		summaryStatisticsImportRequest.setData(summaryDataList);
		return summaryStatisticsImportRequest;
	}

	private Variable createVariable(final String name, final VariableType variableType) {
		final Variable variable = new Variable();
		variable.setName(name);
		variable.addVariableType(variableType);
		return variable;
	}

}
