
package org.ibp.api.java.impl.middleware.ontology;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.generationcp.commons.derivedvariable.DerivedVariableUtils;
import org.generationcp.middleware.ContextHolder;
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
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.Util;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.ServiceBaseImpl;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
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
	private static final List EDITABLE_VARIABLES_TYPES = Arrays.asList(VariableType.TRAIT, VariableType.SELECTION_METHOD, VariableType.ENVIRONMENT_CONDITION, VariableType.GERMPLASM_ATTRIBUTE, VariableType.GERMPLASM_PASSPORT);
	private static final List<Integer> EDITABLE_VARIABLES_TYPE_IDS = Arrays.asList( //
		VariableType.TRAIT.getId(), //
		VariableType.SELECTION_METHOD.getId(), //
		VariableType.ENVIRONMENT_CONDITION.getId(), //
		VariableType.GERMPLASM_ATTRIBUTE.getId(), //
		VariableType.GERMPLASM_PASSPORT.getId());

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private VariableValidator variableValidator;

	@Autowired
	private ProgramValidator programValidator;

	@Autowired
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Override
	public List<VariableDetails> getAllVariablesByFilter(String cropName, String programId, String propertyId, Boolean favourite) {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), VariableServiceImpl.VARIABLE_NAME);

		ProgramDTO program = new ProgramDTO();
		program.setCrop(cropName);
		program.setUniqueID(programId);

		this.programValidator.validate(program, bindingResult);

		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		setCurrentProgram(programId);

		if (!Strings.isNullOrEmpty(propertyId)) {
			this.validateId(propertyId, VariableServiceImpl.VARIABLE_NAME);
		}

		try {
			org.generationcp.middleware.manager.ontology.daoElements.VariableFilter middlewareVariableFilter =
					new org.generationcp.middleware.manager.ontology.daoElements.VariableFilter();
			middlewareVariableFilter.setProgramUuid(programId);
			if (favourite != null) {
				middlewareVariableFilter.setFavoritesOnly(favourite);
			}

			Integer property = StringUtil.parseInt(propertyId, null);
			if (property != null) {
				middlewareVariableFilter.addPropertyId(property);
			}
			List<Variable> variables = this.ontologyVariableDataManager.getWithFilter(middlewareVariableFilter);
			List<VariableDetails> variableDetailsList = new ArrayList<>();

			ModelMapper mapper = OntologyMapper.getInstance();

			for (Variable variable : variables) {
				VariableDetails variableSummary = mapper.map(variable, VariableDetails.class);
				variableDetailsList.add(variableSummary);
			}
			return variableDetailsList;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException(VariableServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public List<VariableDetails> getVariablesByFilter(String cropName, String programId, VariableFilter variableFilter) {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), VariableServiceImpl.VARIABLE_NAME);

		ProgramDTO program = new ProgramDTO();
		program.setCrop(cropName);
		program.setUniqueID(programId);

		this.programValidator.validate(program, bindingResult);

		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		setCurrentProgram(programId);

		try {

			ModelMapper mapper = OntologyMapper.getInstance();

			org.generationcp.middleware.manager.ontology.daoElements.VariableFilter middlewareVariableFilter =
					new org.generationcp.middleware.manager.ontology.daoElements.VariableFilter();

			this.mapVariableFilter(variableFilter, middlewareVariableFilter);

			List<Variable> variables = this.ontologyVariableDataManager.getWithFilter(middlewareVariableFilter);
			List<VariableDetails> variableDetailsList = new ArrayList<>();

			for (Variable variable : variables) {
				VariableDetails variableSummary = mapper.map(variable, VariableDetails.class);
				variableDetailsList.add(variableSummary);
			}
			return variableDetailsList;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException(VariableServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public VariableDetails getVariableById(String cropName, String programId, String variableId) {

		this.validateId(variableId, VariableServiceImpl.VARIABLE_NAME);
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), VariableServiceImpl.VARIABLE_NAME);

		ProgramDTO program = new ProgramDTO();
		program.setCrop(cropName);
		program.setUniqueID(programId);

		this.programValidator.validate(program, errors);

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		setCurrentProgram(programId);

		TermRequest term = new TermRequest(variableId, VariableServiceImpl.VARIABLE_NAME, CvId.VARIABLES.getId());
		this.termValidator.validate(term, errors);

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		try {
			Integer id = StringUtil.parseInt(variableId, null);

			Variable ontologyVariable = this.ontologyVariableDataManager.getVariable(programId, id, true);
			ontologyVariableDataManager.fillVariableUsage(ontologyVariable);

			final FormulaDto formula = ontologyVariable.getFormula();
			if (formula != null) {
				final Map<String, FormulaVariable> formulaVariableMap =
					Maps.uniqueIndex(formula.getInputs(), new Function<FormulaVariable, String>() {

						public String apply(FormulaVariable from) {
							return String.valueOf(from.getId());
						}
					});
				formula.setDefinition(DerivedVariableUtils.getEditableFormat(formula.getDefinition(), formulaVariableMap));

			}

			if (ontologyVariable == null) {
				return null;
			}

			boolean deletable = true;

			if (Boolean.TRUE.equals(ontologyVariable.getHasUsage())) {
				deletable = false;
			}

			ModelMapper mapper = OntologyMapper.getInstance();
			VariableDetails response = mapper.map(ontologyVariable, VariableDetails.class);

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
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException(VariableServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public GenericResponse addVariable(String cropName, String programId, VariableDetails variable) {

		variable.setId(null);
		variable.setProgramUuid(programId);

		ProgramDTO program = new ProgramDTO();
		program.setCrop(cropName);
		program.setUniqueID(programId);

		try {

			BindingResult errors = new MapBindingResult(new HashMap<String, String>(), VariableServiceImpl.VARIABLE_NAME);
			this.programValidator.validate(program, errors);
			if (errors.hasErrors()) {
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			setCurrentProgram(programId);

			this.variableValidator.validate(variable, errors);
			if (errors.hasErrors()) {
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			this.formatVariableSummary(variable);

			Integer methodId = StringUtil.parseInt(variable.getMethod().getId(), null);
			Integer propertyId = StringUtil.parseInt(variable.getProperty().getId(), null);
			Integer scaleId = StringUtil.parseInt(variable.getScale().getId(), null);

			OntologyVariableInfo variableInfo = new OntologyVariableInfo();
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

			for (org.ibp.api.domain.ontology.VariableType variableType : variable.getVariableTypes()) {
				variableInfo.addVariableType(VariableType.getById(this.parseVariableTypeAsInteger(variableType)));
			}

			for (org.ibp.api.domain.ontology.VariableType variableType : variable.getVariableTypes()) {
				if (VariableServiceImpl.EDITABLE_VARIABLES_TYPE_IDS.contains(Integer.valueOf(variableType.getId()))) {
					variableInfo.setAlias(variable.getAlias());
				}
			}

			this.ontologyVariableDataManager.addVariable(variableInfo);
			return new GenericResponse(String.valueOf(variableInfo.getId()));
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException(VariableServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public void updateVariable(String cropName, String programId, String variableId, VariableDetails variable) {

		variable.setId(variableId);
		variable.setProgramUuid(programId);

		ProgramDTO program = new ProgramDTO();
		program.setCrop(cropName);
		program.setUniqueID(programId);

		try {

			BindingResult errors = new MapBindingResult(new HashMap<String, String>(), VariableServiceImpl.VARIABLE_NAME);

			this.programValidator.validate(program, errors);

			if (errors.hasErrors()) {
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			setCurrentProgram(programId);

			this.validateId(variableId, VariableServiceImpl.VARIABLE_NAME);
			TermRequest term = new TermRequest(variableId, VariableServiceImpl.VARIABLE_NAME, CvId.VARIABLES.getId());
			this.termValidator.validate(term, errors);

			if (errors.hasErrors()) {
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			this.variableValidator.validate(variable, errors);
			if (errors.hasErrors()) {
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			this.formatVariableSummary(variable);

			Integer id = StringUtil.parseInt(variable.getId(), null);

			Integer methodId = StringUtil.parseInt(variable.getMethod().getId(), null);
			Integer propertyId = StringUtil.parseInt(variable.getProperty().getId(), null);
			Integer scaleId = StringUtil.parseInt(variable.getScale().getId(), null);

			OntologyVariableInfo variableInfo = new OntologyVariableInfo();
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

			for (org.ibp.api.domain.ontology.VariableType variableType : variable.getVariableTypes()) {
				variableInfo.addVariableType(VariableType.getById(this.parseVariableTypeAsInteger(variableType)));
			}

			this.ontologyVariableDataManager.updateVariable(variableInfo);
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException(VariableServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public void deleteVariable(String id) {

		// Note: Validate Id for valid format and check if variable exists or not
		this.validateId(id, VariableServiceImpl.VARIABLE_NAME);
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), VariableServiceImpl.VARIABLE_NAME);

		// Note: Check if variable is deletable or not by checking its usage in variable
		this.termDeletableValidator.validate(new TermRequest(String.valueOf(id), VariableServiceImpl.VARIABLE_NAME, CvId.VARIABLES.getId()),
				errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		try {
			this.ontologyVariableDataManager.deleteVariable(StringUtil.parseInt(id, null));
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException(VariableServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public void deleteVariablesFromCache(final String cropName, final Integer[] variablesIds, String programId) {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), VariableServiceImpl.VARIABLE_NAME);

		ProgramDTO program = new ProgramDTO();
		program.setCrop(cropName);
		program.setUniqueID(programId);

		this.programValidator.validate(program, bindingResult);

		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		setCurrentProgram(programId);

		for (final Integer variableId : variablesIds) {
			this.validateId(String.valueOf(variableId), VariableServiceImpl.VARIABLE_NAME);
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), VariableServiceImpl.VARIABLE_NAME);

			final TermRequest term = new TermRequest(String.valueOf(variableId), VariableServiceImpl.VARIABLE_NAME, CvId.VARIABLES.getId());
			this.termValidator.validate(term, errors);

			if (errors.hasErrors()) {
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}

		this.ontologyVariableDataManager.deleteVariablesFromCache(Arrays.asList(variablesIds));
	}

	protected void formatVariableSummary(VariableDetails variableDetails) {

		Integer scaleId = StringUtil.parseInt(variableDetails.getScale().getId(), null);

		// Should discard unwanted parameters. We do not want expected min/max values if associated data type is not numeric
		if (scaleId != null) {
			try {
				Scale scale = this.ontologyScaleDataManager.getScaleById(scaleId, true);

				if (scale != null && !Objects.equals(scale.getDataType().getId(), DataType.NUMERIC_VARIABLE.getId())) {
					variableDetails.setExpectedMin(null);
					variableDetails.setExpectedMax(null);
				}

			} catch (MiddlewareException e) {
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
	private void setCurrentProgram(String programId) {
		ContextHolder.setCurrentProgram(programId);
	}

	private Integer parseVariableTypeAsInteger(org.ibp.api.domain.ontology.VariableType variableType) {
		if (variableType == null) {
			return null;
		}
		return StringUtil.parseInt(variableType.getId(), null);
	}

	private void mapVariableFilter(VariableFilter variableFilter,
			org.generationcp.middleware.manager.ontology.daoElements.VariableFilter middlewareVariableFilter) {

		middlewareVariableFilter.setProgramUuid(variableFilter.getProgramUuid());

		if (!Util.isNullOrEmpty(variableFilter.getPropertyIds())) {
			for (Integer i : variableFilter.getPropertyIds()) {
				middlewareVariableFilter.addPropertyId(i);
			}
		}

		if (!Util.isNullOrEmpty(variableFilter.getMethodIds())) {
			for (Integer i : variableFilter.getMethodIds()) {
				middlewareVariableFilter.addMethodId(i);
			}
		}

		if (!Util.isNullOrEmpty(variableFilter.getScaleIds())) {
			for (Integer i : variableFilter.getScaleIds()) {
				middlewareVariableFilter.addScaleId(i);
			}
		}

		if (!Util.isNullOrEmpty(variableFilter.getVariableIds())) {
			for (Integer i : variableFilter.getVariableIds()) {
				middlewareVariableFilter.addVariableId(i);
			}
		}

		if (!Util.isNullOrEmpty(variableFilter.getExcludedVariableIds())) {
			for (Integer i : variableFilter.getExcludedVariableIds()) {
				middlewareVariableFilter.addExcludedVariableId(i);
			}
		}

		if (!Util.isNullOrEmpty(variableFilter.getDataTypes())) {
			for (Integer i : variableFilter.getDataTypes()) {
				middlewareVariableFilter.addDataType(DataType.getById(i));
			}
		}

		if (!Util.isNullOrEmpty(variableFilter.getVariableTypes())) {
			for (Integer i : variableFilter.getVariableTypes()) {
				middlewareVariableFilter.addVariableType(VariableType.getById(i));
			}
		}

		if (!Util.isNullOrEmpty(variableFilter.getPropertyClasses())) {
			for (String s : variableFilter.getPropertyClasses()) {
				middlewareVariableFilter.addPropertyClass(s);
			}
		}

	}

	@Override
	public List<VariableDetails> getVariablesByFilter(final VariableFilter  variableFilter) {
		org.generationcp.middleware.manager.ontology.daoElements.VariableFilter middlewareVariableFilter =
				new org.generationcp.middleware.manager.ontology.daoElements.VariableFilter();
		this.mapVariableFilter(variableFilter, middlewareVariableFilter);
		List<Variable> variables = this.ontologyVariableDataManager.getWithFilter(middlewareVariableFilter);
		List<VariableDetails> variableDetailsList = new ArrayList<>();
		ModelMapper mapper = OntologyMapper.getInstance();
		for (Variable variable : variables) {
			VariableDetails variableSummary = mapper.map(variable, VariableDetails.class);
			variableDetailsList.add(variableSummary);
		}
		return variableDetailsList;
	}

	@Override
	public long countVariablesByDatasetId(final int datasetId, final List<Integer> variableTypes) {
		return this.ontologyVariableDataManager.countVariablesByDatasetId(datasetId, variableTypes);
	}

	@Override
	public List<VariableDTO> getVariablesByDatasetId(final int datasetId, final String cropname, final List<Integer> variableTypes,
		final int pageSize, final int pageNumber) {
		final List<VariableDTO> variableDTOs = this.ontologyVariableDataManager.getVariablesByDatasetId(datasetId, variableTypes, pageSize, pageNumber);
		for (final VariableDTO variableDTO : variableDTOs) {
			variableDTO.setCrop(cropname);
		}
		return variableDTOs;
	}

	@Override
	public long countAllVariables(final List<Integer> variableTypes) {
		return this.ontologyVariableDataManager.countAllVariables(variableTypes);
	}

	@Override
	public List<VariableDTO> getAllVariables(final String cropname, final List<Integer> variableTypes, final int pageSize,
		final int pageNumber) {
		final List<VariableDTO> variableDTOs = this.ontologyVariableDataManager.getAllVariables(variableTypes, cropname, pageSize, pageNumber);
		for (final VariableDTO variableDTO : variableDTOs) {
			variableDTO.setCrop(cropname);
		}
		return variableDTOs;
	}

}
