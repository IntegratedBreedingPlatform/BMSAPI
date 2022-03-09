package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.dms.StockModel;
import org.generationcp.middleware.service.impl.analysis.MeansImportRequest;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeansImportRequestValidatorTest {

	public static final String EDIA_M_CM_BLUES = "EDia_M_cm_BLUEs";
	public static final String EDIA_M_CM_BLUPS = "EDia_M_cm_BLUPs";
	public static final String PH_M_CM_BLUES = "PH_M_cm_BLUEs";
	public static final String PH_M_CM_BLUPS = "PH_M_cm_BLUPs";
	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private BindingResult errors;

	@InjectMocks
	private MeansImportRequestValidator meansImportRequestValidator;

	@Test
	public void testValidate_OK() {

		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();

		final Study study = new Study();
		study.setId(meansImportRequest.getStudyId());
		study.setStudyType(new StudyTypeDto());
		final Map<String, Integer> instanceGeolocationIdMap = new HashMap<>();
		instanceGeolocationIdMap.put("1", meansImportRequest.getData().get(0).getEnvironmentId());
		instanceGeolocationIdMap.put("2", meansImportRequest.getData().get(1).getEnvironmentId());
		when(this.studyDataManager.getStudy(study.getId())).thenReturn(study);
		when(this.studyDataManager.getDataSetsByType(study.getId(), DatasetTypeEnum.MEANS_DATA.getId())).thenReturn(new ArrayList<>());
		when(this.studyDataManager.getInstanceGeolocationIdsMap(study.getId())).thenReturn(instanceGeolocationIdMap);
		when(this.studyDataManager.getStocksByStudyAndEntryNumbers(eq(study.getId()), any())).thenReturn(
			Arrays.asList(this.createStockModel("1")));
		when(this.ontologyVariableDataManager.getWithFilter(any())).thenReturn(
			Arrays.asList(this.createVariable(EDIA_M_CM_BLUPS, VariableType.ANALYSIS),
				this.createVariable(PH_M_CM_BLUPS, VariableType.ANALYSIS),
				this.createVariable(EDIA_M_CM_BLUES, VariableType.ANALYSIS),
				this.createVariable(PH_M_CM_BLUES, VariableType.ANALYSIS)));

		try {
			this.meansImportRequestValidator.validate(meansImportRequest);
			verifyNoInteractions(this.errors);
		} catch (final Exception e) {
			fail("Should not throw an exception");
		}
	}

	@Test
	public void testValidate_Fail() {
		try {
			final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
			this.meansImportRequestValidator.validate(meansImportRequest);
			fail("Should throw an exception");
		} catch (final Exception e) {

		}
	}

	@Test
	public void testValidateStudy_StudyIsRequired() {
		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		meansImportRequest.setStudyId(null);

		try {
			this.meansImportRequestValidator.validateStudy(meansImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("study.required", e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateStudy_StudyDoesNotExist() {
		when(this.studyDataManager.getStudy(1)).thenReturn(null);

		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		try {
			this.meansImportRequestValidator.validateStudy(meansImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("study.not.exist", e.getErrors().get(0).getCode());
		}

	}

	@Test
	public void testCheckIfMeansDatasetAlreadyExists_MeansDatasetAlreadyExists() {
		when(this.studyDataManager.getDataSetsByType(1, DatasetTypeEnum.MEANS_DATA.getId())).thenReturn(Arrays.asList(new DataSet()));

		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		try {
			this.meansImportRequestValidator.checkIfMeansDatasetAlreadyExists(meansImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("means.import.means.dataset.already.exists", e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testCheckMeansDataIsEmpty() {
		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		meansImportRequest.setData(new ArrayList<>());

		try {
			this.meansImportRequestValidator.checkMeansDataIsEmpty(meansImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("means.import.means.data.required", e.getErrors().get(0).getCode());
		}

	}

	@Test
	public void testCheckDataValuesIsEmpty() {
		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		meansImportRequest.getData().get(0).setValues(null);
		try {
			this.meansImportRequestValidator.checkDataValuesIsEmpty(meansImportRequest);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals("means.import.means.data.values.required", e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateEnvironmentId_HasBlankEnvironmentId() {
		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		meansImportRequest.getData().get(0).setEnvironmentId(null);
		this.meansImportRequestValidator.validateEnvironmentId(meansImportRequest, this.errors);

		verify(this.errors).reject("means.import.means.environment.ids.required", "");
	}

	@Test
	public void testValidateEnvironmentId_EnvironmentIdsDoNotExist() {
		when(this.studyDataManager.getInstanceGeolocationIdsMap(1)).thenReturn(new HashMap<>());

		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		this.meansImportRequestValidator.validateEnvironmentId(meansImportRequest, this.errors);

		verify(this.errors).reject("means.import.means.environment.ids.do.not.exist", new Object[] {
			"1, 2"}, "");
	}

	@Test
	public void testValidateEntryNumber_HasBlankEntryNumber() {
		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		meansImportRequest.getData().get(0).setEntryNo(null);
		this.meansImportRequestValidator.validateEntryNumber(meansImportRequest, this.errors);

		verify(this.errors).reject("means.import.means.entry.numbers.required", "");
	}

	@Test
	public void testValidateEntryNumber_EntryNumbersDoNotExist() {
		when(this.studyDataManager.getStocksByStudyAndEntryNumbers(eq(1), any())).thenReturn(new ArrayList<>());

		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		this.meansImportRequestValidator.validateEntryNumber(meansImportRequest, this.errors);

		verify(this.errors).reject("means.import.means.entry.numbers.do.not.exist", new Object[] {
			"1"}, "");
	}

	@Test
	public void testValidateEntryNumber_DuplicateEntryNumbersPerEnvironment() {
		when(this.studyDataManager.getStocksByStudyAndEntryNumbers(eq(1), any())).thenReturn(Arrays.asList(this.createStockModel("1")));

		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		final MeansImportRequest.MeansData duplicateEntryNoMeansData = new MeansImportRequest.MeansData();
		duplicateEntryNoMeansData.setEntryNo(1);
		duplicateEntryNoMeansData.setEnvironmentId(1);
		meansImportRequest.getData().add(duplicateEntryNoMeansData);
		this.meansImportRequestValidator.validateEntryNumber(meansImportRequest, this.errors);

		verify(this.errors).reject("means.import.means.duplicate.entry.numbers.per.environment", "");
	}

	@Test
	public void testValidateAnalysisVariableNames_VariableNamesDoNotExist() {
		when(this.ontologyVariableDataManager.getWithFilter(any())).thenReturn(new ArrayList<>());

		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		this.meansImportRequestValidator.validateAnalysisVariableNames(meansImportRequest, this.errors);

		verify(this.errors).reject("means.import.means.analysis.variable.names.do.not.exist",
			new Object[] {EDIA_M_CM_BLUPS + ", " + PH_M_CM_BLUPS + ", " + EDIA_M_CM_BLUES + ", " + PH_M_CM_BLUES}, "");
	}

	@Test
	public void testValidateAnalysisVariableNames_VariablesHaveInvalidVariableType() {
		when(this.ontologyVariableDataManager.getWithFilter(any())).thenReturn(
			Arrays.asList(this.createVariable(EDIA_M_CM_BLUPS, VariableType.TRAIT),
				this.createVariable(PH_M_CM_BLUPS, VariableType.TRAIT),
				this.createVariable(EDIA_M_CM_BLUES, VariableType.TRAIT), this.createVariable(PH_M_CM_BLUES, VariableType.TRAIT)));

		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		this.meansImportRequestValidator.validateAnalysisVariableNames(meansImportRequest, this.errors);

		verify(this.errors).reject("means.import.means.variables.must.be.analysis.type",
			new Object[] {PH_M_CM_BLUPS + ", " + PH_M_CM_BLUES + ", " + EDIA_M_CM_BLUPS + ", " + EDIA_M_CM_BLUES}, "");
	}

	private MeansImportRequest createMeansImportRequest() {

		final Random random = new Random();
		final MeansImportRequest meansImportRequest = new MeansImportRequest();
		meansImportRequest.setStudyId(1);

		final MeansImportRequest.MeansData meansData1 = new MeansImportRequest.MeansData();
		meansData1.setEnvironmentId(1);
		meansData1.setEntryNo(1);
		final Map<String, Double> values1 = new HashMap<>();
		values1.put(EDIA_M_CM_BLUES, random.nextDouble());
		values1.put(EDIA_M_CM_BLUPS, random.nextDouble());
		values1.put(PH_M_CM_BLUES, random.nextDouble());
		values1.put(PH_M_CM_BLUPS, random.nextDouble());
		meansData1.setValues(values1);

		final MeansImportRequest.MeansData meansData2 = new MeansImportRequest.MeansData();
		meansData2.setEnvironmentId(2);
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

	private StockModel createStockModel(final String entryNumber) {
		final StockModel stockModel = new StockModel();
		stockModel.setUniqueName(entryNumber);
		return stockModel;
	}

}
