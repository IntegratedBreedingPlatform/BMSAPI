package org.ibp.api.java.impl.middleware.program.favorite;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.program.ProgramFavoriteRequestDto;
import org.generationcp.middleware.api.program.ProgramFavoriteService;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.dms.ProgramFavorite;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.impl.middleware.program.validator.ProgramFavoriteValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ProgramFavoriteValidatorTest {

	private final String PROGRAM_UUID = RandomStringUtils.randomAlphabetic(20);

	private final Integer LOCATION_ID = RandomUtils.nextInt();
	@Mock
	private LocationDataManager locationDataManager;

	@Mock
	private BreedingMethodService breedingMethodService;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private ProgramFavoriteService programFavoriteService;

	@Mock
	private LocationValidator locationValidator;

	@InjectMocks
	private ProgramFavoriteValidator programFavoriteValidator;

	ProgramFavoriteRequestDto programFavoriteRequestDtos;

	@Before
	public void setup() {
		programFavoriteRequestDtos = new ProgramFavoriteRequestDto();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidateAddFavorites_ThrowsException_WhenEntityListIsEmpty(){
		try {
		this.programFavoriteValidator.validateAddFavorites(null, programFavoriteRequestDtos);
			Assert.fail("Should have thrown validation exception for Entity list empty but did not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("program.favorite.entity.list.id.required", e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateAddFavorites_ThrowsException_WhenFavoriteAlreadyExists(){
		final ProgramFavorite favorite = new  ProgramFavorite();
		final List<ProgramFavorite> favorites = Arrays.asList(favorite);
		this.programFavoriteRequestDtos.setEntityIds(new HashSet<>(Arrays.asList(RandomUtils.nextInt())));
		this.programFavoriteRequestDtos.setFavoriteType(ProgramFavorite.FavoriteType.LOCATION);
		Mockito.when(this.programFavoriteService.getProgramFavorites(Mockito.any(), Mockito.any(),
			Mockito.any())).thenReturn(favorites);

		try {
			this.programFavoriteValidator.validateAddFavorites(null, this.programFavoriteRequestDtos);
			Assert.fail("Should have thrown validation exception for Favorite already exists but did not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("program.favorite.already.exists", e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateAddFavorites_ThrowsException_WhenLocationIdNotExists(){
		this.programFavoriteRequestDtos.setEntityIds(new HashSet<>(Arrays.asList(RandomUtils.nextInt())));
		this.programFavoriteRequestDtos.setFavoriteType(ProgramFavorite.FavoriteType.LOCATION);
		Mockito.when(this.locationDataManager.getLocationsByIDs(Mockito.any())).thenReturn(Arrays.asList());

		try {
			this.programFavoriteValidator.validateAddFavorites(null, this.programFavoriteRequestDtos);
			Assert.fail("Should have thrown validation exception for location id unexisting but did not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("program.favorite.location.not.identified", e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateAddFavorites_ThrowsException_WhenLocationIdDoNotBelongToProgram(){
		final Location location = new Location();
		location.setProgramUUID(RandomStringUtils.randomAlphabetic(20));
		location.setLocid(this.LOCATION_ID);
		final BindingResult errors = new MapBindingResult(new HashMap<>(), ProgramFavoriteRequestDto.class.getName());
		errors.reject("location.belongs.to.another.program", "");
		this.programFavoriteRequestDtos.setEntityIds(new HashSet<>(Arrays.asList(this.LOCATION_ID)));
		this.programFavoriteRequestDtos.setFavoriteType(ProgramFavorite.FavoriteType.LOCATION);

		Mockito.when(this.locationDataManager.getLocationsByIDs(Mockito.any())).thenReturn(Arrays.asList(location));


		Mockito.doThrow(new ApiRequestValidationException(errors.getAllErrors()))
			.when(this.locationValidator).validateLocationBelongToProgram(
				ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(this.PROGRAM_UUID), ArgumentMatchers.eq(location));

		try {
			this.programFavoriteValidator.validateAddFavorites(this.PROGRAM_UUID, this.programFavoriteRequestDtos);
			Assert.fail("Should have thrown validation exception for location id not belong to the program but did not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("location.belongs.to.another.program", e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateAddFavorites_ThrowsException_WhenVariableIdNotExists(){
		this.programFavoriteRequestDtos.setEntityIds(new HashSet<>(Arrays.asList(RandomUtils.nextInt())));
		this.programFavoriteRequestDtos.setFavoriteType(ProgramFavorite.FavoriteType.VARIABLE);
		Mockito.when(this.ontologyVariableDataManager.getWithFilter(Mockito.any())).thenReturn(Arrays.asList());

		try {
			this.programFavoriteValidator.validateAddFavorites(null, this.programFavoriteRequestDtos);
			Assert.fail("Should have thrown validation exception for variable id unexisting but did not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("program.favorite.variable.not.identified", e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateAddFavorites_ThrowsException_WhenMethodIdNotExists(){
		this.programFavoriteRequestDtos.setEntityIds(new HashSet<>(Arrays.asList(RandomUtils.nextInt())));
		this.programFavoriteRequestDtos.setFavoriteType(ProgramFavorite.FavoriteType.METHOD);
		Mockito.when(this.breedingMethodService.getBreedingMethods(Mockito.any(),Mockito.any())).thenReturn(Arrays.asList());

		try {
			this.programFavoriteValidator.validateAddFavorites(null, this.programFavoriteRequestDtos);
			Assert.fail("Should have thrown validation exception for method id unexisting but did not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("program.favorite.breeding.methods.not.identified", e.getErrors().get(0).getCode());
		}
	}
}
