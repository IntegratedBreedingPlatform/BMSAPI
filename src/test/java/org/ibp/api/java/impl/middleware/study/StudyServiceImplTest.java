
package org.ibp.api.java.impl.middleware.study;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.service.api.study.StudyService;
import org.ibp.api.domain.germplasm.GermplasmListEntrySummary;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.MeasurementIdentifier;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.domain.study.Trait;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class StudyServiceImplTest {

	private static final int TEST_STUDY_IDENTIFIER = 2013;

	private static final int TEST_OBSERVATION_IDENTIIFER = 5;

	private StudyServiceImpl studyServiceImpl;

	@Mock
	private StudyService mockMiddlewareStudyService;

	private final String programUID = UUID.randomUUID().toString();

	final PodamFactory factory = new PodamFactoryImpl();

	final Function<ObservationDto, Observation> observationTransformFunction = new Function<ObservationDto, Observation>() {

		@Override
		public Observation apply(ObservationDto input) {
			return mapObservationDtoToObservation(input);
		}

	};

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		studyServiceImpl = new StudyServiceImpl();
		studyServiceImpl.setMiddlewareStudyService(this.mockMiddlewareStudyService);
	}

	@Test
	public void listAllStudies() throws MiddlewareQueryException {

		List<org.generationcp.middleware.service.api.study.StudySummary> mockResult = new ArrayList<>();
		org.generationcp.middleware.service.api.study.StudySummary studySummary =
				new org.generationcp.middleware.service.api.study.StudySummary();
		studySummary.setId(1);
		studySummary.setName("Study Name");
		studySummary.setObjective("Study Objective");
		studySummary.setTitle("Study Title");
		studySummary.setProgramUUID(programUID);
		studySummary.setStartDate("2015-01-01");
		studySummary.setEndDate("2015-12-31");
		studySummary.setType(StudyType.T);

		mockResult.add(studySummary);
		Mockito.when(mockMiddlewareStudyService.listAllStudies(programUID)).thenReturn(mockResult);

		List<StudySummary> studySummaries = studyServiceImpl.listAllStudies(programUID);
		Assert.assertEquals(mockResult.size(), studySummaries.size());
		Assert.assertEquals(studySummary.getId(), studySummaries.get(0).getId());
		Assert.assertEquals(studySummary.getName(), studySummaries.get(0).getName());
		Assert.assertEquals(studySummary.getTitle(), studySummaries.get(0).getTitle());
		Assert.assertEquals(studySummary.getObjective(), studySummaries.get(0).getObjective());
		Assert.assertEquals(studySummary.getStartDate(), studySummaries.get(0).getStartDate());
		Assert.assertEquals(studySummary.getEndDate(), studySummaries.get(0).getEndDate());
		Assert.assertEquals(studySummary.getType().toString(), studySummaries.get(0).getType());

	}

	@Test
	public void getStudyGermplasmList() throws MiddlewareQueryException {

		final List<StudyGermplasmDto> studyGermplasmTestData =
				Lists.newArrayList(factory.manufacturePojo(StudyGermplasmDto.class), factory.manufacturePojo(StudyGermplasmDto.class));
		Mockito.when(mockMiddlewareStudyService.getStudyGermplasmList(TEST_STUDY_IDENTIFIER)).thenReturn(studyGermplasmTestData);

		final Function<StudyGermplasmDto, StudyGermplasm> transformFunction = new Function<StudyGermplasmDto, StudyGermplasm>() {

			public StudyGermplasm apply(final StudyGermplasmDto studyGermplasmDto) {
				final StudyGermplasm studyGermplasm = new StudyGermplasm();
				studyGermplasm.setEntryNo(studyGermplasmDto.getEntryNo());
				studyGermplasm.setEntryType(studyGermplasmDto.getEntryType());
				studyGermplasm.setPosition(studyGermplasmDto.getPosition());
				studyGermplasm.setGermplasmListEntrySummary(new GermplasmListEntrySummary(studyGermplasmDto.getGermplasmId(),
						studyGermplasmDto.getDesignation(), studyGermplasmDto.getSeedSource(), studyGermplasmDto.getEntryCode(),
						studyGermplasmDto.getCross()));
				return studyGermplasm;
			}
		};

		final List<StudyGermplasm> studyGermplasmList = studyServiceImpl.getStudyGermplasmList(2013);

		final List<StudyGermplasm> expectedResults = Lists.transform(studyGermplasmTestData, transformFunction);
		Assert.assertEquals(expectedResults, studyGermplasmList);
	}

	@Test
	public void getObservations() {
		final List<ObservationDto> observationDtoTestData =
				Lists.newArrayList(factory.manufacturePojo(ObservationDto.class), factory.manufacturePojo(ObservationDto.class));
		Mockito.when(mockMiddlewareStudyService.getObservations(TEST_STUDY_IDENTIFIER)).thenReturn(observationDtoTestData);

		final List<Observation> actualObservations = studyServiceImpl.getObservations(TEST_STUDY_IDENTIFIER);

		Assert.assertEquals(Lists.transform(observationDtoTestData, observationTransformFunction), actualObservations);

	}

	@Test
	public void getSingleObservations() {
		final List<ObservationDto> observationDtoTestData = Lists.newArrayList(factory.manufacturePojo(ObservationDto.class));
		Mockito.when(mockMiddlewareStudyService.getSingleObservation(TEST_STUDY_IDENTIFIER, TEST_OBSERVATION_IDENTIIFER)).thenReturn(
				observationDtoTestData);

		final Observation actualObservations = studyServiceImpl.getSingleObservation(TEST_STUDY_IDENTIFIER, TEST_OBSERVATION_IDENTIIFER);

		Assert.assertEquals(Lists.transform(observationDtoTestData, observationTransformFunction).get(0), actualObservations);

	}

	private Observation mapObservationDtoToObservation(ObservationDto measurement) {
		final Observation observation = new Observation();
		if (measurement != null) {
			observation.setUniqueIdentifier(measurement.getMeasurementId());
			observation.setEnrtyNumber(measurement.getEntryNo());
			observation.setEntryType(measurement.getEntryType());
			observation.setEnvironmentNumber(measurement.getTrialInstance());
			observation.setGermplasmDesignation(measurement.getDesignation());
			observation.setGermplasmId(measurement.getGid());
			observation.setPlotNumber(measurement.getPlotNumber());
			observation.setReplicationNumber(measurement.getRepitionNumber());
			observation.setSeedSource(measurement.getSeedSource());

			final List<MeasurementDto> traits = measurement.getTraitMeasurements();
			final List<Measurement> measurements = new ArrayList<Measurement>();
			for (final MeasurementDto trait : traits) {
				measurements.add(new Measurement(new MeasurementIdentifier(trait.getPhenotypeId(), new Trait(trait.getTrait().getTraitId(),
						trait.getTrait().getTraitName())), trait.getTriatValue()));
			}

			observation.setMeasurements(measurements);
		}
		return observation;
	}
}
