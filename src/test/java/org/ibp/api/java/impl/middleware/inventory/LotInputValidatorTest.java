package org.ibp.api.java.impl.middleware.inventory;

import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.InventoryScaleValidator;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotInputValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LotInputValidatorTest {

	public static final int GID = 1;
	public static final int LOCATION_ID = 6000;
	public static final int SCALE_ID = TermId.SEED_AMOUNT_G.getId();
	public static final String STOCK_ID = "ABCD";
	public static final String COMMENTS = "Comments";
	public static final String STOCK_PREFIX = "123";
	@InjectMocks
	private LotInputValidator lotInputValidator;

	@Mock
	private LocationValidator locationValidator;

	@Mock
	private InventoryScaleValidator inventoryScaleValidator;

	@Mock
	private LotService lotService;

	private LotGeneratorInputDto lotGeneratorInputDto;

	@Mock
	private GermplasmValidator germplasmValidator;

	@Before
	public void setup() {
		this.lotGeneratorInputDto = new LotGeneratorInputDto();
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataComments() {
		this.lotGeneratorInputDto.setGid(GID);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setGenerateStock(false);
		this.lotGeneratorInputDto.setScaleId(SCALE_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setComments(
			"CommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsCommentsComments");

		this.lotInputValidator.validate(this.lotGeneratorInputDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataGenerateStockNull() {
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setScaleId(SCALE_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setComments(COMMENTS);

		this.lotInputValidator.validate(this.lotGeneratorInputDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataGenerateStockTrue() {
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setScaleId(SCALE_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setComments(COMMENTS);
		this.lotGeneratorInputDto.setGenerateStock(true);

		this.lotInputValidator.validate(this.lotGeneratorInputDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataGenerateStockTrueWithPrefix() {
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setScaleId(SCALE_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setStockPrefix(STOCK_PREFIX);
		this.lotGeneratorInputDto.setComments(COMMENTS);
		this.lotGeneratorInputDto.setGenerateStock(true);

		this.lotInputValidator.validate(this.lotGeneratorInputDto);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateDataGenerateStockFalse() {
		this.lotGeneratorInputDto.setGid(1);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setScaleId(SCALE_ID);
		this.lotGeneratorInputDto.setComments(COMMENTS);
		this.lotGeneratorInputDto.setGenerateStock(false);
		this.lotGeneratorInputDto.setStockPrefix(STOCK_PREFIX);

		this.lotInputValidator.validate(this.lotGeneratorInputDto);
	}
}
