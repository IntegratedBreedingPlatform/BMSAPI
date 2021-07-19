package org.ibp.api.java.impl.middleware.inventory;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.Germplasm;
import org.hamcrest.CoreMatchers;
import org.ibp.api.exception.ApiRequestValidationException;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

public class GermplasmValidatorTest {

	public static final int UNIT_ID = TermId.SEED_AMOUNT_G.getId();
	public static final int LOCATION_ID = 6000;
	public static final String STOCK_ID = "ABCD";
	public static final String COMMENTS = "Comments";

	@Mock
	private GermplasmService germplasmService;

	@InjectMocks
	private GermplasmValidator germplasmValidator;

	private BindingResult errors;
	public static final Integer GERMPLASM_ID = 1;
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}


	@Test
	public void testValidateValidGermplasmId() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		final Germplasm germplasm = new Germplasm(GERMPLASM_ID);
		Mockito.when(this.germplasmService.getGermplasmByGIDs(Arrays.asList(GERMPLASM_ID))).thenReturn(Arrays.asList(germplasm));
		this.germplasmValidator.validateGermplasmId(this.errors, GERMPLASM_ID);

		Assert.assertEquals(0, this.errors.getAllErrors().size());
	}

	@Test
	public void testValidateNullGermplasmId() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		final Integer germplasmId = null;
		this.germplasmValidator.validateGermplasmId(this.errors, germplasmId);

		Assert.assertEquals(1, this.errors.getAllErrors().size());
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("germplasm.required"));
	}

	@Test
	public void testValidateInvalidGermplasmId() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		Mockito.when(this.germplasmService.getGermplasmByGIDs(Arrays.asList(GERMPLASM_ID))).thenReturn(null);
		this.germplasmValidator.validateGermplasmId(this.errors, GERMPLASM_ID);

		Assert.assertEquals(1, this.errors.getAllErrors().size());
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("germplasm.invalid"));
	}

	@Test
	public void testValidateGermplasmListInvalid() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		final List<Integer> gids = Collections.singletonList(GERMPLASM_ID);
		Mockito.when(this.germplasmService.getGermplasmByGIDs(gids)).thenReturn(Collections.EMPTY_LIST);

		try {
			this.germplasmValidator.validateGids(this.errors, gids);
			Assert.fail("Should have thrown validation error but did not.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), CoreMatchers.hasItem("gids.invalid"));
		}
	}

	@Test
	public void testValidateGermplasmUUID() {
		final String germplasmDbId = RandomStringUtils.randomAlphabetic(20);
		this.errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		Mockito.when(this.germplasmService.getGermplasmByGUIDs(Collections.singletonList(germplasmDbId))).thenReturn(Collections.singletonList(new Germplasm()));
		this.germplasmValidator.validateGermplasmUUID(this.errors, germplasmDbId);
		Assert.assertEquals(0, this.errors.getAllErrors().size());
	}

	@Test
	public void testValidateGermplasmUUID_Invalid() {
		final String germplasmDbId = RandomStringUtils.randomAlphabetic(20);
		this.errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		Mockito.when(this.germplasmService.getGermplasmByGUIDs(Collections.singletonList(germplasmDbId))).thenReturn(Collections.emptyList());
		this.germplasmValidator.validateGermplasmUUID(this.errors, germplasmDbId);

		Assert.assertEquals(1, this.errors.getAllErrors().size());
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("germplasm.invalid"));
	}



}
