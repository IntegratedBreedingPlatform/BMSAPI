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
import org.ibp.api.domain.dataset.DatasetVariable;
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
		final List<Integer> traitIds = Arrays.asList(1, 2, 3);

		this.studyDatasetService.countPhenotypes(studyId, datasetId, traitIds);
		Mockito.verify(this.studyValidator).validate(studyId, false);
		Mockito.verify(this.middlewareDatasetService).countPhenotypes(datasetId, traitIds);
	}

	@Test
	public void testCountPhenotypesByInstance() {

		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final int instanceId = random.nextInt();
		this.studyDatasetService.countPhenotypesByInstance(studyId, datasetId, instanceId);
		Mockito.verify(this.studyValidator).validate(studyId, false);
		Mockito.verify(this.middlewareDatasetService).countPhenotypesByInstance(datasetId, instanceId);
	}

	@Test
	public void testAddDatasetVariable() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final VariableType variableType = VariableType.SELECTION_METHOD;
		final int variableTypeId = variableType.getId();
		final int variableId = random.nextInt();
		final String alias = RandomStringUtils.randomAlphabetic(20);
		final DatasetVariable datasetVariable = new DatasetVariable(variableTypeId, variableId, alias);
		Mockito.doReturn(this.standardVariable).when(this.datasetValidator)
			.validateDatasetVariable(studyId, datasetId, true, datasetVariable, false);
		Mockito.doReturn(this.variable).when(this.measurementVariableTransformer).transform(this.standardVariable, false);

		this.studyDatasetService.addDatasetVariable(studyId, datasetId, datasetVariable);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.datasetValidator).validateDatasetVariable(studyId, datasetId, true, datasetVariable, false);
		Mockito.verify(this.middlewareDatasetService).addVariable(datasetId, variableId, variableType, alias);
		Mockito.verify(this.measurementVariableTransformer).transform(this.standardVariable, false);
		Mockito.verify(this.variable).setName(alias);
		Mockito.verify(this.variable).setVariableType(variableType);
		Mockito.verify(this.variable).setRequired(false);
	}
}
