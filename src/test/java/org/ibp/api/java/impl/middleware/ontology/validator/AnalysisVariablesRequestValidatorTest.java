package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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
import java.util.List;
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
		final List<Integer> variableIds = Arrays.asList(1, 2, 3);
		final Map<Integer, Variable> variableMap =
			this.createTestVariableMap(DataType.NUMERIC_VARIABLE, variableIds);
		Mockito.when(this.ontologyVariableService.getVariablesWithFilterById(any())).thenReturn(variableMap);

		final Multimap<Integer, VariableType> variableTypeMultimap = ArrayListMultimap.create();
		variableTypeMultimap.put(1, VariableType.TRAIT);
		variableTypeMultimap.put(2, VariableType.TRAIT);
		variableTypeMultimap.put(3, VariableType.TRAIT);
		Mockito.when(this.ontologyVariableService.getVariableTypesOfVariables(any())).thenReturn(variableTypeMultimap);

		final AnalysisVariablesRequest analysisVariablesRequest = new AnalysisVariablesRequest();
		analysisVariablesRequest.setAnalysisNames(Arrays.asList("BLUEs", "blues", "BLUPs"));
		analysisVariablesRequest.setVariableIds(variableIds);
		analysisVariablesRequest.setVariableType(VariableType.ANALYSIS.getName());
		this.analysisVariablesRequestValidator.validate(analysisVariablesRequest, this.errors);
		Mockito.verify(this.errors).reject("analysis.variable.request.duplicate.analysis.names", "");
	}

	@Test
	public void testValidate_InvalidVariableType() {
		final List<Integer> variableIds = Arrays.asList(1, 2, 3);
		final Map<Integer, Variable> variableMap =
			this.createTestVariableMap(DataType.NUMERIC_VARIABLE, variableIds);
		Mockito.when(this.ontologyVariableService.getVariablesWithFilterById(any())).thenReturn(variableMap);

		final Multimap<Integer, VariableType> variableTypeMultimap = ArrayListMultimap.create();
		variableTypeMultimap.put(1, VariableType.TRAIT);
		variableTypeMultimap.put(2, VariableType.TRAIT);
		variableTypeMultimap.put(3, VariableType.TRAIT);
		Mockito.when(this.ontologyVariableService.getVariableTypesOfVariables(any())).thenReturn(variableTypeMultimap);

		final AnalysisVariablesRequest analysisVariablesRequest = new AnalysisVariablesRequest();
		analysisVariablesRequest.setAnalysisNames(Arrays.asList("BLUEs", "BLUPs"));
		analysisVariablesRequest.setVariableIds(variableIds);
		analysisVariablesRequest.setVariableType("Random Variable Type");
		this.analysisVariablesRequestValidator.validate(analysisVariablesRequest, this.errors);
		Mockito.verify(this.errors).reject("analysis.variable.request.invalid.variable.type", "");
	}

	@Test
	public void testValidate_VariablesShouldBeNumeric() {
		final List<Integer> variableIds = Arrays.asList(1, 2, 3);
		final Map<Integer, Variable> variableMap =
			this.createTestVariableMap(DataType.CHARACTER_VARIABLE, variableIds);
		Mockito.when(this.ontologyVariableService.getVariablesWithFilterById(any())).thenReturn(variableMap);

		final Multimap<Integer, VariableType> variableTypeMultimap = ArrayListMultimap.create();
		variableTypeMultimap.put(1, VariableType.TRAIT);
		variableTypeMultimap.put(2, VariableType.TRAIT);
		variableTypeMultimap.put(3, VariableType.TRAIT);
		Mockito.when(this.ontologyVariableService.getVariableTypesOfVariables(any())).thenReturn(variableTypeMultimap);

		final AnalysisVariablesRequest analysisVariablesRequest = new AnalysisVariablesRequest();
		analysisVariablesRequest.setAnalysisNames(Arrays.asList("BLUEs", "BLUPs"));
		analysisVariablesRequest.setVariableIds(variableIds);
		analysisVariablesRequest.setVariableType(VariableType.ANALYSIS.getName());
		this.analysisVariablesRequestValidator.validate(analysisVariablesRequest, this.errors);

		Mockito.verify(this.errors).reject("analysis.variable.request.variables.should.be.numeric.data.type", "");
	}

	@Test
	public void testValidate_VariablesShouldBeTrait() {
		final List<Integer> variableIds = Arrays.asList(1, 2, 3);
		final Map<Integer, Variable> variableMap =
			this.createTestVariableMap(DataType.NUMERIC_VARIABLE, variableIds);
		Mockito.when(this.ontologyVariableService.getVariablesWithFilterById(any())).thenReturn(variableMap);

		final Multimap<Integer, VariableType> variableTypeMultimap = ArrayListMultimap.create();
		variableTypeMultimap.put(1, VariableType.TRAIT);
		variableTypeMultimap.put(2, VariableType.TRAIT);
		variableTypeMultimap.put(3, VariableType.ENTRY_DETAIL);
		Mockito.when(this.ontologyVariableService.getVariableTypesOfVariables(any())).thenReturn(variableTypeMultimap);

		final AnalysisVariablesRequest analysisVariablesRequest = new AnalysisVariablesRequest();
		analysisVariablesRequest.setAnalysisNames(Arrays.asList("BLUEs", "BLUPs"));
		analysisVariablesRequest.setVariableIds(variableIds);
		analysisVariablesRequest.setVariableType(VariableType.ANALYSIS.getName());
		this.analysisVariablesRequestValidator.validate(analysisVariablesRequest, this.errors);

		Mockito.verify(this.errors).reject("analysis.variable.request.variables.should.be.traits", "");
	}

	@Test
	public void testValidate_OK() {
		final List<Integer> variableIds = Arrays.asList(1, 2, 3);
		final Map<Integer, Variable> variableMap =
			this.createTestVariableMap(DataType.NUMERIC_VARIABLE, variableIds);
		Mockito.when(this.ontologyVariableService.getVariablesWithFilterById(any())).thenReturn(variableMap);

		final Multimap<Integer, VariableType> variableTypeMultimap = ArrayListMultimap.create();
		variableTypeMultimap.put(1, VariableType.TRAIT);
		variableTypeMultimap.put(2, VariableType.TRAIT);
		variableTypeMultimap.put(3, VariableType.TRAIT);
		Mockito.when(this.ontologyVariableService.getVariableTypesOfVariables(any())).thenReturn(variableTypeMultimap);

		final AnalysisVariablesRequest analysisVariablesRequest = new AnalysisVariablesRequest();
		analysisVariablesRequest.setAnalysisNames(Arrays.asList("BLUEs", "BLUPs"));
		analysisVariablesRequest.setVariableIds(variableIds);
		analysisVariablesRequest.setVariableType(VariableType.ANALYSIS_SUMMARY.getName());
		this.analysisVariablesRequestValidator.validate(analysisVariablesRequest, this.errors);
		Mockito.verifyNoInteractions(this.errors);
	}

	private Map<Integer, Variable> createTestVariableMap(final DataType dataType,
		final List<Integer> variableIds) {
		final Map<Integer, Variable> variableMap = new HashMap<>();
		for (final Integer id : variableIds) {
			final Variable variable = new Variable();
			variable.setId(id);
			variable.setScale(new Scale());
			variable.getScale().setDataType(dataType);
			variableMap.put(variable.getId(), variable);
		}
		return variableMap;
	}

}
