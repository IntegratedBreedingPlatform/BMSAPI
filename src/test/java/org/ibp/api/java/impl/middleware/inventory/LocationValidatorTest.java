package org.ibp.api.java.impl.middleware.inventory;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.germplasm.GermplasmAttributeService;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.location.LocationRequestDto;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.LocationTypeDTO;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotUpdateRequestDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.hamcrest.CoreMatchers;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

public class LocationValidatorTest {

	public static final int UNIT_ID = TermId.SEED_AMOUNT_G.getId();
	public static final int LOCATION_ID = 6000;
	public static final String STOCK_ID = "ABCD";
	public static final String COMMENTS = "Comments";
	public static final int GID = 1;
	public final Set<Integer> STORAGE_LOCATION_TYPE = new HashSet<>(Arrays.asList(1500));

	private static final String LOCATION_NAME = RandomStringUtils.randomAlphabetic(10);
	private static final String LOCATION_ABBR = RandomStringUtils.randomAlphabetic(3);
	private static final Integer LOCATION_TYPE = 410;

	@Mock
	private LocationDataManager locationDataManager;

	@Mock
	private LocationService locationService;

	@Mock
	private GermplasmNameService germplasmNameService;

	@Mock
	private GermplasmService germplasmService;

	@Mock
	private GermplasmAttributeService germplasmAttributeService;

	@Mock
	private StudyService studyService;

	@Mock
	private LotService lotService;

	@InjectMocks
	private LocationValidator locationValidator;

	private LotGeneratorInputDto lotGeneratorInputDto;

	private BindingResult errors;

	@Before
	public void setup() {
		this.lotGeneratorInputDto = new LotGeneratorInputDto();
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testValidateNullLocationId() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		this.lotGeneratorInputDto.setGid(GID);
		this.lotGeneratorInputDto.setLocationId(null);
		this.lotGeneratorInputDto.setGenerateStock(false);

		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());

		this.locationValidator.validateSeedLocationId(this.errors, null);

