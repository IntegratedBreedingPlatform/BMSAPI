package org.ibp.api.java.impl.middleware.inventory;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.inventory.manager.LotItemDto;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotItemDtoListValidator;
import org.ibp.api.java.ontology.VariableService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LotItemDtoListValidatorTest {

	public static final String SEED_STORAGE_LOCATION = "DSS";
	public static final String SEED_AMOUNT_g = "SEED_AMOUNT_g";

	@InjectMocks
	private LotItemDtoListValidator lotItemDtoListValidator;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private VariableService variableService;

	@Mock
	private LocationDataManager locationDataManager;

	@Mock
	private LotService lotService;

	@Before
	public void setup() {

	}

	@Test
	public void testValidateListNull() {
		try {
			this.lotItemDtoListValidator.validate(null);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.list.null"));

		}
	}

	@Test
	public void testValidateEmptyList() {
		final List<LotItemDto> lotList = new ArrayList<>();
		try {
			this.lotItemDtoListValidator.validate(lotList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.list.no.items"));
		}
	}

	@Test
	public void testValidateListWithNullItem() {
		final List<LotItemDto> lotList = new ArrayList<>();
		lotList.add(null);
		try {
			this.lotItemDtoListValidator.validate(lotList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.list.item.null"));
		}
	}

	@Test
	public void testValidateGermplasmListEmpty() {
		final List<LotItemDto> lotList = new ArrayList<>();
		final LotItemDto lotItemDto = createLotItemDto();

		try {
			lotItemDto.setGid(null);
			lotList.add(lotItemDto);
			this.lotItemDtoListValidator.validate(lotList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.list.gid.null"));
		}
	}

	@Test
	public void testValidateGermplasmListInvalid() {
		final List<LotItemDto> lotList = new ArrayList<>();

		final LotItemDto lotItemDto = createLotItemDto();

		Mockito.when(this.germplasmDataManager.getGermplasms(Arrays.asList(lotItemDto.getGid()))).thenReturn(Arrays.asList());

		try {
			lotList.add(lotItemDto);
			this.lotItemDtoListValidator.validate(lotList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.invalid.gids"));

		}
	}

	@Test
	public void testValidateStorageLocationsNull() {
		final List<LotItemDto> lotList = new ArrayList<>();
		final LotItemDto lotItemDto = createLotItemDto();

		Mockito.when(this.germplasmDataManager.getGermplasms(Arrays.asList(lotItemDto.getGid()))).thenReturn(Arrays.asList(new Germplasm()));

		try {
			lotItemDto.setStorageLocationAbbr("");
			lotList.add(lotItemDto);
			this.lotItemDtoListValidator.validate(lotList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.list.location.abbreviation.null.or.empty"));
		}
	}

	@Test
	public void testValidateStorageLocationsInvalid() {
		final List<LotItemDto> lotList = new ArrayList<>();
		final LotItemDto lotItemDto = createLotItemDto();

		Mockito.when(this.germplasmDataManager.getGermplasms(Arrays.asList(lotItemDto.getGid()))).thenReturn(Arrays.asList(new Germplasm()));

		try {
			lotItemDto.setStorageLocationAbbr("");
			lotList.add(lotItemDto);
			this.lotItemDtoListValidator.validate(lotList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.list.location.abbreviation.null.or.empty"));
		}
	}

	@Test
	public void testValidateScaleNamesNull() {
		final List<LotItemDto> lotList = new ArrayList<>();
		final LotItemDto lotItemDto = createLotItemDto();

		Mockito.when(this.germplasmDataManager.getGermplasms(Arrays.asList(lotItemDto.getGid()))).thenReturn(Arrays.asList(new Germplasm()));

		final List<Location> existingLocations = new ArrayList<>();
		final Location seedStorageLocation = new Location();
		seedStorageLocation.setLabbr(SEED_STORAGE_LOCATION);
		existingLocations.add(seedStorageLocation);
		Mockito.when(this.locationDataManager.getFilteredLocations(Mockito.any(), Mockito.any(), Mockito.any()))
			.thenReturn(existingLocations);

		try {
			lotItemDto.setScaleName(null);
			lotList.add(lotItemDto);
			this.lotItemDtoListValidator.validate(lotList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.list.units.null.or.empty"));
		}
	}

	@Test
	public void testValidateScaleNamesInvalid() {
		final List<LotItemDto> lotList = new ArrayList<>();
		final LotItemDto lotItemDto = createLotItemDto();

		Mockito.when(this.germplasmDataManager.getGermplasms(Arrays.asList(lotItemDto.getGid()))).thenReturn(Arrays.asList(new Germplasm()));

		final List<Location> existingLocations = new ArrayList<>();
		final Location seedStorageLocation = new Location();
		seedStorageLocation.setLabbr(SEED_STORAGE_LOCATION);
		existingLocations.add(seedStorageLocation);
		Mockito.when(this.locationDataManager.getFilteredLocations(Mockito.any(), Mockito.any(), Mockito.any()))
			.thenReturn(existingLocations);

		final VariableDetails variableDetails = new VariableDetails();
		variableDetails.setName(SEED_AMOUNT_g);
		final List<VariableDetails> existingInventoryScales = new ArrayList<>();
		existingInventoryScales.add(variableDetails);
		Mockito.when(this.variableService.getVariablesByFilter(Mockito.any())).thenReturn(existingInventoryScales);

		try {
			lotItemDto.setScaleName("Amount");
			lotList.add(lotItemDto);
			this.lotItemDtoListValidator.validate(lotList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.invalid.units"));
		}
	}

	@Test
	public void testValidateStockIdsNull() {
		final List<LotItemDto> lotList = new ArrayList<>();
		final LotItemDto lotItemDto = createLotItemDto();

		Mockito.when(this.germplasmDataManager.getGermplasms(Arrays.asList(lotItemDto.getGid()))).thenReturn(Arrays.asList(new Germplasm()));

		final List<Location> existingLocations = new ArrayList<>();
		final Location seedStorageLocation = new Location();
		seedStorageLocation.setLabbr(SEED_STORAGE_LOCATION);
		existingLocations.add(seedStorageLocation);
		Mockito.when(this.locationDataManager.getFilteredLocations(Mockito.any(), Mockito.any(), Mockito.any()))
			.thenReturn(existingLocations);

		final VariableDetails variableDetails = new VariableDetails();
		variableDetails.setName(SEED_AMOUNT_g);
		final List<VariableDetails> existingInventoryScales = new ArrayList<>();
		existingInventoryScales.add(variableDetails);
		Mockito.when(this.variableService.getVariablesByFilter(Mockito.any())).thenReturn(existingInventoryScales);

		try {
			lotItemDto.setStockId(null);
			lotList.add(lotItemDto);
			this.lotItemDtoListValidator.validate(lotList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.list.stock.ids.null.or.empty"));
		}
	}

	@Test
	public void testValidateStockIdsMaxLength() {
		final List<LotItemDto> lotList = new ArrayList<>();
		final LotItemDto lotItemDto = createLotItemDto();

		Mockito.when(this.germplasmDataManager.getGermplasms(Arrays.asList(lotItemDto.getGid()))).thenReturn(Arrays.asList(new Germplasm()));

		final List<Location> existingLocations = new ArrayList<>();
		final Location seedStorageLocation = new Location();
		seedStorageLocation.setLabbr(SEED_STORAGE_LOCATION);
		existingLocations.add(seedStorageLocation);
		Mockito.when(this.locationDataManager.getFilteredLocations(Mockito.any(), Mockito.any(), Mockito.any()))
			.thenReturn(existingLocations);

		final VariableDetails variableDetails = new VariableDetails();
		variableDetails.setName(SEED_AMOUNT_g);
		final List<VariableDetails> existingInventoryScales = new ArrayList<>();
		existingInventoryScales.add(variableDetails);
		Mockito.when(this.variableService.getVariablesByFilter(Mockito.any())).thenReturn(existingInventoryScales);

		try {
			lotItemDto.setStockId(RandomStringUtils.randomAlphabetic(256));
			lotList.add(lotItemDto);
			this.lotItemDtoListValidator.validate(lotList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.stock.id.length.higher.than.maximum"));
		}
	}

	@Test
	public void testValidateStockIdsDuplicated() {	
		final List<LotItemDto> lotList = new ArrayList<>();
		final LotItemDto lotItemDto = createLotItemDto();
		final LotItemDto lotItemDto1 = createLotItemDto();

		Mockito.when(this.germplasmDataManager.getGermplasms(Arrays.asList(lotItemDto.getGid(), 2)))
			.thenReturn(Arrays.asList(new Germplasm(), new Germplasm()));

		final List<Location> existingLocations = new ArrayList<>();
		final Location seedStorageLocation = new Location();
		seedStorageLocation.setLabbr(SEED_STORAGE_LOCATION);
		existingLocations.add(seedStorageLocation);
		Mockito.when(this.locationDataManager.getFilteredLocations(Mockito.any(), Mockito.any(), Mockito.any()))
			.thenReturn(existingLocations);

		final VariableDetails variableDetails = new VariableDetails();
		variableDetails.setName(SEED_AMOUNT_g);
		final List<VariableDetails> existingInventoryScales = new ArrayList<>();
		existingInventoryScales.add(variableDetails);
		Mockito.when(this.variableService.getVariablesByFilter(Mockito.any())).thenReturn(existingInventoryScales);

		try {
			lotItemDto1.setGid(2);
			lotList.add(lotItemDto);
			lotList.add(lotItemDto1);
			this.lotItemDtoListValidator.validate(lotList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.list.stock.ids.duplicated"));
		}
	}

	@Test
	public void testValidateStockIdsInvalid() {
		final List<LotItemDto> lotList = new ArrayList<>();
		final LotItemDto lotItemDto = createLotItemDto();

		Mockito.when(this.germplasmDataManager.getGermplasms(Arrays.asList(lotItemDto.getGid()))).thenReturn(Arrays.asList(new Germplasm()));

		final List<Location> existingLocations = new ArrayList<>();
		final Location seedStorageLocation = new Location();
		seedStorageLocation.setLabbr(SEED_STORAGE_LOCATION);
		existingLocations.add(seedStorageLocation);
		Mockito.when(this.locationDataManager.getFilteredLocations(Mockito.any(), Mockito.any(), Mockito.any()))
			.thenReturn(existingLocations);

		final VariableDetails variableDetails = new VariableDetails();
		variableDetails.setName(SEED_AMOUNT_g);
		final List<VariableDetails> existingInventoryScales = new ArrayList<>();
		existingInventoryScales.add(variableDetails);
		Mockito.when(this.variableService.getVariablesByFilter(Mockito.any())).thenReturn(existingInventoryScales);

		try {
			lotList.add(lotItemDto);
			this.lotItemDtoListValidator.validate(lotList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.list.stock.ids.invalid"));
		}
	}

	@Test
	public void testValidateInitialBalancesNull() {
		final List<LotItemDto> lotList = new ArrayList<>();
		final LotItemDto lotItemDto = createLotItemDto();

		Mockito.when(this.germplasmDataManager.getGermplasms(Arrays.asList(lotItemDto.getGid()))).thenReturn(Arrays.asList(new Germplasm()));

		final VariableDetails variableDetails = new VariableDetails();
		variableDetails.setName(SEED_AMOUNT_g);
		final List<VariableDetails> existingInventoryScales = new ArrayList<>();
		existingInventoryScales.add(variableDetails);
		Mockito.when(this.variableService.getVariablesByFilter(Mockito.any())).thenReturn(existingInventoryScales);

		final List<Location> existingLocations = new ArrayList<>();
		final Location seedStorageLocation = new Location();
		seedStorageLocation.setLabbr(SEED_STORAGE_LOCATION);
		existingLocations.add(seedStorageLocation);
		Mockito.when(this.locationDataManager.getFilteredLocations(Mockito.any(), Mockito.any(), Mockito.any()))
			.thenReturn(existingLocations);

		try {
			lotItemDto.setInitialBalance(null);
			lotList.add(lotItemDto);
			this.lotItemDtoListValidator.validate(lotList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.list.initial.balances.null"));

		}
	}

	@Test
	public void testValidateInitialBalancesNegative() {
		final List<LotItemDto> lotList = new ArrayList<>();
		final LotItemDto lotItemDto = createLotItemDto();

		Mockito.when(this.germplasmDataManager.getGermplasms(Arrays.asList(lotItemDto.getGid()))).thenReturn(Arrays.asList(new Germplasm()));

		final VariableDetails variableDetails = new VariableDetails();
		variableDetails.setName(SEED_AMOUNT_g);
		final List<VariableDetails> existingInventoryScales = new ArrayList<>();
		existingInventoryScales.add(variableDetails);
		Mockito.when(this.variableService.getVariablesByFilter(Mockito.any())).thenReturn(existingInventoryScales);

		final List<Location> existingLocations = new ArrayList<>();
		final Location seedStorageLocation = new Location();
		seedStorageLocation.setLabbr(SEED_STORAGE_LOCATION);
		existingLocations.add(seedStorageLocation);
		Mockito.when(this.locationDataManager.getFilteredLocations(Mockito.any(), Mockito.any(), Mockito.any()))
			.thenReturn(existingLocations);

		try {
			lotItemDto.setInitialBalance((double) -11);
			lotList.add(lotItemDto);
			this.lotItemDtoListValidator.validate(lotList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.list.initial.balances.negative.values"));

		}
	}

	@Test
	public void testValidateComments() {
		final List<LotItemDto> lotList = new ArrayList<>();
		final LotItemDto lotItemDto = createLotItemDto();

		Mockito.when(this.germplasmDataManager.getGermplasms(Arrays.asList(lotItemDto.getGid()))).thenReturn(Arrays.asList(new Germplasm()));

		final List<Location> existingLocations = new ArrayList<>();
		final Location seedStorageLocation = new Location();
		seedStorageLocation.setLabbr(SEED_STORAGE_LOCATION);
		existingLocations.add(seedStorageLocation);
		Mockito.when(this.locationDataManager.getFilteredLocations(Mockito.any(), Mockito.any(), Mockito.any()))
			.thenReturn(existingLocations);

		final VariableDetails variableDetails = new VariableDetails();
		variableDetails.setName(SEED_AMOUNT_g);
		final List<VariableDetails> existingInventoryScales = new ArrayList<>();
		existingInventoryScales.add(variableDetails);
		Mockito.when(this.variableService.getVariablesByFilter(Mockito.any())).thenReturn(existingInventoryScales);

		try {
			lotItemDto.setNotes(RandomStringUtils.randomAlphabetic(256));
			lotList.add(lotItemDto);
			this.lotItemDtoListValidator.validate(lotList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.input.list.notes.length"));
		}
	}

	private static LotItemDto createLotItemDto() {
		final LotItemDto lotItemDto = new LotItemDto();
		lotItemDto.setGid(Integer.valueOf(RandomStringUtils.randomNumeric(9)));
		lotItemDto.setStorageLocationAbbr(SEED_STORAGE_LOCATION);
		lotItemDto.setInitialBalance((double) 30);
		lotItemDto.setScaleName(LotItemDtoListValidatorTest.SEED_AMOUNT_g);
		lotItemDto.setStockId(RandomStringUtils.randomAlphabetic(30));
		lotItemDto.setNotes(RandomStringUtils.randomAlphabetic(200));
		return lotItemDto;
	}
}
