package org.ibp.api.java.impl.middleware.inventory;

import org.generationcp.middleware.domain.inventory.manager.LotDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.hamcrest.CoreMatchers;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import java.util.Arrays;
import java.util.HashMap;

import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

public class GermplasmValidatorTest {

	@Mock
	private GermplasmService germplasmService;

	@InjectMocks
	private GermplasmValidator germplasmValidator;

	private LotGeneratorInputDto lotGeneratorInputDto;

	private BindingResult errors;

	@Before
	public void setup() {
		this.lotGeneratorInputDto = new LotGeneratorInputDto();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidateValidGermplasmId() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotDto.class.getName());
		final Integer germplasmId = 1;
		this.lotGeneratorInputDto.setGid(germplasmId);
		this.lotGeneratorInputDto.setLocationId(6000);
		this.lotGeneratorInputDto.setGenerateStock(false);

		this.lotGeneratorInputDto.setScaleId(8264);
		this.lotGeneratorInputDto.setStockId("ABCD");
		this.lotGeneratorInputDto.setComments("Comments");
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		final GermplasmSummary germplasmSummary = new GermplasmSummary();
		Mockito.when(this.germplasmService.getGermplasm(String.valueOf(germplasmId))).thenReturn(germplasmSummary);
		this.germplasmValidator.validateGermplasmId(this.errors, germplasmId);

		Assert.assertTrue(this.errors.getAllErrors().size() == 0);
	}

	@Test
	public void testValidateNullGermplasmId() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotDto.class.getName());
		final Integer germplasmId = null;
		this.lotGeneratorInputDto.setGid(germplasmId);
		this.lotGeneratorInputDto.setLocationId(6000);
		this.lotGeneratorInputDto.setGenerateStock(false);

		this.lotGeneratorInputDto.setScaleId(8264);
		this.lotGeneratorInputDto.setStockId("ABCD");
		this.lotGeneratorInputDto.setComments("Comments");
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());

		this.germplasmValidator.validateGermplasmId(this.errors, germplasmId);

		Assert.assertTrue(this.errors.getAllErrors().size() == 1);
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("germplasm.required"));
	}

	@Test
	public void testValidateInvalidGermplasmId() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotDto.class.getName());
		final Integer germplasmId = 1;
		this.lotGeneratorInputDto.setGid(germplasmId);
		this.lotGeneratorInputDto.setLocationId(6000);
		this.lotGeneratorInputDto.setGenerateStock(false);

		this.lotGeneratorInputDto.setScaleId(8264);
		this.lotGeneratorInputDto.setStockId("ABCD");
		this.lotGeneratorInputDto.setComments("Comments");
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		Mockito.when(this.germplasmService.getGermplasm(String.valueOf(germplasmId))).thenReturn(null);
		this.germplasmValidator.validateGermplasmId(this.errors, germplasmId);

		Assert.assertTrue(this.errors.getAllErrors().size() == 1);
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("germplasm.invalid"));
	}
}
