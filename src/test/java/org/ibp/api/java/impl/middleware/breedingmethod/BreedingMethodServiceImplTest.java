package org.ibp.api.java.impl.middleware.breedingmethod;

import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.hamcrest.MatcherAssert;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.Errors;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class BreedingMethodServiceImplTest {

	private static final String CROP_NAME = "Maize";
	private static final String PROGRAM_UUID = UUID.randomUUID().toString();

	@InjectMocks
	private BreedingMethodServiceImpl breedingMethodService;

	@Mock
	private ProgramValidator programValidator;

	@Mock
	private org.generationcp.middleware.api.breedingmethod.BreedingMethodService middlewareBreedingMethodService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldGetBreedingMethods() {
		Mockito.doNothing().when(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any(Errors.class));

		final boolean favorites = true;
		final List<BreedingMethodDTO> mockedBreedingMethod = Mockito.mock(List.class);
		final BreedingMethodSearchRequest searchRequest = new BreedingMethodSearchRequest(PROGRAM_UUID, null, favorites);
		Mockito.when(this.middlewareBreedingMethodService.getBreedingMethods(searchRequest))
			.thenReturn(mockedBreedingMethod);

		List<BreedingMethodDTO> actualBreedingMethods =
			this.breedingMethodService.getBreedingMethods(CROP_NAME, searchRequest);
		assertNotNull(actualBreedingMethods);
		assertThat(actualBreedingMethods, is(mockedBreedingMethod));

		Mockito.verify(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any(Errors.class));
		Mockito.verify(this.middlewareBreedingMethodService).getBreedingMethods(searchRequest);
	}

	@Test
	public void shouldGetBreedingMethodsWithoutUseProgramUUID() {
		final List<BreedingMethodDTO> mockedBreedingMethod = Mockito.mock(List.class);
		final BreedingMethodSearchRequest searchRequest = new BreedingMethodSearchRequest();
		Mockito.when(this.middlewareBreedingMethodService.getBreedingMethods(searchRequest)).thenReturn(mockedBreedingMethod);


		List<BreedingMethodDTO> actualBreedingMethods = this.breedingMethodService.getBreedingMethods(CROP_NAME, searchRequest);
		assertNotNull(actualBreedingMethods);
		assertThat(actualBreedingMethods, is(mockedBreedingMethod));

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verify(this.middlewareBreedingMethodService).getBreedingMethods(searchRequest);
	}

	@Test
	public void shouldFailGetBreedingMethodsFilteringFavoritesWithoutProgramUUID() {
		try {
			final BreedingMethodSearchRequest searchRequest = new BreedingMethodSearchRequest(null, null, true);
			this.breedingMethodService.getBreedingMethods(CROP_NAME, searchRequest);
			fail("Should have failed.");
		} catch (Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("breeding.methods.favorite.requires.program"));
		}

		Mockito.verifyZeroInteractions(this.programValidator);
		Mockito.verifyZeroInteractions(this.middlewareBreedingMethodService);
	}

}
