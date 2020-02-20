package org.ibp.api.java.impl.middleware.inventory;

import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.hamcrest.CoreMatchers;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ConflictException;
import org.ibp.api.java.impl.middleware.common.validator.InventoryUnitValidator;
import org.ibp.api.java.ontology.VariableService;
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
import java.util.List;

import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

public class InventoryScaleValidatorTest {

	public static final int LOCATION_ID = 6000;
	public static final int GERMPLASM_ID = 1;
	public static final String STOCK_ID = "ABCD";
	public static final String COMMENTS = "Comments";
	public static final int UNIT_ID = 1;

	@InjectMocks
	private InventoryUnitValidator inventoryUnitValidator;

	@Mock
	private VariableService variableService;

	private LotGeneratorInputDto lotGeneratorInputDto;

	private BindingResult errors;

	@Before
	public void setup() {
		this.lotGeneratorInputDto = new LotGeneratorInputDto();
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = ConflictException.class)
	public void testValidateNotNullInventoryScaleId() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setGenerateStock(false);
		final Integer unitId = null;
		this.lotGeneratorInputDto.setUnitId(unitId);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		Mockito.when(this.variableService.getVariablesByFilter(variableFilter)).thenReturn(Lists.newArrayList());
		this.inventoryUnitValidator.validateNotNullInventoryScaleId(this.errors, unitId);

		Assert.assertEquals(this.errors.getAllErrors().size(), 1);
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("inventory.unit.required"));
	}

	@Test
	public void testValidateInventoryScaleId() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setGenerateStock(false);
		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		Mockito.when(this.variableService.getVariablesByFilter(variableFilter)).thenReturn(Lists.newArrayList());
		this.inventoryUnitValidator.validateInventoryUnitId(this.errors, UNIT_ID);

		Assert.assertEquals(this.errors.getAllErrors().size(), 1);
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("inventory.unit.invalid"));
	}

	@Test
	public void testValidateValidInventoryScaleId() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		this.lotGeneratorInputDto.setGid(GERMPLASM_ID);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setGenerateStock(false);
		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		final VariableDetails variableDetail = new VariableDetails();
		variableDetail.setId(String.valueOf(UNIT_ID));

		final List<VariableDetails> variables = Lists.newArrayList(variableDetail);

		Mockito.when(this.variableService.getVariablesByFilter(variableFilter)).thenReturn(variables);
		this.inventoryUnitValidator.validateInventoryUnitId(this.errors, UNIT_ID);

		Assert.assertEquals(this.errors.getAllErrors().size(),0);
	}
}
