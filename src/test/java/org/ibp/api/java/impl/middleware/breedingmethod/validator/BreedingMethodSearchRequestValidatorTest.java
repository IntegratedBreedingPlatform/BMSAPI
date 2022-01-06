package org.ibp.api.java.impl.middleware.breedingmethod.validator;

import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.pojos.MethodType;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class BreedingMethodSearchRequestValidatorTest {

  private static final String CROP_NAME = UUID.randomUUID().toString();
  private static final String PROGRAM_UUID = UUID.randomUUID().toString();

  @InjectMocks
  private BreedingMethodSearchRequestValidator validator;

  @Mock
  private ProgramValidator programValidator;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testValidate() {
    final BreedingMethodSearchRequest request = new BreedingMethodSearchRequest();
    request.setFavoriteProgramUUID(PROGRAM_UUID);
    request.setFilterFavoriteProgramUUID(true);
    request.setMethodTypes(Arrays.asList(MethodType.DERIVATIVE.getCode(), MethodType.GENERATIVE.getCode()));

    Mockito.doNothing().when(this.programValidator).validate(Mockito.any(), Mockito.any());

    this.validator.validate(CROP_NAME, request);

    final ArgumentCaptor<ProgramDTO> programDTOArgumentCaptor = ArgumentCaptor.forClass(ProgramDTO.class);
    Mockito.verify(this.programValidator).validate(programDTOArgumentCaptor.capture(), Mockito.any());
    final ProgramDTO actualProgramDTO = programDTOArgumentCaptor.getValue();
    assertNotNull(actualProgramDTO);
    assertThat(actualProgramDTO.getCrop(), is(CROP_NAME));
    assertThat(actualProgramDTO.getUniqueID(), is(PROGRAM_UUID));
  }

//  @Test
//  public void shouldFailGetBreedingMethodsFilteringFavoritesWithoutProgramUUID() {
//    try {
//      final BreedingMethodSearchRequest searchRequest = new BreedingMethodSearchRequest(null, null, true);
//      this.breedingMethodService.getBreedingMethods(CROP_NAME, searchRequest, null);
//      fail("Should have failed.");
//    } catch (final Exception e) {
//      MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
//      MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
//          hasItem("breeding.methods.favorite.requires.program"));
//    }
//
//    Mockito.verifyZeroInteractions(this.programValidator);
//    Mockito.verifyZeroInteractions(this.middlewareBreedingMethodService);
//  }
//
//  @Test
//  public void shouldFailGetBreedingMethodsInvalidMethodType() {
//    try {
//      final BreedingMethodSearchRequest searchRequest = new BreedingMethodSearchRequest(null, null, false);
//      searchRequest.setMethodTypes(Collections.singletonList("ABC"));
//      this.breedingMethodService.getBreedingMethods(CROP_NAME, searchRequest, null);
//      fail("Should have failed.");
//    } catch (final Exception e) {
//      MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
//      MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
//          hasItem("invalid.breeding.method.type"));
//    }
//
//    Mockito.verifyZeroInteractions(this.programValidator);
//    Mockito.verifyZeroInteractions(this.middlewareBreedingMethodService);
//  }

}
