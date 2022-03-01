package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.generationcp.middleware.api.ontology.OntologyVariableService;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.ibp.api.domain.ontology.AnalysisVariablesImportRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class AnalysisVariablesRequestValidatorTest {

	@Mock
	private BindingResult errors;

	@Mock
	private OntologyVariableService ontologyVariableService;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@InjectMocks
	private final AnalysisVariablesRequestValidator analysisVariablesRequestValidator = new AnalysisVariablesRequestValidator();

	@Test
	public void testValidate_RequiredFields() {
		final AnalysisVariablesImportRequest analysisVariablesImportRequest = new AnalysisVariablesImportRequest();
		this.analysisVariablesRequestValidator.validate(analysisVariablesImportRequest, this.errors);
		Mockito.verify(this.errors).reject("analysis.variable.request.variable.type.is.required", "");
		Mockito.verify(this.errors).reject("analysis.variable.request.analysis.method.names.are.required", "");
		Mockito.verify(this.errors).reject("analysis.variable.request.variable.ids.are.required", "");
	}

	@Test
	public void testValidate_DuplicateAnalysisNames() {
		final List<Integer> variableIds = Arrays.asList(1, 2, 3);
		final Map<Integer, Variable> variableMap =
			this.createTestVariableMap(DataType.NUMERIC_VARIABLE, variableIds, new ArrayList<>());
		Mockito.when(this.ontologyVariableService.getVariablesWithFilterById(any())).thenReturn(variableMap);

		final Multimap<Integer, VariableType> variableTypeMultimap = ArrayListMultimap.create();
		variableTypeMultimap.put(1, VariableType.TRAIT);
		variableTypeMultimap.put(2, VariableType.TRAIT);
		variableTypeMultimap.put(3, VariableType.TRAIT);
		Mockito.when(this.ontologyVariableService.getVariableTypesOfVariables(any())).thenReturn(variableTypeMultimap);

		final AnalysisVariablesImportRequest analysisVariablesImportRequest = new AnalysisVariablesImportRequest();
		analysisVariablesImportRequest.setAnalysisMethodNames(Arrays.asList("BLUEs", "blues", "BLUPs"));
		analysisVariablesImportRequest.setVariableIds(variableIds);
		analysisVariablesImportRequest.setVariableType(VariableType.ANALYSIS.getName());
		this.analysisVariablesRequestValidator.validate(analysisVariablesImportRequest, this.errors);
		Mockito.verify(this.errors).reject("analysis.variable.request.duplicate.analysis.method.names", "");
	}

	@Test
	public void testValidate_InvalidVariableType() {
		final List<Integer> variableIds = Arrays.asList(1, 2, 3);
		final Map<Integer, Variable> variableMap =
			this.createTestVariableMap(DataType.NUMERIC_VARIABLE, variableIds, new ArrayList<>());
		Mockito.when(this.ontologyVariableService.getVariablesWithFilterById(any())).thenReturn(variableMap);

		final Multimap<Integer, VariableType> variableTypeMultimap = ArrayListMultimap.create();
		variableTypeMultimap.put(1, VariableType.TRAIT);
		variableTypeMultimap.put(2, VariableType.TRAIT);
		variableTypeMultimap.put(3, VariableType.TRAIT);
		Mockito.when(this.ontologyVariableService.getVariableTypesOfVariables(any())).thenReturn(variableTypeMultimap);

		final AnalysisVariablesImportRequest analysisVariablesImportRequest = new AnalysisVariablesImportRequest();
		analysisVariablesImportRequest.setAnalysisMethodNames(Arrays.asList("BLUEs", "BLUPs"));
		analysisVariablesImportRequest.setVariableIds(variableIds);
		analysisVariablesImportRequest.setVariableType("Random Variable Type");
		this.analysisVariablesRequestValidator.validate(analysisVariablesImportRequest, this.errors);
		Mockito.verify(this.errors).reject("analysis.variable.request.invalid.variable.type", "");
	}

	@Test
	public void testValidate_VariablesShouldBeNumeric() {
		final List<Integer> variableIds = Arrays.asList(1, 2, 3);
		final Map<Integer, Variable> variableMap =
			this.createTestVariableMap(DataType.CHARACTER_VARIABLE, variableIds, new ArrayList<>());
		Mockito.when(this.ontologyVariableService.getVariablesWithFilterById(any())).thenReturn(variableMap);

		final Multimap<Integer, VariableType> variableTypeMultimap = ArrayListMultimap.create();
		variableTypeMultimap.put(1, VariableType.TRAIT);
		variableTypeMultimap.put(2, VariableType.TRAIT);
		variableTypeMultimap.put(3, VariableType.TRAIT);
		Mockito.when(this.ontologyVariableService.getVariableTypesOfVariables(any())).thenReturn(variableTypeMultimap);

		final AnalysisVariablesImportRequest analysisVariablesImportRequest = new AnalysisVariablesImportRequest();
		analysisVariablesImportRequest.setAnalysisMethodNames(Arrays.asList("BLUEs", "BLUPs"));
		analysisVariablesImportRequest.setVariableIds(variableIds);
		analysisVariablesImportRequest.setVariableType(VariableType.ANALYSIS.getName());
		this.analysisVariablesRequestValidator.validate(analysisVariablesImportRequest, this.errors);

		Mockito.verify(this.errors)
			.reject("analysis.variable.request.variables.should.be.numeric.data.type.or.categorical.variable.values.are.numeric", "");
	}

	@Test
	public void testValidate_VariablesShouldBeNumeric_VariableIsCategoricalButValuesAreNonNumeric() {
		final List<Integer> variableIds = Arrays.asList(1, 2, 3);
		final Map<Integer, Variable> variableMap =
			this.createTestVariableMap(DataType.CATEGORICAL_VARIABLE, variableIds, Arrays.asList("A", "B", "C"));
		variableMap.put(3, this.createVariable(3, DataType.NUMERIC_VARIABLE, new ArrayList<>()));
		Mockito.when(this.ontologyVariableService.getVariablesWithFilterById(any())).thenReturn(variableMap);

		final Multimap<Integer, VariableType> variableTypeMultimap = ArrayListMultimap.create();
		variableTypeMultimap.put(1, VariableType.TRAIT);
		variableTypeMultimap.put(2, VariableType.TRAIT);
		variableTypeMultimap.put(3, VariableType.TRAIT);
		Mockito.when(this.ontologyVariableService.getVariableTypesOfVariables(any())).thenReturn(variableTypeMultimap);

		final AnalysisVariablesImportRequest analysisVariablesImportRequest = new AnalysisVariablesImportRequest();
		analysisVariablesImportRequest.setAnalysisMethodNames(Arrays.asList("BLUEs", "BLUPs"));
		analysisVariablesImportRequest.setVariableIds(variableIds);
		analysisVariablesImportRequest.setVariableType(VariableType.ANALYSIS.getName());
		this.analysisVariablesRequestValidator.validate(analysisVariablesImportRequest, this.errors);

		Mockito.verify(this.errors)
			.reject("analysis.variable.request.variables.should.be.numeric.data.type.or.categorical.variable.values.are.numeric", "");
	}

	@Test
	public void testValidate_VariablesShouldBeNumeric_VariableIsCategoricalButValuesAreNumeric() {
		final List<Integer> variableIds = Arrays.asList(1, 2, 3);
		final Map<Integer, Variable> variableMap =
			this.createTestVariableMap(DataType.CATEGORICAL_VARIABLE, variableIds, Arrays.asList("1", "2", "3"));
		variableMap.put(3, this.createVariable(3, DataType.NUMERIC_VARIABLE, new ArrayList<>()));
		Mockito.when(this.ontologyVariableService.getVariablesWithFilterById(any())).thenReturn(variableMap);

		final Multimap<Integer, VariableType> variableTypeMultimap = ArrayListMultimap.create();
		variableTypeMultimap.put(1, VariableType.TRAIT);
		variableTypeMultimap.put(2, VariableType.TRAIT);
		variableTypeMultimap.put(3, VariableType.TRAIT);
		Mockito.when(this.ontologyVariableService.getVariableTypesOfVariables(any())).thenReturn(variableTypeMultimap);

		final AnalysisVariablesImportRequest analysisVariablesImportRequest = new AnalysisVariablesImportRequest();
		analysisVariablesImportRequest.setAnalysisMethodNames(Arrays.asList("BLUEs", "BLUPs"));
		analysisVariablesImportRequest.setVariableIds(variableIds);
		analysisVariablesImportRequest.setVariableType(VariableType.ANALYSIS.getName());
		this.analysisVariablesRequestValidator.validate(analysisVariablesImportRequest, this.errors);

		Mockito.verify(this.errors, Mockito.times(0))
			.reject("analysis.variable.request.variables.should.be.numeric.data.type.or.categorical.variable.values.are.numeric", "");
	}

	@Test
	public void testValidate_VariablesShouldBeTrait() {
		final List<Integer> variableIds = Arrays.asList(1, 2, 3);
		final Map<Integer, Variable> variableMap =
			this.createTestVariableMap(DataType.NUMERIC_VARIABLE, variableIds, new ArrayList<>());
		Mockito.when(this.ontologyVariableService.getVariablesWithFilterById(any())).thenReturn(variableMap);

		final Multimap<Integer, VariableType> variableTypeMultimap = ArrayListMultimap.create();
		variableTypeMultimap.put(1, VariableType.TRAIT);
		variableTypeMultimap.put(2, VariableType.TRAIT);
		variableTypeMultimap.put(3, VariableType.ENTRY_DETAIL);
		Mockito.when(this.ontologyVariableService.getVariableTypesOfVariables(any())).thenReturn(variableTypeMultimap);

		final AnalysisVariablesImportRequest analysisVariablesImportRequest = new AnalysisVariablesImportRequest();
		analysisVariablesImportRequest.setAnalysisMethodNames(Arrays.asList("BLUEs", "BLUPs"));
		analysisVariablesImportRequest.setVariableIds(variableIds);
		analysisVariablesImportRequest.setVariableType(VariableType.ANALYSIS.getName());
		this.analysisVariablesRequestValidator.validate(analysisVariablesImportRequest, this.errors);

		Mockito.verify(this.errors).reject("analysis.variable.request.variables.should.be.traits", "");
	}

	@Test
	public void testValidate_SomeVariablesDoNotExist() {
		final List<Integer> variableIds = Arrays.asList(1, 2, 3);
		final Map<Integer, Variable> variableMap =
			this.createTestVariableMap(DataType.NUMERIC_VARIABLE, variableIds, new ArrayList<>());
		Mockito.when(this.ontologyVariableService.getVariablesWithFilterById(any())).thenReturn(variableMap);

		final Multimap<Integer, VariableType> variableTypeMultimap = ArrayListMultimap.create();
		variableTypeMultimap.put(1, VariableType.TRAIT);
		variableTypeMultimap.put(2, VariableType.TRAIT);
		variableTypeMultimap.put(3, VariableType.TRAIT);
		Mockito.when(this.ontologyVariableService.getVariableTypesOfVariables(any())).thenReturn(variableTypeMultimap);

		final List<Term> terms = Arrays.asList(new Term(1, "", ""));
		Mockito.when(this.ontologyDataManager.getTermsByIds(any())).thenReturn(terms);

		final AnalysisVariablesImportRequest analysisVariablesImportRequest = new AnalysisVariablesImportRequest();
		analysisVariablesImportRequest.setAnalysisMethodNames(Arrays.asList("BLUEs", "BLUPs"));
		analysisVariablesImportRequest.setVariableIds(variableIds);
		analysisVariablesImportRequest.setVariableType(VariableType.ANALYSIS_SUMMARY.getName());
		this.analysisVariablesRequestValidator.validate(analysisVariablesImportRequest, this.errors);

		Mockito.verify(this.errors, Mockito.times(2)).reject(eq("variable.does.not.exist"), any(), any());
	}

	@Test
	public void testValidate_OK() {
		final List<Integer> variableIds = Arrays.asList(1, 2, 3);
		final Map<Integer, Variable> variableMap =
			this.createTestVariableMap(DataType.NUMERIC_VARIABLE, variableIds, new ArrayList<>());
		Mockito.when(this.ontologyVariableService.getVariablesWithFilterById(any())).thenReturn(variableMap);

		final Multimap<Integer, VariableType> variableTypeMultimap = ArrayListMultimap.create();
		variableTypeMultimap.put(1, VariableType.TRAIT);
		variableTypeMultimap.put(2, VariableType.TRAIT);
		variableTypeMultimap.put(3, VariableType.TRAIT);
		Mockito.when(this.ontologyVariableService.getVariableTypesOfVariables(any())).thenReturn(variableTypeMultimap);

		final List<Term> terms = Arrays.asList(new Term(1, "", ""), new Term(2, "", ""), new Term(3, "", ""));
		Mockito.when(this.ontologyDataManager.getTermsByIds(any())).thenReturn(terms);

		final AnalysisVariablesImportRequest analysisVariablesImportRequest = new AnalysisVariablesImportRequest();
		analysisVariablesImportRequest.setAnalysisMethodNames(Arrays.asList("BLUEs", "BLUPs"));
		analysisVariablesImportRequest.setVariableIds(variableIds);
		analysisVariablesImportRequest.setVariableType(VariableType.ANALYSIS_SUMMARY.getName());
		this.analysisVariablesRequestValidator.validate(analysisVariablesImportRequest, this.errors);
		Mockito.verifyNoInteractions(this.errors);
	}

	private Map<Integer, Variable> createTestVariableMap(final DataType dataType,
		final List<Integer> variableIds, final List<String> categoricalValues) {
		final Map<Integer, Variable> variableMap = new HashMap<>();
		for (final Integer id : variableIds) {
			final Variable variable = this.createVariable(id, dataType, categoricalValues);
			variableMap.put(variable.getId(), variable);
		}
		return variableMap;
	}

	private Variable createVariable(final Integer id, final DataType dataType, final List<String> categoricalValues) {
		final Variable variable = new Variable();
		variable.setId(id);
		variable.setScale(new Scale());
		variable.getScale().setDataType(dataType);
		for (final String categoricalValue : categoricalValues) {
			variable.getScale().addCategory(new TermSummary(1, categoricalValue, ""));
		}
		return variable;
	}

}
