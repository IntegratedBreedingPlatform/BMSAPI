package org.ibp.api.java.impl.middleware.inventory;

import com.google.common.collect.Lists;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotUpdateRequestDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Location;
import org.hamcrest.CoreMatchers;
import org.ibp.api.domain.ontology.VariableFilter;
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

import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

public class LocationValidatorTest {

	public static final int UNIT_ID = TermId.SEED_AMOUNT_G.getId();
	public static final int LOCATION_ID = 6000;
	public static final String STOCK_ID = "ABCD";
	public static final String COMMENTS = "Comments";
	public static final int GID = 1;
	final Set<Integer> STORAGE_LOCATION_TYPE = new HashSet<>(Arrays.asList(1500));

	@Mock
	private LocationDataManager locationDataManager;

	@Mock
	private LocationService locationService;

	@InjectMocks
	private LocationValidator locationValidator;

	private LotGeneratorInputDto lotGeneratorInputDto;

	private BindingResult errors;

	@Before
	public void setup() {
		this.lotGeneratorInputDto = new LotGeneratorInputDto();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidateNullLocationId() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		final Integer locationId = null;
		this.lotGeneratorInputDto.setGid(GID);
		this.lotGeneratorInputDto.setLocationId(locationId);
		this.lotGeneratorInputDto.setGenerateStock(false);

		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());

		this.locationValidator.validateSeedLocationId(this.errors, null, locationId);

		Assert.assertEquals(this.errors.getAllErrors().size(), 1);
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("location.required"));
	}

	@Test
	public void testValidateInvalidLocationId() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		this.lotGeneratorInputDto.setGid(GID);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setGenerateStock(false);

		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		Mockito.when(this.locationDataManager.getLocationByID(LOCATION_ID)).thenReturn(null);
		this.locationValidator.validateSeedLocationId(this.errors, null, LOCATION_ID);

		Assert.assertEquals(this.errors.getAllErrors().size(), 1);
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("location.invalid"));
	}

	@Test
	public void testValidateInvalidSeedLocation() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		this.lotGeneratorInputDto.setGid(GID);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setGenerateStock(false);

		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		final Location location = new Location();
		Mockito.when(this.locationDataManager.getLocationByID(LOCATION_ID)).thenReturn(location);
		Mockito.when(this.locationDataManager.getAllSeedingLocations(Lists.newArrayList(LOCATION_ID))).thenReturn(Lists.newArrayList());
		this.locationValidator.validateSeedLocationId(this.errors, null, LOCATION_ID);

		Assert.assertEquals(this.errors.getAllErrors().size(), 1);
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("seed.location.invalid"));
	}

	@Test
	public void testValidateValidSeedLocation() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		this.lotGeneratorInputDto.setGid(GID);
		this.lotGeneratorInputDto.setLocationId(LOCATION_ID);
		this.lotGeneratorInputDto.setGenerateStock(false);

		this.lotGeneratorInputDto.setUnitId(UNIT_ID);
		this.lotGeneratorInputDto.setStockId(STOCK_ID);
		this.lotGeneratorInputDto.setNotes(COMMENTS);
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		final Location location = new Location();
		final List<Location> locationList = Lists.newArrayList(location);
		Mockito.when(this.locationDataManager.getLocationByID(LOCATION_ID)).thenReturn(location);
		Mockito.when(this.locationDataManager.getAllSeedingLocations(Lists.newArrayList(LOCATION_ID))).thenReturn(locationList);
		this.locationValidator.validateSeedLocationId(this.errors, null, LOCATION_ID);

		Assert.assertEquals(this.errors.getAllErrors().size(), 0);
	}

	@Test
	public void testValidateValidSeedLocationAbbr() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotUpdateRequestDto.class.getName());
		final Location location = new Location();
		location.setLabbr("DSS");
		final List<Location> locationList = Lists.newArrayList(location);
		final List<String> locationAbbrList = Lists.newArrayList("DSS");

		Mockito.when(this.locationService
			.getFilteredLocations(new LocationSearchRequest(null, STORAGE_LOCATION_TYPE, null, locationAbbrList, null, false), null))
			.thenReturn(locationList);
		this.locationValidator.validateSeedLocationAbbr(this.errors, null, locationAbbrList);

		Assert.assertEquals(this.errors.getAllErrors().size(), 0);
	}

	@Test
	public void testValidateInvalidSeedLocationAbbr() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotUpdateRequestDto.class.getName());
		final List<Location> locationList = Collections.emptyList();
		final List<String> locationAbbrList = Lists.newArrayList("DSS");
		;

		Mockito.when(this.locationService
			.getFilteredLocations(new LocationSearchRequest(null, STORAGE_LOCATION_TYPE, null, locationAbbrList, null, false), null))
			.thenReturn(locationList);
		this.locationValidator.validateSeedLocationAbbr(this.errors, null, locationAbbrList);

		Assert.assertEquals(this.errors.getAllErrors().size(), 1);
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("lot.input.invalid.abbreviations"));

	}

	@Test
	public void testValidateEmptySeedLocationAbbr() {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotUpdateRequestDto.class.getName());
		final Location location = new Location();
		location.setLabbr("DSS");
		final List<String> locationAbbrList = Lists.newArrayList("");

		this.locationValidator.validateSeedLocationAbbr(this.errors, null, locationAbbrList);

		Assert.assertEquals(this.errors.getAllErrors().size(), 1);
		final ObjectError objectError = this.errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("lot.input.list.location.null.or.empty"));
	}
}
