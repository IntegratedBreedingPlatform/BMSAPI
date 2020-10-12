package org.ibp.api.java.impl.middleware.inventory;

import factory.ExtendedLotDtoDummyFactory;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotMergeRequestDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotMergeValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LotMergeValidatorTest {

	private static final String LOT_KEEP_UUID = UUID.randomUUID().toString();

	@InjectMocks
	private LotMergeValidator lotMergeValidator;

	@Test
	public void shouldRequestBeValid() {
		final LotMergeRequestDto lotMergeRequestDto = new LotMergeRequestDto();
		lotMergeRequestDto.setLotUUIDToKeep(LOT_KEEP_UUID);

		this.lotMergeValidator.validateRequest(lotMergeRequestDto);
	}

	@Test
	public void shouldFailValidateRequestWithNullRequest() {
		try {
			this.lotMergeValidator.validateRequest(null);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.merge.input.null"));
		}
	}

	@Test
	public void shouldFailValidateRequestWithNullKeepLot() {
		try {
			this.lotMergeValidator.validateRequest(new LotMergeRequestDto());
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.merge.keep.lot.null"));
		}
	}

    @Test
	public void shouldAllLotsToBeMergedBeValid() {
		final List<ExtendedLotDto> extendedLotDtos = Arrays.asList(
				ExtendedLotDtoDummyFactory.create(1, LOT_KEEP_UUID,  LotStatus.ACTIVE, "unitName"),
				ExtendedLotDtoDummyFactory.create(1, ExtendedLotDtoDummyFactory.DEFAULT_UUID,  LotStatus.ACTIVE, "unitName")
		);

		this.lotMergeValidator.validate(LOT_KEEP_UUID, extendedLotDtos);
	}

	@Test
	public void shouldFailValidateLotsToBeMergedIfThereIsOneLotPresent(){
		final List<ExtendedLotDto> extendedLotDtos = Arrays.asList(
				ExtendedLotDtoDummyFactory.create(1, LotStatus.ACTIVE, "unitName")
		);
		try {
			this.lotMergeValidator.validate(LOT_KEEP_UUID, extendedLotDtos);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.merge.one.lot.present"));
		}
	}

	@Test
	public void shouldFailValidateLotsToBeMergedIfKeepLotIsNotSelected(){
		final List<ExtendedLotDto> extendedLotDtos = Arrays.asList(
				ExtendedLotDtoDummyFactory.create(1, UUID.randomUUID().toString(), LotStatus.ACTIVE, "unitName"),
				ExtendedLotDtoDummyFactory.create(1, UUID.randomUUID().toString(), LotStatus.ACTIVE, "unitName")
		);
		try {
			this.lotMergeValidator.validate(LOT_KEEP_UUID, extendedLotDtos);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.merge.keep.lot.not.selected"));
		}
	}
	
	@Test
	public void shouldFailValidateLotsToBeMergedIfThereIsTwoLotsWithSameUUID(){
		final List<ExtendedLotDto> extendedLotDtos = Arrays.asList(
				ExtendedLotDtoDummyFactory.create(1,"UUID"),
				ExtendedLotDtoDummyFactory.create(2,"UUID")
		);
		try {
			this.lotMergeValidator.validate(LOT_KEEP_UUID, extendedLotDtos);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.merge.one.lot.present"));
		}
	}

	@Test
	public void shouldFailValidateLotsToBeMergedWithNotActiveStatus() {
		final List<ExtendedLotDto> extendedLotDtos = Arrays.asList(
			ExtendedLotDtoDummyFactory.create(1, LOT_KEEP_UUID, LotStatus.ACTIVE, "unitName"),
			ExtendedLotDtoDummyFactory.create(1, LotStatus.CLOSED, "unitName")
		);

		try {
			this.lotMergeValidator.validate(LOT_KEEP_UUID, extendedLotDtos);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lots.closed"));
		}
	}

	@Test
	public void shouldFailValidateLotsToBeMergedWithDifferentGid() {
		final List<ExtendedLotDto> extendedLotDtos = Arrays.asList(
				ExtendedLotDtoDummyFactory.create(1, LOT_KEEP_UUID, LotStatus.ACTIVE, "unitName"),
				ExtendedLotDtoDummyFactory.create(2, LotStatus.ACTIVE, "unitName"));

		try {
			this.lotMergeValidator.validate(LOT_KEEP_UUID, extendedLotDtos);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.merge.gid.different"));
		}
	}

	@Test
	public void shouldFailValidateLotsToBeMergedWithDifferentUnitName() {
		final List<ExtendedLotDto> extendedLotDtos = Arrays.asList(
				ExtendedLotDtoDummyFactory.create(1, LOT_KEEP_UUID, LotStatus.ACTIVE, "unitName"),
				ExtendedLotDtoDummyFactory.create(1, LotStatus.ACTIVE, "otherUnitName"));

		try {
			this.lotMergeValidator.validate(LOT_KEEP_UUID, extendedLotDtos);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.merge.unit.different"));
		}
	}

	@Test
	public void shouldFailValidateLotsToBeMergedIfDiscardedLotHasZeroAsActualBalance() {
		final List<ExtendedLotDto> extendedLotDtos = Arrays.asList(
			ExtendedLotDtoDummyFactory.create(1, LOT_KEEP_UUID, 0D),
			ExtendedLotDtoDummyFactory.create(1, UUID.randomUUID().toString(), 0D)
		);

		try {
			this.lotMergeValidator.validate(LOT_KEEP_UUID, extendedLotDtos);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("lot.merge.discarded.lot.invalid.actual.balance"));
		}
	}
	
}
