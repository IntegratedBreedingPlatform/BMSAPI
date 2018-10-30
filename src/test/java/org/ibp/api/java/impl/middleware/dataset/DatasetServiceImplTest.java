package org.ibp.api.java.impl.middleware.dataset;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.domain.dataset.DatasetTrait;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class DatasetServiceImplTest {
	
	@Mock
	private DatasetService middlewareDatasetService;
	
	@Mock
	private StudyValidator studyValidator;
	
	@Mock
	private DatasetValidator datasetValidator;
	
	@Mock
	private MeasurementVariable variable;
	
	@Mock
	private StandardVariable standardVariable;
	
	@Mock
	private MeasurementVariableTransformer measurementVariableTransformer;
	
	@InjectMocks
	private DatasetServiceImpl studyDatasetService;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testCountPhenotypes() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final List<Integer> traitIds = Arrays.asList(1,2,3);
		this.studyDatasetService.countPhenotypes(studyId, datasetId, traitIds);
		Mockito.verify(this.studyValidator).validate(studyId, false);
		Mockito.verify(this.middlewareDatasetService).countPhenotypes(datasetId, traitIds);
	}
	
	@Test
	public void testAddDatasetTrait() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final int traitId = random.nextInt();
		final String alias = RandomStringUtils.randomAlphabetic(20);
		Mockito.doReturn(this.standardVariable).when(this.datasetValidator).validateDatasetTrait(studyId, datasetId, true, traitId, false);
		Mockito.doReturn(this.variable).when(this.measurementVariableTransformer).transform(this.standardVariable, false);
		
		this.studyDatasetService.addDatasetTrait(studyId, datasetId, new DatasetTrait(traitId, alias));
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.datasetValidator).validateDatasetTrait(studyId, datasetId, true, traitId, false);
		Mockito.verify(this.middlewareDatasetService).addTrait(datasetId, traitId, alias);
		Mockito.verify(this.measurementVariableTransformer).transform(this.standardVariable, false);
		Mockito.verify(this.variable).setName(alias);
		Mockito.verify(this.variable).setVariableType(VariableType.TRAIT);
		Mockito.verify(this.variable).setRequired(false);
	}
	
}
