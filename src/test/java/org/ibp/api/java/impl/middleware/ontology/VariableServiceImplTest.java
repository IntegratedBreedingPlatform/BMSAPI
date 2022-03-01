package org.ibp.api.java.impl.middleware.ontology;

import org.generationcp.middleware.api.ontology.OntologyVariableService;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.ibp.api.domain.ontology.AnalysisVariablesImportRequest;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.ontology.validator.AnalysisVariablesRequestValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.ontology.VariableService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class VariableServiceImplTest {

	@Mock
	private OntologyVariableService ontologyVariableService;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private TermValidator termValidator;

	@Mock
	private AnalysisVariablesRequestValidator analysisVariablesRequestValidator;

	@InjectMocks
	private VariableService variableService = new VariableServiceImpl();

	@Test
	public void testCreateAnalysisVariables_OK() {

		final AnalysisVariablesImportRequest analysisVariablesImportRequest = new AnalysisVariablesImportRequest();
		analysisVariablesImportRequest.setVariableType("Analysis");
		analysisVariablesImportRequest.setAnalysisMethodNames(Arrays.asList("BLUEs"));
		analysisVariablesImportRequest.setVariableIds(Arrays.asList(1));

		Mockito.when(this.ontologyVariableService.createAnalysisVariables(analysisVariablesImportRequest.getVariableIds(),
			analysisVariablesImportRequest.getAnalysisMethodNames(), analysisVariablesImportRequest.getVariableType())).thenReturn(Arrays.asList(1));
		Mockito.when(this.ontologyVariableDataManager.getWithFilter(any())).thenReturn(Arrays.asList(new Variable(2, "", "", "")));

		final List<VariableDetails> result = this.variableService.createAnalysisVariables(analysisVariablesImportRequest);

		Mockito.verify(this.analysisVariablesRequestValidator).validate(eq(analysisVariablesImportRequest), any());
		assertEquals("2", result.get(0).getId());

	}

	@Test
	public void testCreateAnalysisVariables_ValidationError() {

		final AnalysisVariablesImportRequest analysisVariablesImportRequest = new AnalysisVariablesImportRequest();
		analysisVariablesImportRequest.setVariableType("Analysis");
		analysisVariablesImportRequest.setAnalysisMethodNames(Arrays.asList("BLUEs"));
		analysisVariablesImportRequest.setVariableIds(Arrays.asList(1));

		Mockito.doAnswer(invocation -> {
			final BindingResult errors = invocation.getArgument(1, BindingResult.class);
			errors.reject("error");
			return null;
		}).when(this.analysisVariablesRequestValidator).validate(eq(analysisVariablesImportRequest), any());

		try {
			final List<VariableDetails> result = this.variableService.createAnalysisVariables(analysisVariablesImportRequest);
			Assert.fail("Should throw an error");
		} catch (final ApiRequestValidationException e) {
			Mockito.verify(this.ontologyVariableService, Mockito.times(0)).createAnalysisVariables(any(), any(), any());
			Mockito.verify(this.ontologyVariableDataManager, Mockito.times(0)).getWithFilter(any());
		}

	}

}
