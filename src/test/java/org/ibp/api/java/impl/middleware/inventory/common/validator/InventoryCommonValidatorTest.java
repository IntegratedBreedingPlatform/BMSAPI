package org.ibp.api.java.impl.middleware.inventory.common.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.ontology.VariableService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class InventoryCommonValidatorTest {

	private List<VariableDetails> variableDetails;

	private VariableFilter variableFilter;

	@Mock
	private VariableService variableService;

	@InjectMocks
	private InventoryCommonValidator inventoryCommonValidator;

	private BindingResult errors;

	@Before
	public void setUp() {
		variableDetails = this.buildVariableDetails();
		variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		Mockito.when(variableService.getVariablesByFilter(variableFilter)).thenReturn(variableDetails);
	}

	@Test
	public void test_validateLotNotes_throwsException() {
		try {
			this.errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
			this.inventoryCommonValidator.validateLotNotes(RandomStringUtils.randomAlphanumeric(256), errors);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.notes.length"));
		}
	}

	@Test
	public void test_validateTransactionNotes_throwsException() {
		try {
			this.errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
			this.inventoryCommonValidator.validateTransactionNotes(RandomStringUtils.randomAlphanumeric(256), errors);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("transaction.notes.length"));
		}
	}

	@Test
	public void test_validateStockIdPrefix_throwsInvalidLengthException() {
		try {
			this.errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
			this.inventoryCommonValidator.validateStockIdPrefix(RandomStringUtils.randomAlphanumeric(256), errors);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.stock.prefix.invalid.length"));
		}
	}

	@Test
	public void test_validateStockIdPrefix_throwsInvalidPatternException() {
		try {
			this.errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
			this.inventoryCommonValidator
				.validateStockIdPrefix(RandomStringUtils.randomAlphanumeric(14) + RandomStringUtils.randomNumeric(1), errors);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.stock.prefix.invalid.pattern"));
		}
	}

	@Test
	public void test_validateUnitNames_throwsException() {
		try {
			this.errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
			this.inventoryCommonValidator.validateUnitNames(Collections.singletonList("pounds"), errors);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.invalid.units"));
		}
	}

	@Test
	public void test_validateLotNotesLists_throwsException() {
		try {
			this.errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
			this.inventoryCommonValidator.validateLotNotes(Collections.singletonList(""), errors);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.list.notes.null.or.empty"));
		}
	}

	private List<VariableDetails> buildVariableDetails() {
		final VariableDetails unitKg = new VariableDetails();
		unitKg.setName("kg");
		final VariableDetails unitG = new VariableDetails();
		unitG.setName("g");
		final List<VariableDetails> variableDetails = Arrays.asList(unitKg, unitG);
		return variableDetails;
	}

}
