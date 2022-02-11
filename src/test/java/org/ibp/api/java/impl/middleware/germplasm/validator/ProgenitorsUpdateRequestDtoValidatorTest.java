package org.ibp.api.java.impl.middleware.germplasm.validator;

import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.domain.germplasm.ProgenitorsUpdateRequestDto;
import org.generationcp.middleware.pojos.MethodType;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ProgenitorsUpdateRequestDtoValidatorTest {

	@Mock
	private GermplasmService germplasmService;

	@Mock
	private BreedingMethodService breedingMethodService;

	@InjectMocks
	private ProgenitorsUpdateRequestDtoValidator progenitorsUpdateRequestDtoValidator;

	private final Integer gid = 2;

	@Test
	public void testValidate_ThrowsException_WhenRequestIsNull() {
		try {
			this.progenitorsUpdateRequestDtoValidator.validate(gid, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("request.null"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenMethodIsInvalid() {
		try {
			final ProgenitorsUpdateRequestDto progenitorsUpdateRequestDto = new ProgenitorsUpdateRequestDto(2, null, null, null);
			Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
				.thenReturn(Collections.emptyList());
			this.progenitorsUpdateRequestDtoValidator.validate(gid, progenitorsUpdateRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.update.breeding.method.invalid"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenInvalidGpidsCombination() {
		try {
			final ProgenitorsUpdateRequestDto progenitorsUpdateRequestDto = new ProgenitorsUpdateRequestDto(1, null, 1, null);
			Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
				.thenReturn(Arrays.asList(this.getBreedingMethod(MethodType.GENERATIVE, 1)));
			this.progenitorsUpdateRequestDtoValidator.validate(gid, progenitorsUpdateRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.invalid.progenitors.combination"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenNonAllowedOtherProgenitors() {
		try {
			final ProgenitorsUpdateRequestDto progenitorsUpdateRequestDto = new ProgenitorsUpdateRequestDto(1, 1, 0, Arrays.asList(3));
			Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
				.thenReturn(Arrays.asList(this.getBreedingMethod(MethodType.GENERATIVE, 1)));
			this.progenitorsUpdateRequestDtoValidator.validate(gid, progenitorsUpdateRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.update.no.extra.progenitors.allowed"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenOtherProgenitorsForNonGenerative() {
		try {
			final ProgenitorsUpdateRequestDto progenitorsUpdateRequestDto = new ProgenitorsUpdateRequestDto(1, 1, 4, Arrays.asList(3));
			Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
				.thenReturn(Arrays.asList(this.getBreedingMethod(MethodType.DERIVATIVE, 1)));
			this.progenitorsUpdateRequestDtoValidator.validate(gid, progenitorsUpdateRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.update.no.extra.progenitors.allowed.non.gen.breeding.method"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenOtherProgenitorsGenerativeWithNonZeroMprgn() {
		try {
			final ProgenitorsUpdateRequestDto progenitorsUpdateRequestDto = new ProgenitorsUpdateRequestDto(1, 1, 4, Arrays.asList(3));
			Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
				.thenReturn(Arrays.asList(this.getBreedingMethod(MethodType.GENERATIVE, 2)));
			this.progenitorsUpdateRequestDtoValidator.validate(gid, progenitorsUpdateRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.update.no.extra.progenitors.allowed.final.gen.method"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenInvalidOtherProgenitors() {
		try {
			final ProgenitorsUpdateRequestDto progenitorsUpdateRequestDto = new ProgenitorsUpdateRequestDto(1, 1, 4, Arrays.asList(0));
			Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
				.thenReturn(Arrays.asList(this.getBreedingMethod(MethodType.GENERATIVE, 0)));
			this.progenitorsUpdateRequestDtoValidator.validate(gid, progenitorsUpdateRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.update.extra.progenitors.can.not.be.null"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenNullOtherProgenitors() {
		try {
			final ProgenitorsUpdateRequestDto progenitorsUpdateRequestDto = new ProgenitorsUpdateRequestDto(1, 1, 4, Arrays.asList(0));
			Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
				.thenReturn(Arrays.asList(this.getBreedingMethod(MethodType.GENERATIVE, 0)));
			this.progenitorsUpdateRequestDtoValidator.validate(gid, progenitorsUpdateRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.update.extra.progenitors.can.not.be.null"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenProgenitorsContainsTheGidToUpdate() {
		try {
			final ProgenitorsUpdateRequestDto progenitorsUpdateRequestDto = new ProgenitorsUpdateRequestDto(1, 2, 4, Arrays.asList(7));
			Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
				.thenReturn(Arrays.asList(this.getBreedingMethod(MethodType.GENERATIVE, 0)));
			this.progenitorsUpdateRequestDtoValidator.validate(gid, progenitorsUpdateRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.update.progenitors.can.not.be.equals.to.gid"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenInvalidProgenitors() {
		try {
			final ProgenitorsUpdateRequestDto progenitorsUpdateRequestDto = new ProgenitorsUpdateRequestDto(1, 1, 4, Arrays.asList(7));
			Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
				.thenReturn(Arrays.asList(this.getBreedingMethod(MethodType.GENERATIVE, 0)));
			Mockito.when(this.germplasmService.getGermplasmByGIDs(Mockito.anyList())).thenReturn(Collections.emptyList());
			this.progenitorsUpdateRequestDtoValidator.validate(gid, progenitorsUpdateRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.update.invalid.gid"));
		}
	}

	private BreedingMethodDTO getBreedingMethod(final MethodType methodType, final Integer progenitorNumbes) {
		final BreedingMethodDTO breedingMethodDTO = new BreedingMethodDTO();
		breedingMethodDTO.setMid(1);
		breedingMethodDTO.setType(methodType.getCode());
		breedingMethodDTO.setNumberOfProgenitors(progenitorNumbes);
		return breedingMethodDTO;
	}
}
