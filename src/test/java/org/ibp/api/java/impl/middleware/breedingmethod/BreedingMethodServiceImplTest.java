package org.ibp.api.java.impl.middleware.breedingmethod;

import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

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
	public void shouldSearchBreedingMethods() {
		Mockito.doNothing().when(this.programValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any(Errors.class));

		final List<BreedingMethodDTO> mockedBreedingMethod = Mockito.mock(List.class);
		final BreedingMethodSearchRequest searchRequest = new BreedingMethodSearchRequest();
		searchRequest.setFavoriteProgramUUID(PROGRAM_UUID);
		searchRequest.setFilterFavoriteProgramUUID(true);
		Mockito.when(this.middlewareBreedingMethodService.searchBreedingMethods(searchRequest, null, null))
			.thenReturn(mockedBreedingMethod);

		final List<BreedingMethodDTO> actualBreedingMethods =
			this.breedingMethodService.searchBreedingMethods(searchRequest, null, null);
		assertNotNull(actualBreedingMethods);
		assertThat(actualBreedingMethods, is(mockedBreedingMethod));

		Mockito.verify(this.middlewareBreedingMethodService).searchBreedingMethods(searchRequest, null, null);
	}

}
