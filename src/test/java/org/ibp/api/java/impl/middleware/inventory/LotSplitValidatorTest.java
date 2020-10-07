package org.ibp.api.java.impl.middleware.inventory;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotMergeRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotSplitRequestDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotInputValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotSplitValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.NotExtensible;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LotSplitValidatorTest {

	private static final String SPLIT_LOT_UUID = UUID.randomUUID().toString();

	@InjectMocks
	private LotSplitValidator lotSplitValidator;

	@Mock
	private LotInputValidator lotInputValidator;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldRequestBeValid() {
		final LotSplitRequestDto lotSplitRequestDto = new LotSplitRequestDto();
		lotSplitRequestDto.setSplitLotUUID(SPLIT_LOT_UUID);

		LotSplitRequestDto.InitialLotDepositDto initialLotDepositDto = new LotSplitRequestDto.InitialLotDepositDto();
		lotSplitRequestDto.setInitialDeposit(initialLotDepositDto);

		LotSplitRequestDto.NewLotSplitDto newLotSplitDto = new LotSplitRequestDto.NewLotSplitDto();
		lotSplitRequestDto.setNewLot(newLotSplitDto);

		Mockito.doNothing().when(this.lotInputValidator);

		this.lotSplitValidator.validateRequest(lotSplitRequestDto);
	}

	private ExtendedLotDto createDummyExtendedLotDto(Integer gid, LotStatus lotStatus, String unitName) {
		return this.createDummyExtendedLotDto(gid, lotStatus, unitName, UUID.randomUUID().toString(), 5D);
	}

	private ExtendedLotDto createDummyExtendedLotDto(Integer gid, LotStatus lotStatus, String unitName, String lotUUID, double actualBalance) {
		final ExtendedLotDto dto = new ExtendedLotDto();
		dto.setGid(gid);
		dto.setStatus(lotStatus.name());
		dto.setUnitName(unitName);
		dto.setLotUUID(lotUUID);
		dto.setActualBalance(actualBalance);
		return dto;
	}
}
