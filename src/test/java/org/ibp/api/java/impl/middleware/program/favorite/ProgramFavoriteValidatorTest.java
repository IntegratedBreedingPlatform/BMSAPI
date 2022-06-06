package org.ibp.api.java.impl.middleware.program.favorite;

import org.apache.commons.lang.math.RandomUtils;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.program.ProgramFavoriteRequestDto;
import org.generationcp.middleware.api.program.ProgramFavoriteService;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.ProgramLocationDefault;
import org.generationcp.middleware.pojos.dms.ProgramFavorite;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.program.validator.ProgramFavoriteValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ProgramFavoriteValidatorTest {

	@Mock
	private LocationDataManager locationDataManager;

	@Mock
	private BreedingMethodService breedingMethodService;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private ProgramFavoriteService programFavoriteService;

	@Mock
	private LocationService locationService;

	@InjectMocks
	private ProgramFavoriteValidator programFavoriteValidator;

	private ProgramFavoriteRequestDto programFavoriteRequestDtos;

	@Before
	public void setup() {
		this.programFavoriteRequestDtos = new ProgramFavoriteRequestDto();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidateAddFavorites_ThrowsException_WhenEntityListIsEmpty(){
		try {
		this.programFavoriteValidator.validateAddFavorites(null, this.programFavoriteRequestDtos);
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
	public void testValidateAddFavorites_ThrowsException_WhenVariableIdNotExists(){
		this.programFavoriteRequestDtos.setEntityIds(new HashSet<>(Arrays.asList(RandomUtils.nextInt())));
		this.programFavoriteRequestDtos.setFavoriteType(ProgramFavorite.FavoriteType.VARIABLES);
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
		this.programFavoriteRequestDtos.setFavoriteType(ProgramFavorite.FavoriteType.METHODS);
		Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.isNull())).thenReturn(Arrays.asList());

		try {
			this.programFavoriteValidator.validateAddFavorites(null, this.programFavoriteRequestDtos);
			Assert.fail("Should have thrown validation exception for method id unexisting but did not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("program.favorite.breeding.methods.not.identified", e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateDeleteFavorites_ThrowsException_WhenLocationUsedAsDefault(){
		final ProgramFavorite programFavorite = new ProgramFavorite();
		programFavorite.setProgramFavoriteId(1);
		Mockito.when(this.programFavoriteService.getProgramFavorites(ArgumentMatchers.any(),
			ArgumentMatchers.eq(ProgramFavorite.FavoriteType.LOCATION), ArgumentMatchers.eq(null)))
			.thenReturn(Arrays.asList(programFavorite));
		final ProgramLocationDefault programLocationDefault = new ProgramLocationDefault();
		programLocationDefault.setStorageLocationId(1);
		programLocationDefault.setBreedingLocationId(1);
		Mockito.when(this.locationService.getProgramLocationDefault(ArgumentMatchers.any()))
			.thenReturn(programLocationDefault);
		try {
			this.programFavoriteValidator.validateDeleteFavorites(null, new HashSet<>(Arrays.asList(1)));
			Assert.fail("Should have thrown validation exception for program favorite location used as default but did not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("program.favorite.location.used.as.default", e.getErrors().get(0).getCode());
		}
	}
}
