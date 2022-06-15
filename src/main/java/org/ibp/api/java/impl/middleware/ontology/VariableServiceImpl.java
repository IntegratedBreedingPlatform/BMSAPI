
package org.ibp.api.java.impl.middleware.ontology;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.generationcp.commons.derivedvariable.DerivedVariableUtils;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.ontology.AnalysisVariablesImportRequest;
import org.generationcp.middleware.api.ontology.OntologyVariableService;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.OntologyVariableInfo;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.Util;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.ServiceBaseImpl;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.AnalysisVariablesImportRequestValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.VariableValidator;
import org.ibp.api.java.ontology.VariableService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Validate data of API Services and pass data to middleware services
 */

@Service
@Transactional
public class VariableServiceImpl extends ServiceBaseImpl implements VariableService {

	private static final String VARIABLE_NAME = "Variable";
	private static final String ERROR_MESSAGE = "Error!";
	private static final List<VariableType> EDITABLE_VARIABLES_TYPES = Arrays
		.asList(VariableType.TRAIT, VariableType.SELECTION_METHOD, VariableType.ENVIRONMENT_CONDITION, VariableType.GERMPLASM_ATTRIBUTE,
			VariableType.GERMPLASM_PASSPORT, VariableType.ENTRY_DETAIL, VariableType.INVENTORY_ATTRIBUTE);
	private static final List<Integer> EDITABLE_VARIABLES_TYPE_IDS = Arrays.asList( //
		VariableType.TRAIT.getId(), //
		VariableType.SELECTION_METHOD.getId(), //
		VariableType.ENVIRONMENT_CONDITION.getId(), //
		VariableType.GERMPLASM_ATTRIBUTE.getId(), //
		VariableType.INVENTORY_ATTRIBUTE.getId(), //
		VariableType.GERMPLASM_PASSPORT.getId(),
		VariableType.ENTRY_DETAIL.getId());

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private VariableValidator variableValidator;

	@Autowired
	private ProgramValidator programValidator;

	@Autowired
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Autowired
	private OntologyVariableService ontologyVariableService;

	@Autowired
	private AnalysisVariablesImportRequestValidator analysisVariablesRequestValidator;

