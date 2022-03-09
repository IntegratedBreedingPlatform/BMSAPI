package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.impl.analysis.MeansImportRequest;
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
public class MeansImportRequestValidatorTest {

	public static final String EDIA_M_CM_BLUES = "EDia_M_cm_BLUEs";
	public static final String EDIA_M_CM_BLUPS = "EDia_M_cm_BLUPs";
	public static final String PH_M_CM_BLUES = "PH_M_cm_BLUEs";
	public static final String PH_M_CM_BLUPS = "PH_M_cm_BLUPs";

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@InjectMocks
	private MeansImportRequestValidator meansImportRequestValidator;

	@Test
	public void testValidateMeansDataIsNotEmpty() {
		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		meansImportRequest.setData(new ArrayList<>());

		try {
			this.meansImportRequestValidator.validateMeansDataIsNotEmpty(meansImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("means.import.means.data.required", e.getErrors().get(0).getCode());
		}

	}

	@Test
	public void testValidateDataValuesIsNotEmpty() {
		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		meansImportRequest.getData().get(0).setValues(null);
		try {
			this.meansImportRequestValidator.validateDataValuesIsNotEmpty(meansImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("means.import.means.data.values.required", e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateEnvironmentNumberIsNotEmpty_HasBlankEnvironmentId() {
		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		meansImportRequest.getData().get(0).setEnvironmentNumber(null);

		try {
			this.meansImportRequestValidator.validateEnvironmentNumberIsNotEmpty(meansImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("means.import.means.environment.numbers.required", e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateEntryNumberIsNotEmptyAndDistinctPerEnvironment_HasBlankEntryNumber() {
		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		meansImportRequest.getData().get(0).setEntryNo(null);
		try {
			this.meansImportRequestValidator.validateEntryNumberIsNotEmptyAndDistinctPerEnvironment(meansImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("means.import.means.entry.numbers.required", e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateEntryNumberIsNotEmptyAndDistinctPerEnvironment_DuplicateEntryNumbersPerEnvironment() {
		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		final MeansImportRequest.MeansData duplicateEntryNoMeansData = new MeansImportRequest.MeansData();
		duplicateEntryNoMeansData.setEntryNo(1);
		duplicateEntryNoMeansData.setEnvironmentNumber(1);
		meansImportRequest.getData().add(duplicateEntryNoMeansData);

		try {
			this.meansImportRequestValidator.validateEntryNumberIsNotEmptyAndDistinctPerEnvironment(meansImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("means.import.means.duplicate.entry.numbers.per.environment", e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateAnalysisVariableNames_VariableNamesDoNotExist() {
		when(this.ontologyVariableDataManager.getWithFilter(any())).thenReturn(new ArrayList<>());

		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();

		try {
			this.meansImportRequestValidator.validateAnalysisVariableNames(meansImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("means.import.means.analysis.variable.names.do.not.exist", e.getErrors().get(0).getCode());
			assertEquals(new Object[] {EDIA_M_CM_BLUPS + ", " + PH_M_CM_BLUPS + ", " + EDIA_M_CM_BLUES + ", " + PH_M_CM_BLUES},
				e.getErrors().get(0).getArguments());
		}
	}

	@Test
	public void testValidateAnalysisVariableNames_VariablesHaveInvalidVariableType() {
		when(this.ontologyVariableDataManager.getWithFilter(any())).thenReturn(
			Arrays.asList(this.createVariable(EDIA_M_CM_BLUPS, VariableType.TRAIT),
				this.createVariable(PH_M_CM_BLUPS, VariableType.TRAIT),
				this.createVariable(EDIA_M_CM_BLUES, VariableType.TRAIT), this.createVariable(PH_M_CM_BLUES, VariableType.TRAIT)));

		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();

		try {
			this.meansImportRequestValidator.validateAnalysisVariableNames(meansImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("means.import.means.variables.must.be.analysis.type", e.getErrors().get(0).getCode());
			assertEquals(new Object[] {PH_M_CM_BLUPS + ", " + PH_M_CM_BLUES + ", " + EDIA_M_CM_BLUPS + ", " + EDIA_M_CM_BLUES},
				e.getErrors().get(0).getArguments());
		}
	}

	private MeansImportRequest createMeansImportRequest() {

		final Random random = new Random();
		final MeansImportRequest meansImportRequest = new MeansImportRequest();

		final MeansImportRequest.MeansData meansData1 = new MeansImportRequest.MeansData();
		meansData1.setEnvironmentNumber(1);
		meansData1.setEntryNo(1);
		final Map<String, Double> values1 = new HashMap<>();
		values1.put(EDIA_M_CM_BLUES, random.nextDouble());
		values1.put(EDIA_M_CM_BLUPS, random.nextDouble());
		values1.put(PH_M_CM_BLUES, random.nextDouble());
		values1.put(PH_M_CM_BLUPS, random.nextDouble());
		meansData1.setValues(values1);

		final MeansImportRequest.MeansData meansData2 = new MeansImportRequest.MeansData();
		meansData2.setEnvironmentNumber(2);
		meansData2.setEntryNo(1);
		final Map<String, Double> values2 = new HashMap<>();
		values2.put(EDIA_M_CM_BLUES, random.nextDouble());
		values2.put(EDIA_M_CM_BLUPS, random.nextDouble());
		values2.put(PH_M_CM_BLUES, random.nextDouble());
		values2.put(PH_M_CM_BLUPS, random.nextDouble());
		meansData2.setValues(values2);

		final List<MeansImportRequest.MeansData> meansDataList = new ArrayList<>();
		meansDataList.add(meansData1);
		meansDataList.add(meansData2);
		meansImportRequest.setData(meansDataList);
		return meansImportRequest;
	}

	private Variable createVariable(final String name, final VariableType variableType) {
		final Variable variable = new Variable();
		variable.setName(name);
		variable.addVariableType(variableType);
		return variable;
	}

	private StudyEntryDto createStudyEntry(final Integer entryNumber) {
		final StudyEntryDto studyEntryDto = new StudyEntryDto();
		studyEntryDto.setEntryNumber(entryNumber);
		return studyEntryDto;
	}

}