		Assert.assertEquals(1, this.errors.getAllErrors().size());
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("location.required"));
	}

	@Test
	public void testValidateInvalidLocationId() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		this.lotGeneratorInputDto.setGid(GID);
		this.lotGeneratorInputDto.setLocationId(LocationValidatorTest.LOCATION_ID);
		this.lotGeneratorInputDto.setGenerateStock(false);

		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		Mockito.when(this.locationDataManager.getLocationByID(LocationValidatorTest.LOCATION_ID)).thenReturn(null);
		this.locationValidator.validateSeedLocationId(this.errors, LocationValidatorTest.LOCATION_ID);

		Assert.assertEquals(1, this.errors.getAllErrors().size());
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("location.invalid"));
	}

	@Test
	public void testValidateInvalidSeedLocation() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		this.lotGeneratorInputDto.setGid(GID);
		this.lotGeneratorInputDto.setLocationId(LocationValidatorTest.LOCATION_ID);
		this.lotGeneratorInputDto.setGenerateStock(false);

		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		final Location location = new Location();
		Mockito.when(this.locationDataManager.getLocationByID(LocationValidatorTest.LOCATION_ID)).thenReturn(location);
		Mockito.when(this.locationDataManager.getAllSeedingLocations(Lists.newArrayList(LocationValidatorTest.LOCATION_ID)))
			.thenReturn(Lists.newArrayList());
		this.locationValidator.validateSeedLocationId(this.errors, LocationValidatorTest.LOCATION_ID);

		Assert.assertEquals(1, this.errors.getAllErrors().size());
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("seed.location.invalid"));
	}

	@Test
	public void testValidateValidSeedLocation() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		this.lotGeneratorInputDto.setGid(GID);
		this.lotGeneratorInputDto.setLocationId(LocationValidatorTest.LOCATION_ID);
		this.lotGeneratorInputDto.setGenerateStock(false);

		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		final Location location = new Location();
		final List<Location> locationList = Collections.singletonList(location);
		Mockito.when(this.locationDataManager.getLocationByID(LocationValidatorTest.LOCATION_ID)).thenReturn(location);
		Mockito.when(this.locationDataManager.getAllSeedingLocations(Lists.newArrayList(LocationValidatorTest.LOCATION_ID)))
			.thenReturn(locationList);
		this.locationValidator.validateSeedLocationId(this.errors, LocationValidatorTest.LOCATION_ID);

		Assert.assertEquals(0, this.errors.getAllErrors().size());
	}

	@Test
	public void testValidateValidSeedLocationAbbr() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotUpdateRequestDto.class.getName());
		final Location location = new Location();
		location.setLabbr("DSS");
		final List<Location> locationList = Collections.singletonList(location);
		final List<String> locationAbbrList = Lists.newArrayList("DSS");

		Mockito.when(this.locationService
				.getFilteredLocations(new LocationSearchRequest(null, STORAGE_LOCATION_TYPE, null, locationAbbrList, null), null))
			.thenReturn(locationList);
		this.locationValidator.validateSeedLocationAbbr(this.errors, locationAbbrList);

		Assert.assertEquals(0, this.errors.getAllErrors().size());
	}

	@Test
	public void testValidateInvalidSeedLocationAbbr() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotUpdateRequestDto.class.getName());
		final List<Location> locationList = Collections.emptyList();
		final List<String> locationAbbrList = Lists.newArrayList("DSS");

		Mockito.when(this.locationService
				.getFilteredLocations(new LocationSearchRequest(null, STORAGE_LOCATION_TYPE, null, locationAbbrList, null), null))
			.thenReturn(locationList);
		this.locationValidator.validateSeedLocationAbbr(this.errors, locationAbbrList);

		Assert.assertEquals(1, this.errors.getAllErrors().size());
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("lot.input.invalid.abbreviations"));

	}

	@Test
	public void testValidateEmptySeedLocationAbbr() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotUpdateRequestDto.class.getName());
		final Location location = new Location();
		location.setLabbr("DSS");
		final List<String> locationAbbrList = Lists.newArrayList("");

		this.locationValidator.validateSeedLocationAbbr(this.errors, locationAbbrList);

		Assert.assertEquals(1, this.errors.getAllErrors().size());
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("lot.input.list.location.null.or.empty"));
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testvalidateRequiredLocation() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotUpdateRequestDto.class.getName());

		try {
			this.locationValidator.validateLocation(errors, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("location.required"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testvalidateInvalidLocation() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotUpdateRequestDto.class.getName());

		try {
			this.locationValidator.validateLocation(errors, LocationValidatorTest.LOCATION_ID);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("location.invalid"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateCanBeDeleted_ThrowsException_WhenLocationIdIsInvalid() {

		try {
			this.locationValidator.validateCanBeDeleted(LocationValidatorTest.LOCATION_ID);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("location.invalid"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateCanBeDeleted_ThrowsException_WhenLocationIdIsRequired() {

		try {
			this.locationValidator.validateCanBeDeleted(null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("location.required"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateCanBeDeleted_ThrowsException_WhenLocationBelongsToGermplasm() {
		Mockito.when(this.locationDataManager.getLocationByID(LocationValidatorTest.LOCATION_ID)).thenReturn(new Location());
		Mockito.when(this.germplasmService.isLocationUsedInGermplasm(LocationValidatorTest.LOCATION_ID)).thenReturn(true);

		try {
			this.locationValidator.validateCanBeDeleted(LocationValidatorTest.LOCATION_ID);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("location.is.used.in.germplasms"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateCanBeDeleted_ThrowsException_WhenLocationBelongsToLot() {
		Mockito.when(this.locationDataManager.getLocationByID(LocationValidatorTest.LOCATION_ID)).thenReturn(new Location());
		Mockito.when(this.lotService.isLocationUsedInLot(LocationValidatorTest.LOCATION_ID)).thenReturn(true);

		try {
			this.locationValidator.validateCanBeDeleted(LocationValidatorTest.LOCATION_ID);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("location.is.used.in.lots"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateCanBeDeleted_ThrowsException_WhenLocationBelongsToAttribute() {
		Mockito.when(this.locationDataManager.getLocationByID(LocationValidatorTest.LOCATION_ID)).thenReturn(new Location());
		Mockito.when(this.germplasmAttributeService.isLocationUsedInAttribute(LocationValidatorTest.LOCATION_ID)).thenReturn(true);

		try {
			this.locationValidator.validateCanBeDeleted(LocationValidatorTest.LOCATION_ID);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("location.is.used.in.attributes"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateCanBeDeleted_ThrowsException_WhenLocationBelongsToName() {
		Mockito.when(this.locationDataManager.getLocationByID(LocationValidatorTest.LOCATION_ID)).thenReturn(new Location());
		Mockito.when(this.germplasmNameService.isLocationUsedInGermplasmName(LocationValidatorTest.LOCATION_ID)).thenReturn(true);

		try {
			this.locationValidator.validateCanBeDeleted(LocationValidatorTest.LOCATION_ID);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("location.is.used.in.names"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateCanBeDeleted_ThrowsException_WhenLocationBelongsToStudy() {
		Mockito.when(this.locationDataManager.getLocationByID(LocationValidatorTest.LOCATION_ID)).thenReturn(new Location());
		Mockito.when(this.studyService.isLocationUsedInStudy(LocationValidatorTest.LOCATION_ID)).thenReturn(true);

		try {
			this.locationValidator.validateCanBeDeleted(LocationValidatorTest.LOCATION_ID);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("location.is.used.in.studies"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidate_update_ThrowsException_WhenLocationIdIsInvalid() {
		try {
			this.locationValidator.validate(LocationValidatorTest.LOCATION_ID, new LocationRequestDto());
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("location.invalid"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidate_update_ThrowsException_WhenLocationTypeIsInvalid() {
		Mockito.when(this.locationDataManager.getLocationByID(LocationValidatorTest.LOCATION_ID)).thenReturn(new Location());
		final LocationRequestDto locationRequestDto =
			this.buildLocationRequestDto(LocationValidatorTest.LOCATION_NAME, LocationValidatorTest.LOCATION_ABBR, 0);

		try {
			this.locationValidator.validate(LocationValidatorTest.LOCATION_ID, locationRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("location.type.invalid"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidate_update_ThrowsException_WhenLocationAbbrIsInUsed() {
		Mockito.when(this.locationDataManager.getLocationByID(LocationValidatorTest.LOCATION_ID)).thenReturn(new Location());
		final List<org.generationcp.middleware.api.location.Location> listLocations =
			Arrays.asList(new org.generationcp.middleware.api.location.Location());
		Mockito.when(this.locationService.getLocations(Mockito.any(), Mockito.any())).thenReturn(listLocations);

		final LocationRequestDto locationRequestDto =
			this.buildLocationRequestDto(LocationValidatorTest.LOCATION_NAME, LocationValidatorTest.LOCATION_ABBR, null);

		try {
			this.locationValidator.validate(LocationValidatorTest.LOCATION_ID, locationRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("location.abbr.is.in.used"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidate_create_ThrowsException_WhenLocationTypeIsRequired() {
		final LocationRequestDto locationRequestDto =
			this.buildLocationRequestDto(LocationValidatorTest.LOCATION_NAME, LocationValidatorTest.LOCATION_ABBR, null);

		try {
			this.locationValidator.validate(null, locationRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("location.type.is.required"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidate_create_ThrowsException_WhenLocationTypeIsInvalid() {
		final LocationRequestDto locationRequestDto =
			this.buildLocationRequestDto(LocationValidatorTest.LOCATION_NAME, LocationValidatorTest.LOCATION_ABBR, 0);

		try {
			this.locationValidator.validate(null, locationRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("location.type.invalid"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidate_create_ThrowsException_WhenLocationAbbrIsInUsed() {
		final LocationRequestDto locationRequestDto =
			this.buildLocationRequestDto(LocationValidatorTest.LOCATION_NAME, LocationValidatorTest.LOCATION_ABBR,
				LocationValidatorTest.LOCATION_TYPE);
		final LocationTypeDTO locationTypeDTO = new LocationTypeDTO();
		locationTypeDTO.setId(LocationValidatorTest.LOCATION_TYPE);

		final List<LocationTypeDTO> locationTypeDTOS = Arrays.asList(new LocationTypeDTO());
		final List<org.generationcp.middleware.api.location.Location> listLocations =
			Arrays.asList(new org.generationcp.middleware.api.location.Location());

		Mockito.when(this.locationService.getLocationTypes()).thenReturn(Arrays.asList(locationTypeDTO));
		Mockito.when(this.locationService.getLocations(Mockito.any(), Mockito.any())).thenReturn(listLocations);

		try {
			this.locationValidator.validate(null, locationRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("location.abbr.is.in.used"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidate_ThrowsException_WhenCountryIsInvalid() {
		final LocationTypeDTO locationTypeDTO = new LocationTypeDTO();
		locationTypeDTO.setId(LocationValidatorTest.LOCATION_TYPE);
		final List<LocationTypeDTO> locationTypeDTOS = Arrays.asList(new LocationTypeDTO());

		Mockito.when(this.locationService.getLocationTypes()).thenReturn(Arrays.asList(locationTypeDTO));
		final LocationRequestDto locationRequestDto =
			this.buildLocationRequestDto(LocationValidatorTest.LOCATION_NAME, LocationValidatorTest.LOCATION_ABBR,
				LocationValidatorTest.LOCATION_TYPE);
		locationRequestDto.setCountryId(0);

		try {
			this.locationValidator.validate(null, locationRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("location.country.invalid"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidate_ThrowsException_WhenProvinceIsInvalid() {
		final LocationTypeDTO locationTypeDTO = new LocationTypeDTO();
		locationTypeDTO.setId(LocationValidatorTest.LOCATION_TYPE);
		final List<LocationTypeDTO> locationTypeDTOS = Arrays.asList(new LocationTypeDTO());

		Mockito.when(this.locationService.getLocationTypes()).thenReturn(Arrays.asList(locationTypeDTO));
		final LocationRequestDto locationRequestDto =
			this.buildLocationRequestDto(LocationValidatorTest.LOCATION_NAME, LocationValidatorTest.LOCATION_ABBR,
				LocationValidatorTest.LOCATION_TYPE);
		locationRequestDto.setProvinceId(0);

		try {
			this.locationValidator.validate(null, locationRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("location.province.invalid"));
			throw e;
		}
	}

	private LocationRequestDto buildLocationRequestDto(final String name, final String abbreviation, final Integer type) {
		final LocationRequestDto locationRequestDto = new LocationRequestDto();
		locationRequestDto.setName(name);
		locationRequestDto.setAbbreviation(abbreviation);
		locationRequestDto.setType(type);
		return locationRequestDto;
	}
}
