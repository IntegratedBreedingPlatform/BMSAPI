package org.ibp.api.rest.ontology;

import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.commons.derivedvariable.DerivedVariableUtils;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

@Api(value = "Ontology Formula Services")
@Controller
@RequestMapping("/ontology")
public class FormulaResource {

	@Autowired
	private FormulaService service;

	@Autowired
	private DerivedVariableProcessor processor;

	@Resource
	private ResourceBundleMessageSource resourceBundleMessageSource;

	@ApiOperation(value = "Create Formula", notes = "Create a formula to calculate a Variable")
	@RequestMapping(value = "/{cropname}/formula", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<FormulaDto> createFormula(@PathVariable final String cropname, @RequestBody final FormulaDto formulaDto) {
		this.validate(formulaDto);

		return new ResponseEntity<>(this.service.save(formulaDto), HttpStatus.CREATED);
	}

	private void validate(final FormulaDto formulaDto) {
		Preconditions.checkNotNull(formulaDto, this.getMessage("variable.formula.required"));
		Preconditions.checkArgument(formulaDto.getTargetTermId() != null, this.getMessage("variable.formula.targetid.required"));
		Preconditions
			.checkArgument(!StringUtils.isBlank(formulaDto.getDefinition()), this.getMessage("variable.formula.definition.required"));

		String formula = formulaDto.getDefinition();
		final Map<String, Object> terms = DerivedVariableUtils.extractTerms(formula);
		for (final Map.Entry<String, Object> termEntry : terms.entrySet()) {
			termEntry.setValue(BigDecimal.ONE);
		}

		formula = DerivedVariableUtils.replaceDelimiters(formula);
		// Inform the ontology manager admin full details of the exception by throwing it
		// Mapping engine errors to UI errors would be impractical
		processor.evaluateFormula(formula, terms);
	}

	private String getMessage(final String code) {
		return this.resourceBundleMessageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}
}
