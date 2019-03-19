package org.ibp.api.java.impl.middleware.ontology;

import com.google.common.base.Optional;
import org.generationcp.commons.derivedvariable.DerivedVariableUtils;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.manager.ontology.VariableCache;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.ontology.validator.FormulaValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermDeletableValidator;
import org.ibp.api.java.ontology.FormulaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FormulaServiceImpl implements FormulaService {

	@Autowired
	private org.generationcp.middleware.service.api.derived_variables.FormulaService formulaService;

	@Autowired
	private FormulaValidator formulaValidator;

	@Autowired
	protected TermDeletableValidator termDeletableValidator;

	@Override
	public FormulaDto save(final FormulaDto formulaDto) {
		this.extractInputs(formulaDto);

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), FormulaDto.class.getName());
		// This validation will also fill the the inputs ids if exists
		this.formulaValidator.validate(formulaDto, bindingResult);

		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		this.setStorageFormat(formulaDto);
		VariableCache.removeFromCache(formulaDto.getTarget().getId());
		return this.formulaService.save(formulaDto);
	}

	@Override
	public void delete(final Integer formulaId) {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), FormulaDto.class.getName());
		final Optional<FormulaDto> formula = this.formulaService.getById(formulaId);

		if (!formula.isPresent()) {
			bindingResult.reject("variable.formula.not.exist", new Integer[] {formulaId}, "");
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		VariableCache.removeFromCache(formula.get().getTarget().getId());
		this.formulaService.delete(formulaId);
	}

	@Override
	public FormulaDto update(final FormulaDto formulaDto) {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), FormulaDto.class.getName());
		final Optional<FormulaDto> formula = this.formulaService.getById(formulaDto.getFormulaId());

		if (!formula.isPresent()) {
			bindingResult.reject("variable.formula.not.exist", new Integer[] {formulaDto.getFormulaId()}, "");
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		this.extractInputs(formulaDto);
		// This validation will also fill the the inputs ids if exists
		this.formulaValidator.validateUpdate(formulaDto, bindingResult);

		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		this.setStorageFormat(formulaDto);
		VariableCache.removeFromCache(formula.get().getTarget().getId());
		return this.formulaService.update(formulaDto);
	}

	private void extractInputs(final FormulaDto formulaDto) {
		final String definition = formulaDto.getDefinition();
		if (definition == null) {
			return;
		}
		final List<String> inputs = DerivedVariableUtils.extractInputs(definition);
		final List<FormulaVariable> formulaInputs = new ArrayList<>();
		for (final String input : inputs) {
			final FormulaVariable formulaVariable = new FormulaVariable();
			formulaVariable.setName(input);
			formulaInputs.add(formulaVariable);
		}
		formulaDto.setInputs(formulaInputs);
	}

	private void setStorageFormat(final FormulaDto formulaDto) {
		final Map<String, FormulaVariable> formulaVariableMap = new HashMap<>();
		for (final FormulaVariable input : formulaDto.getInputs()) {
			formulaVariableMap.put(input.getName(), input);
		}
		formulaDto.setDefinition(DerivedVariableUtils.getStorageFormat(formulaDto.getDefinition(), formulaVariableMap));
	}
}
