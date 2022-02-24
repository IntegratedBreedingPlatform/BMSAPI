package org.ibp.api.java.impl.middleware.ontology.validator;

import org.generationcp.middleware.api.ontology.OntologyVariableService;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.ibp.api.domain.ontology.AnalysisVariablesRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class AnalysisVariablesRequestValidatorTest {

	@Mock
	private BindingResult errors;

	@Mock
	private OntologyVariableService ontologyVariableService;

	@InjectMocks
	private final AnalysisVariablesRequestValidator analysisVariablesRequestValidator = new AnalysisVariablesRequestValidator();

	@Test
	public void testValidate_RequiredFields() {
		final AnalysisVariablesRequest analysisVariablesRequest = new AnalysisVariablesRequest();
		this.analysisVariablesRequestValidator.validate(analysisVariablesRequest, this.errors);
		Mockito.verify(this.errors).reject("analysis.variable.request.variable.type.is.required", "");
		Mockito.verify(this.errors).reject("analysis.variable.request.analysis.names.are.required", "");
		Mockito.verify(this.errors).reject("analysis.variable.request.variable.ids.are.required", "");
	}

	@Test
	public void testValidate_DuplicateAnalysisNames() {
		final AnalysisVariablesRequest analysisVariablesRequest = new AnalysisVariablesRequest();
		analysisVariablesRequest.setAnalysisNames(Arrays.asList("BLUEs", "blues", "BLUPs"));
		analysisVariablesRequest.setVariableIds(Arrays.asList(1, 2, 3));
		analysisVariablesRequest.setVariableType(VariableType.ANALYSIS.getName());
		this.analysisVariablesRequestValidator.validate(analysisVariablesRequest, this.errors);
		Mockito.verify(this.errors).reject("analysis.variable.request.duplicate.analysis.names", "");
	}

	@Test
	public void testValidate_InvalidVariableType() {
		final AnalysisVariablesRequest analysisVariablesRequest = new AnalysisVariablesRequest();
		analysisVariablesRequest.setAnalysisNames(Arrays.asList("BLUEs", "BLUPs"));
		analysisVariablesRequest.setVariableIds(Arrays.asList(1, 2, 3));
		analysisVariablesRequest.setVariableType("Random Variable Type");
		this.analysisVariablesRequestValidator.validate(analysisVariablesRequest, this.errors);
		Mockito.verify(this.errors).reject("analysis.variable.request.invalid.variable.type", "");
	}

	@Test
	public void testValidate_VariablesShouldBeNumeric() {
		final Variable variable = new Variable();
		variable.setId(1);
		variable.setScale(new Scale());
		variable.getScale().setDataType(DataType.CATEGORICAL_VARIABLE);
		final Map<Integer, Variable> variableMap = new HashMap<>();
		variableMap.put(variable.getId(), variable);
		Mockito.when(this.ontologyVariableService.getVariablesWithFilterById(any())).thenReturn(variableMap);

		final AnalysisVariablesRequest analysisVariablesRequest = new AnalysisVariablesRequest();
		analysisVariablesRequest.setAnalysisNames(Arrays.asList("BLUEs", "BLUPs"));
		analysisVariablesRequest.setVariableIds(Arrays.asList(1, 2, 3));
		analysisVariablesRequest.setVariableType(VariableType.ANALYSIS.getName());
		this.analysisVariablesRequestValidator.validate(analysisVariablesRequest, this.errors);

		Mockito.verify(this.errors).reject("analysis.variable.request.variables.should.be.numeric.data.type", "");
	}

	@Test
	public void testValidate_OK() {
		final AnalysisVariablesRequest analysisVariablesRequest = new AnalysisVariablesRequest();
		analysisVariablesRequest.setAnalysisNames(Arrays.asList("BLUEs", "BLUPs"));
		analysisVariablesRequest.setVariableIds(Arrays.asList(1, 2, 3));
		analysisVariablesRequest.setVariableType(VariableType.ANALYSIS_SUMMARY.getName());
		this.analysisVariablesRequestValidator.validate(analysisVariablesRequest, this.errors);
		Mockito.verifyNoInteractions(this.errors);
	}

}
