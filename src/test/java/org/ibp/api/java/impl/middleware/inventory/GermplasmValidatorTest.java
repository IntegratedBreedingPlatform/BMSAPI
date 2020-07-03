package org.ibp.api.java.impl.middleware.inventory;

import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotImportRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotItemDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ApiRequestValidationException;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

public class GermplasmValidatorTest {

	public static final int UNIT_ID = TermId.SEED_AMOUNT_G.getId();
	public static final int LOCATION_ID = 6000;
	public static final String STOCK_ID = "ABCD";
	public static final String COMMENTS = "Comments";

	@Mock
	private GermplasmService germplasmService;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@InjectMocks
	private GermplasmValidator germplasmValidator;

	private LotGeneratorInputDto lotGeneratorInputDto;

	private BindingResult errors;
	public static final Integer GERMPLASM_ID = 1;

	@Before
	public void setup() {
		this.lotGeneratorInputDto = new LotGeneratorInputDto();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidateValidGermplasmId() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		final Integer germplasmId = 1;
		this.lotGeneratorInputDto.setGid(germplasmId);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setGenerateStock(false);

		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		final GermplasmSummary germplasmSummary = new GermplasmSummary();
		Mockito.when(this.germplasmService.getGermplasm(String.valueOf(germplasmId))).thenReturn(germplasmSummary);
		this.germplasmValidator.validateGermplasmId(this.errors, germplasmId);

		Assert.assertEquals(this.errors.getAllErrors().size(), 0);
	}

	@Test
	public void testValidateNullGermplasmId() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		final Integer germplasmId = null;
		this.lotGeneratorInputDto.setGid(germplasmId);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setGenerateStock(false);

		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());

		this.germplasmValidator.validateGermplasmId(this.errors, germplasmId);

		Assert.assertEquals(this.errors.getAllErrors().size(), 1);
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("germplasm.required"));
	}

	@Test
	public void testValidateInvalidGermplasmId() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		this.lotGeneratorInputDto.setGid(GERMPLASM_ID);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setGenerateStock(false);

		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		Mockito.when(this.germplasmService.getGermplasm(String.valueOf(GERMPLASM_ID))).thenReturn(null);
		this.germplasmValidator.validateGermplasmId(this.errors, GERMPLASM_ID);

		Assert.assertEquals(this.errors.getAllErrors().size(),  1);
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("germplasm.invalid"));
	}

	@Test
	public void testValidateGermplasmListInvalid() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		final List<Integer> gids = Collections.singletonList(GERMPLASM_ID);
		Mockito.when(this.germplasmDataManager.getGermplasms(gids)).thenReturn(Collections.EMPTY_LIST);

		try {
			this.germplasmValidator.validateGids(this.errors, gids);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), CoreMatchers.hasItem("gids.invalid"));
		}
	}
}