	@Override
	public List<VariableDetails> getAllVariablesByFilter(final String cropName, final String programId, final String propertyId,
		final Boolean favourite) {

		final BindingResult bindingResult = new MapBindingResult(new HashMap<>(), VariableServiceImpl.VARIABLE_NAME);

		final ProgramDTO program = new ProgramDTO();
		program.setCrop(cropName);
		program.setUniqueID(programId);

		this.programValidator.validate(program, bindingResult);

		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		this.setCurrentProgram(programId);

		if (!Strings.isNullOrEmpty(propertyId)) {
			this.validateId(propertyId, VariableServiceImpl.VARIABLE_NAME);
		}

		try {
			final org.generationcp.middleware.manager.ontology.daoElements.VariableFilter middlewareVariableFilter =
				new org.generationcp.middleware.manager.ontology.daoElements.VariableFilter();
			middlewareVariableFilter.setProgramUuid(programId);
			if (favourite != null) {
				middlewareVariableFilter.setFavoritesOnly(favourite);
			}

			final Integer property = StringUtil.parseInt(propertyId, null);
			if (property != null) {
				middlewareVariableFilter.addPropertyId(property);
			}

			final List<Variable> variables = this.ontologyVariableDataManager.getWithFilter(middlewareVariableFilter);
			final List<VariableDetails> variableDetailsList = new ArrayList<>();

			final ModelMapper mapper = OntologyMapper.getInstance();

			for (final Variable variable : variables) {
				final VariableDetails variableSummary = mapper.map(variable, VariableDetails.class);
				variableDetailsList.add(variableSummary);
			}
			return variableDetailsList;
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException(VariableServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public List<VariableDetails> getVariablesByFilter(final String cropName, final String programId, final VariableFilter variableFilter) {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<>(), VariableServiceImpl.VARIABLE_NAME);

		final ProgramDTO program = new ProgramDTO();
		program.setCrop(cropName);
		program.setUniqueID(programId);

		this.programValidator.validate(program, bindingResult);

		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		this.setCurrentProgram(programId);

		try {

			final ModelMapper mapper = OntologyMapper.getInstance();

			final org.generationcp.middleware.manager.ontology.daoElements.VariableFilter middlewareVariableFilter =
				new org.generationcp.middleware.manager.ontology.daoElements.VariableFilter();

			this.mapVariableFilter(variableFilter, middlewareVariableFilter);

			final List<Variable> variables = this.ontologyVariableDataManager.getWithFilter(middlewareVariableFilter);
			final List<VariableDetails> variableDetailsList = new ArrayList<>();

			for (final Variable variable : variables) {
				final VariableDetails variableSummary = mapper.map(variable, VariableDetails.class);
				variableDetailsList.add(variableSummary);
			}
			return variableDetailsList;
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException(VariableServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public VariableDetails getVariableById(final String cropName, final String programId, final String variableId) {

		this.validateId(variableId, VariableServiceImpl.VARIABLE_NAME);
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableServiceImpl.VARIABLE_NAME);

		final ProgramDTO program = new ProgramDTO();
		program.setCrop(cropName);
		program.setUniqueID(programId);

		this.programValidator.validate(program, errors);

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		this.setCurrentProgram(programId);

		final TermRequest term = new TermRequest(variableId, VariableServiceImpl.VARIABLE_NAME, CvId.VARIABLES.getId());
		this.termValidator.validate(term, errors);

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		try {
			final Integer id = StringUtil.parseInt(variableId, null);

			final Variable ontologyVariable = this.ontologyVariableDataManager.getVariable(programId, id, true);
			this.ontologyVariableDataManager.fillVariableUsage(ontologyVariable);

			final FormulaDto formula = ontologyVariable.getFormula();
			if (formula != null) {
				final Map<String, FormulaVariable> formulaVariableMap =
					Maps.uniqueIndex(formula.getInputs(), formulaVariable -> String.valueOf(formulaVariable.getId()));
				formula.setDefinition(DerivedVariableUtils.getEditableFormat(formula.getDefinition(), formulaVariableMap));

			}

			if (ontologyVariable == null) {
				return null;
			}

			boolean deletable = true;

			if (Boolean.TRUE.equals(ontologyVariable.getHasUsage())) {
				deletable = false;
			}

			final ModelMapper mapper = OntologyMapper.getInstance();
			final VariableDetails response = mapper.map(ontologyVariable, VariableDetails.class);

			if (ontologyVariable.getIsSystem()) {
				response.getMetadata().setEditable(false);
				response.getMetadata().setDeletable(false);
				response.getMetadata().getUsage().setSystemVariable(true);
				return response;
			}

			if (!deletable) {
				if (CollectionUtils.containsAny(ontologyVariable.getVariableTypes(), VariableServiceImpl.EDITABLE_VARIABLES_TYPES)) {
					response.getMetadata().addEditableField("alias");
					response.getMetadata().addEditableField("expectedRange");
				}
				response.getMetadata().addEditableField("description");
			} else {
				response.getMetadata().addEditableField("name");
				response.getMetadata().addEditableField("description");
				if (CollectionUtils.containsAny(ontologyVariable.getVariableTypes(), VariableServiceImpl.EDITABLE_VARIABLES_TYPES)) {
					response.getMetadata().addEditableField("alias");
				}
				response.getMetadata().addEditableField("cropOntologyId");
				response.getMetadata().addEditableField("variableTypes");
				response.getMetadata().addEditableField("property");
				response.getMetadata().addEditableField("method");
				response.getMetadata().addEditableField("scale");
				response.getMetadata().addEditableField("expectedRange");
			}

			response.getMetadata().setDeletable(deletable);
			return response;
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException(VariableServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public GenericResponse addVariable(final String cropName, final String programId, final VariableDetails variable) {

		variable.setId(null);
		variable.setProgramUuid(programId);

		final ProgramDTO program = new ProgramDTO();
		program.setCrop(cropName);
		program.setUniqueID(programId);

		try {

			final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableServiceImpl.VARIABLE_NAME);
			this.programValidator.validate(program, errors);
			if (errors.hasErrors()) {
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			this.setCurrentProgram(programId);

			this.variableValidator.validate(variable, errors);
			if (errors.hasErrors()) {
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			this.formatVariableSummary(variable);

			final Integer methodId = StringUtil.parseInt(variable.getMethod().getId(), null);
			final Integer propertyId = StringUtil.parseInt(variable.getProperty().getId(), null);
			final Integer scaleId = StringUtil.parseInt(variable.getScale().getId(), null);

			final OntologyVariableInfo variableInfo = new OntologyVariableInfo();
			variableInfo.setName(variable.getName());
			variableInfo.setDescription(variable.getDescription());
			variableInfo.setMethodId(methodId);
			variableInfo.setPropertyId(propertyId);
			variableInfo.setScaleId(scaleId);
			variableInfo.setProgramUuid(variable.getProgramUuid());

			if (!Strings.isNullOrEmpty(variable.getExpectedRange().getMin())) {
				variableInfo.setExpectedMin(variable.getExpectedRange().getMin());
			}

			if (!Strings.isNullOrEmpty(variable.getExpectedRange().getMax())) {
				variableInfo.setExpectedMax(variable.getExpectedRange().getMax());
			}

			for (final org.ibp.api.domain.ontology.VariableType variableType : variable.getVariableTypes()) {
				variableInfo.addVariableType(VariableType.getById(this.parseVariableTypeAsInteger(variableType)));
			}

			for (final org.ibp.api.domain.ontology.VariableType variableType : variable.getVariableTypes()) {
				if (VariableServiceImpl.EDITABLE_VARIABLES_TYPE_IDS.contains(Integer.valueOf(variableType.getId()))) {
					variableInfo.setAlias(variable.getAlias());
				}
			}

			this.ontologyVariableDataManager.addVariable(variableInfo);
			return new GenericResponse(String.valueOf(variableInfo.getId()));
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException(VariableServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public void updateVariable(final String cropName, final String programId, final String variableId, final VariableDetails variable) {

		variable.setId(variableId);
		variable.setProgramUuid(programId);

		final ProgramDTO program = new ProgramDTO();
		program.setCrop(cropName);
		program.setUniqueID(programId);

		try {

			final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableServiceImpl.VARIABLE_NAME);

			this.programValidator.validate(program, errors);

			if (errors.hasErrors()) {
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			this.setCurrentProgram(programId);

			this.validateId(variableId, VariableServiceImpl.VARIABLE_NAME);
			final TermRequest term = new TermRequest(variableId, VariableServiceImpl.VARIABLE_NAME, CvId.VARIABLES.getId());
			this.termValidator.validate(term, errors);

			if (errors.hasErrors()) {
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			this.variableValidator.validate(variable, errors);
			if (errors.hasErrors()) {
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			this.formatVariableSummary(variable);

			final Integer id = StringUtil.parseInt(variable.getId(), null);

			final Integer methodId = StringUtil.parseInt(variable.getMethod().getId(), null);
			final Integer propertyId = StringUtil.parseInt(variable.getProperty().getId(), null);
			final Integer scaleId = StringUtil.parseInt(variable.getScale().getId(), null);

			final OntologyVariableInfo variableInfo = new OntologyVariableInfo();
			variableInfo.setId(id);
			variableInfo.setProgramUuid(variable.getProgramUuid());
			variableInfo.setName(variable.getName());
			variableInfo.setAlias(variable.getAlias());
			variableInfo.setDescription(variable.getDescription());
			variableInfo.setMethodId(methodId);
			variableInfo.setPropertyId(propertyId);
			variableInfo.setScaleId(scaleId);
			variableInfo.setIsFavorite(variable.isFavourite());
			variableInfo.setProgramUuid(variable.getProgramUuid());

			if (!Strings.isNullOrEmpty(variable.getExpectedRange().getMin())) {
				variableInfo.setExpectedMin(variable.getExpectedRange().getMin());
			}

			if (!Strings.isNullOrEmpty(variable.getExpectedRange().getMax())) {
				variableInfo.setExpectedMax(variable.getExpectedRange().getMax());
			}

			for (final org.ibp.api.domain.ontology.VariableType variableType : variable.getVariableTypes()) {
				variableInfo.addVariableType(VariableType.getById(this.parseVariableTypeAsInteger(variableType)));
			}

			this.ontologyVariableDataManager.updateVariable(variableInfo);
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException(VariableServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public void deleteVariable(final String id) {

		// Note: Validate Id for valid format and check if variable exists or not
		this.validateId(id, VariableServiceImpl.VARIABLE_NAME);
		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableServiceImpl.VARIABLE_NAME);

		// Note: Check if variable is deletable or not by checking its usage in variable
		this.termDeletableValidator.validate(new TermRequest(String.valueOf(id), VariableServiceImpl.VARIABLE_NAME, CvId.VARIABLES.getId()),
			errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		try {
			this.ontologyVariableDataManager.deleteVariable(StringUtil.parseInt(id, null));
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException(VariableServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public void deleteVariablesFromCache(final String cropName, final Integer[] variablesIds, final String programId) {

		final BindingResult bindingResult = new MapBindingResult(new HashMap<>(), VariableServiceImpl.VARIABLE_NAME);

		final ProgramDTO program = new ProgramDTO();
		program.setCrop(cropName);
		program.setUniqueID(programId);

		this.programValidator.validate(program, bindingResult);

		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		this.setCurrentProgram(programId);

		for (final Integer variableId : variablesIds) {
			this.validateId(String.valueOf(variableId), VariableServiceImpl.VARIABLE_NAME);
			final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableServiceImpl.VARIABLE_NAME);

			final TermRequest term = new TermRequest(String.valueOf(variableId), VariableServiceImpl.VARIABLE_NAME, CvId.VARIABLES.getId());
			this.termValidator.validate(term, errors);

			if (errors.hasErrors()) {
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}

		this.ontologyVariableDataManager.deleteVariablesFromCache(Arrays.asList(variablesIds));
	}

	protected void formatVariableSummary(final VariableDetails variableDetails) {

		final Integer scaleId = StringUtil.parseInt(variableDetails.getScale().getId(), null);

		// Should discard unwanted parameters. We do not want expected min/max values if associated data type is not numeric
		if (scaleId != null) {
			try {
				final Scale scale = this.ontologyScaleDataManager.getScaleById(scaleId, true);

				if (scale != null && !Objects.equals(scale.getDataType().getId(), DataType.NUMERIC_VARIABLE.getId())) {
					variableDetails.setExpectedMin(null);
					variableDetails.setExpectedMax(null);
				}

			} catch (final MiddlewareException e) {
				throw new ApiRuntimeException(VariableServiceImpl.ERROR_MESSAGE, e);
			}
		}
	}

	/**
	 * <p>
	 * Set current program in ContextHolder
	 * </p>
	 *
	 * <p>
	 * XXX: <br/>
	 * Perhaps this should be a url part the same way as cropName and extracted in ContextResolverImpl.resolveDatabaseFromUrl() but that
	 * would be a more extensive API change
	 * </p>
	 *
	 * @param programId the program unique id
	 */
	private void setCurrentProgram(final String programId) {
		ContextHolder.setCurrentProgram(programId);
	}

	private Integer parseVariableTypeAsInteger(final org.ibp.api.domain.ontology.VariableType variableType) {
		if (variableType == null) {
			return null;
		}
		return StringUtil.parseInt(variableType.getId(), null);
	}

	private void mapVariableFilter(final VariableFilter variableFilter,
		final org.generationcp.middleware.manager.ontology.daoElements.VariableFilter middlewareVariableFilter) {

		middlewareVariableFilter.setProgramUuid(variableFilter.getProgramUuid());

		if (!Util.isNullOrEmpty(variableFilter.getPropertyIds())) {
			variableFilter.getPropertyIds().forEach(middlewareVariableFilter::addPropertyId);
		}

		if (!Util.isNullOrEmpty(variableFilter.getMethodIds())) {
			variableFilter.getMethodIds().forEach(middlewareVariableFilter::addMethodId);
		}

		if (!Util.isNullOrEmpty(variableFilter.getScaleIds())) {
			variableFilter.getScaleIds().forEach(middlewareVariableFilter::addScaleId);
		}

		if (!Util.isNullOrEmpty(variableFilter.getVariableIds())) {
			variableFilter.getVariableIds().forEach(middlewareVariableFilter::addVariableId);
		}

		if (!Util.isNullOrEmpty(variableFilter.getExcludedVariableIds())) {
			variableFilter.getExcludedVariableIds().forEach(middlewareVariableFilter::addExcludedVariableId);
		}

		if (!Util.isNullOrEmpty(variableFilter.getDataTypes())) {
			variableFilter.getDataTypes().stream().map(DataType::getById).forEach(middlewareVariableFilter::addDataType);
		}

		if (!Util.isNullOrEmpty(variableFilter.getVariableTypes())) {
			variableFilter.getVariableTypes().stream().map(VariableType::getById).forEach(middlewareVariableFilter::addVariableType);
		}

		if (!Util.isNullOrEmpty(variableFilter.getPropertyClasses())) {
			variableFilter.getPropertyClasses().forEach(middlewareVariableFilter::addPropertyClass);
		}

		if (!Util.isNullOrEmpty(variableFilter.getNames())) {
			variableFilter.getNames().forEach(middlewareVariableFilter::addName);
		}

		if (!Util.isNullOrEmpty(variableFilter.getDatasetIds())) {
			variableFilter.getDatasetIds().forEach(middlewareVariableFilter::addDatasetId);
		}

		if (!Util.isNullOrEmpty(variableFilter.getGermplasmUUIDs())) {
			variableFilter.getGermplasmUUIDs().forEach(middlewareVariableFilter::addGermplasmUUID);
		}
	}

	@Override
	public List<VariableDetails> getVariablesByFilter(final VariableFilter variableFilter) {
		final org.generationcp.middleware.manager.ontology.daoElements.VariableFilter middlewareVariableFilter =
			new org.generationcp.middleware.manager.ontology.daoElements.VariableFilter();
		this.mapVariableFilter(variableFilter, middlewareVariableFilter);
		final List<Variable> variables = this.ontologyVariableDataManager.getWithFilter(middlewareVariableFilter);
		final List<VariableDetails> variableDetailsList = new ArrayList<>();
		final ModelMapper mapper = OntologyMapper.getInstance();
		for (final Variable variable : variables) {
			final VariableDetails variableSummary = mapper.map(variable, VariableDetails.class);
			variableDetailsList.add(variableSummary);
		}
		return variableDetailsList;
	}

	@Override
	public List<Variable> searchAttributeVariables(final String query, final String programUUID) {
		return this.ontologyVariableDataManager.searchAttributeVariables(query, programUUID);
	}

	@Override
	public List<VariableDetails> createAnalysisVariables(final AnalysisVariablesImportRequest analysisVariablesImportRequest) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		this.analysisVariablesRequestValidator.validate(analysisVariablesImportRequest, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final VariableFilter variableFilter = new VariableFilter();
		final List<Integer> analysisVariables =
			new ArrayList<Integer>(
				this.ontologyVariableService.createAnalysisVariables(analysisVariablesImportRequest, new HashMap<>()).values());
		analysisVariables.stream().forEach(variableFilter::addVariableId);
		return this.getVariablesByFilter(variableFilter);
	}

}
