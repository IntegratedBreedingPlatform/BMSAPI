package org.ibp.api.java.impl.middleware.inventory;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.generationcp.middleware.service.api.inventory.TransactionService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.InventoryUnitValidator;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.ExtendedLotListValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotInputValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

@RunWith(MockitoJUnitRunner.class)
public class LotInputValidatorTest {

	public static final int GID = 1;
	public static final int LOCATION_ID = 6000;
	public static final int UNIT_ID = TermId.SEED_AMOUNT_G.getId();
	public static final String STOCK_ID = "ABCD";
	public static final String COMMENTS = "Comments";
	public static final String STOCK_PREFIX = "123";
	@InjectMocks
	private LotInputValidator lotInputValidator;

	@Mock
	private LocationValidator locationValidator;

	@Mock
	private InventoryUnitValidator inventoryUnitValidator;

	@Mock
	private ExtendedLotListValidator extendedLotListValidator;

	@Mock
	private LotService lotService;

	@Mock
	private TransactionService transactionService;

	private LotGeneratorInputDto lotGeneratorInputDto;

	@Mock
	private GermplasmValidator germplasmValidator;

	@Mock
	private InventoryCommonValidator inventoryCommonValidator;

	@Before
	public void setup() {
		this.lotGeneratorInputDto = new LotGeneratorInputDto();
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataComments() {
		Mockito.doCallRealMethod().when(inventoryCommonValidator).validateLotNotes(Mockito.anyString(), Mockito.any(BindingResult.class));
		this.lotGeneratorInputDto.setGid(GID);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setGenerateStock(false);
		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(RandomStringUtils.randomAlphabetic(256));
		this.lotInputValidator.validate(null, this.lotGeneratorInputDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataGenerateStockNull() {
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);

		this.lotInputValidator.validate(null, this.lotGeneratorInputDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataGenerateStockTrue() {
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		this.lotGeneratorInputDto.setGenerateStock(true);

		this.lotInputValidator.validate(null, this.lotGeneratorInputDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataGenerateStockTrueWithPrefix() {
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setStockPrefix(STOCK_PREFIX);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		this.lotGeneratorInputDto.setGenerateStock(true);

		this.lotInputValidator.validate(null, this.lotGeneratorInputDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataGenerateStockTrueWithInvalidPrefix() {
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setStockPrefix(RandomStringUtils.randomAlphabetic(20));
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		this.lotGeneratorInputDto.setGenerateStock(true);

		this.lotInputValidator.validate(null, this.lotGeneratorInputDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataGenerateStockFalse() {
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		this.lotGeneratorInputDto.setGenerateStock(false);
		this.lotGeneratorInputDto.setStockPrefix(STOCK_PREFIX);

		this.lotInputValidator.validate(null, this.lotGeneratorInputDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataGenerateStockFalseWithInvalidStock() {
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		this.lotGeneratorInputDto.setGenerateStock(false);
		this.lotGeneratorInputDto.setStockId(RandomStringUtils.randomAlphabetic(40));

		this.lotInputValidator.validate(null, this.lotGeneratorInputDto);
	}
}
