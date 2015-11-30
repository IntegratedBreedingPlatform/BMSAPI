
package org.ibp.api.domain.study.validators;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.ibp.api.domain.ontology.TermSummary;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.MeasurementIdentifier;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.Trait;
import org.ibp.api.java.ontology.VariableService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObservationValidationDataExtractorTest {

	private static final Integer TEST_TRAIT_ID = 29004;

	private static final String TEST_TRAIT = "ACCNO";

	private static final Integer OBSERVATION_UNIQUE_ID = 100;

	private final Observation observation = new Observation();

	private final Map<String, String> requestAttributes = new HashMap<>();

	private final VariableService variableService = Mockito.mock(VariableService.class);

	private ObservationValidationDataExtractor observationValidationDataExtractor;

	private VariableDetails testVariableDetails;

	private List<Measurement> measurements;

	@Before
	public void createObservationValidationDataExtractor() throws Exception {

		this.requestAttributes.put("cropname", TestValidatorConstants.CROP_NAME);
		this.requestAttributes.put("studyId", TestValidatorConstants.STUDY_ID);
		this.requestAttributes.put("programId", TestValidatorConstants.PROGRAM_ID);
		this.observation.setUniqueIdentifier(ObservationValidationDataExtractorTest.OBSERVATION_UNIQUE_ID);

		final Measurement measurement = this.getTestMeasurementData();

		this.measurements = Collections.singletonList(measurement);
		this.observation.setMeasurements(this.measurements);

		this.testVariableDetails =
				new ObjectMapper().readValue(this.getClass().getResourceAsStream("acdTolVariable.json"), VariableDetails.class);

		Mockito.when(
				this.variableService.getVariableById(TestValidatorConstants.CROP_NAME, TestValidatorConstants.PROGRAM_ID,
						ObservationValidationDataExtractorTest.TEST_TRAIT_ID.toString())).thenReturn(this.testVariableDetails);

		this.observationValidationDataExtractor = new ObservationValidationDataExtractor();
	}

	private Measurement getTestMeasurementData() {
		final Measurement measurement = new Measurement();
		final MeasurementIdentifier measurementIdentifier = new MeasurementIdentifier();
		measurement.setMeasurementIdentifier(measurementIdentifier);

		measurementIdentifier.setMeasurementId(12);

		final Trait trait = new Trait();
		trait.setTraitId(ObservationValidationDataExtractorTest.TEST_TRAIT_ID);
		trait.setTraitName(ObservationValidationDataExtractorTest.TEST_TRAIT);

		measurementIdentifier.setTrait(trait);
		return measurement;
	}

	@Test
	public void testGetCropName() throws Exception {
		Assert.assertThat(this.observationValidationDataExtractor.getCropName(this.requestAttributes),
				CoreMatchers.equalTo(TestValidatorConstants.CROP_NAME));
	}

	@Test
	public void testStudyId() throws Exception {
		Assert.assertThat(this.observationValidationDataExtractor.getStudyId(this.requestAttributes),
				CoreMatchers.equalTo(TestValidatorConstants.STUDY_ID));
	}

	@Test
	public void testGetProgramId() throws Exception {
		Assert.assertThat(this.observationValidationDataExtractor.getProgramId(this.requestAttributes),
				CoreMatchers.equalTo(TestValidatorConstants.PROGRAM_ID));
	}

	@Test
	public void testGetObservationId() throws Exception {
		Assert.assertThat(this.observationValidationDataExtractor.getObservationId(this.observation),
				CoreMatchers.equalTo(ObservationValidationDataExtractorTest.OBSERVATION_UNIQUE_ID));
	}

	@Test(expected = NullPointerException.class)
	public void testNullCropThrowsException() throws Exception {
		this.requestAttributes.put("cropname", null);
		this.observationValidationDataExtractor.getCropName(this.requestAttributes);
		Assert.fail("We should have nerve got to this point. The getCropName method should have throw a null pointer execption.");
	}

	@Test(expected = NullPointerException.class)
	public void testNullStudyIdThrowsException() throws Exception {
		this.requestAttributes.put("studyId", null);
		this.observationValidationDataExtractor.getStudyId(this.requestAttributes);
		Assert.fail("We should have nerve got to this point. The getStudyId method should have throw a null pointer execption.");
	}

	@Test(expected = NullPointerException.class)
	public void testNullProgramIdThrowsException() throws Exception {
		this.requestAttributes.put("programId", null);
		this.observationValidationDataExtractor.getProgramId(this.requestAttributes);
		Assert.fail("We should have nerve got to this point. The getProgramId method should have throw a null pointer execption.");
	}

	@Test(expected = NullPointerException.class)
	public void testNullObservationIdThrowsException() throws Exception {
		this.observation.setUniqueIdentifier(null);
		this.observationValidationDataExtractor.getObservationId(this.observation);
		Assert.fail("We should have nerve got to this point. The getObservationId method should have throw a null pointer execption.");
	}

	@Test(expected = IllegalStateException.class)
	public void testZeroObservationIdThrowsException() throws Exception {
		this.observation.setUniqueIdentifier(0);
		this.observationValidationDataExtractor.getObservationId(this.observation);
		Assert.fail("We should have nerve got to this point. The getObservationId method should have throw a illegal state execption.");
	}

	@Test
	public void testGetVariableDetails() throws Exception {
		Assert.assertThat(this.observationValidationDataExtractor.getVariableDetails(this.measurements, 0, this.requestAttributes,
				this.variableService), CoreMatchers.equalTo(this.testVariableDetails));
	}

	@Test
	public void testGetVariableDateType() throws Exception {
		Assert.assertThat(this.observationValidationDataExtractor.getVariableDataType(this.observationValidationDataExtractor
				.getVariableDetails(this.measurements, 0, this.requestAttributes, this.variableService)), CoreMatchers
				.equalTo(this.testVariableDetails.getScale().getDataType()));
	}

	@Test
	public void testGetValidValues() throws Exception {
		Assert.assertThat(this.observationValidationDataExtractor.getVariableValidValues(this.observationValidationDataExtractor
				.getVariableDetails(this.measurements, 0, this.requestAttributes, this.variableService)), CoreMatchers
				.equalTo(this.testVariableDetails.getScale().getValidValues()));
	}

	@Test
	public void testGetObservationValidationData() throws Exception {
		final ObservationValidationData observationValidationData =
				this.observationValidationDataExtractor.getObservationValidationData(this.observation, this.requestAttributes,
						this.variableService);
		Assert.assertThat(observationValidationData.getCropName(), CoreMatchers.equalTo(TestValidatorConstants.CROP_NAME));
		Assert.assertThat(observationValidationData.getStudyId(), CoreMatchers.equalTo(TestValidatorConstants.STUDY_ID));
		Assert.assertThat(observationValidationData.getProgramId(), CoreMatchers.equalTo(TestValidatorConstants.PROGRAM_ID));
		Assert.assertThat(observationValidationData.getObservationId(),
				CoreMatchers.equalTo(ObservationValidationDataExtractorTest.OBSERVATION_UNIQUE_ID));

		Assert.assertThat(observationValidationData.getMeasurementVariableDetailsList().get(0).getVariableId(),
				CoreMatchers.equalTo(ObservationValidationDataExtractorTest.TEST_TRAIT_ID.toString()));
		Assert.assertThat(observationValidationData.getMeasurementVariableDetailsList().get(0).getVariableDataType(),
				CoreMatchers.equalTo(this.testVariableDetails.getScale().getDataType()));

		Assert.assertThat(observationValidationData.getMeasurementVariableDetailsList().get(0).getVariableValidValues(),
				CoreMatchers.equalTo(this.testVariableDetails.getScale().getValidValues()));

		final Map<String, TermSummary> mappedCategories =
				observationValidationData.getMeasurementVariableDetailsList().get(0).getMappedCategories();

		// Please note these values are coming out of acdTolVariable.json
		Assert.assertThat(mappedCategories.get("1").getDescription(), CoreMatchers.equalTo("1= highly susceptible "));
		Assert.assertThat(mappedCategories.get("2").getDescription(), CoreMatchers.equalTo("2= susceptible "));
		Assert.assertThat(mappedCategories.get("3").getDescription(), CoreMatchers.equalTo("3= intermediate"));
		Assert.assertThat(mappedCategories.get("4").getDescription(), CoreMatchers.equalTo("4= tolerant"));
		Assert.assertThat(mappedCategories.get("5").getDescription(), CoreMatchers.equalTo("5= highly tolerant"));

	}

}
