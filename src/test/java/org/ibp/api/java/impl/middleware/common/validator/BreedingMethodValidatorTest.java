package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodNewRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.pojos.MethodClass;
import org.generationcp.middleware.pojos.MethodGroup;
import org.generationcp.middleware.pojos.MethodType;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.Random;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BreedingMethodValidatorTest {

	@Mock
	private BreedingMethodService breedingMethodService;

	@InjectMocks
	private BreedingMethodValidator breedingMethodValidator;

	private BreedingMethodDTO breedingMethod;
	private int breedingMethodDbId;

	@Before
	public void setup() {
		this.breedingMethodDbId = new Random().nextInt();
		this.breedingMethod = this.createBreedingMethod();
		when(this.breedingMethodService.getBreedingMethod(anyInt())).thenReturn(Optional.of(this.breedingMethod));
	}

	@Test
	public void validateCreation_Ok() {
		final BreedingMethodNewRequest breedingMethod = this.createBreedingMethodNewRequest();
		this.breedingMethodValidator.validateCreation(breedingMethod);
	}

	@Test
	public void validateCreation_NumberOfProgenitors_Generative_Ok() {
		final BreedingMethodNewRequest breedingMethod = this.createBreedingMethodNewRequest();
		breedingMethod.setType(MethodType.GENERATIVE.getCode());
		breedingMethod.setMethodClass(MethodClass.CROSSING.getId());
		breedingMethod.setNumberOfProgenitors(2);
		this.breedingMethodValidator.validateCreation(breedingMethod);
	}

	@Test
	public void validateCreation_NumberOfProgenitors_Generative_Fail() {
		final BreedingMethodNewRequest breedingMethod = this.createBreedingMethodNewRequest();
		breedingMethod.setType(MethodType.GENERATIVE.getCode());
		breedingMethod.setNumberOfProgenitors(-1);

		try {
			this.breedingMethodValidator.validateCreation(breedingMethod);
		} catch (final ApiRequestValidationException ex) {
			assertThat(ex.getErrors().get(0).getCode(), is("breeding.methods.invalid.numberOfProgenitors.generative"));
		}

	}

	@Test
	public void validateEdition_Ok() {
		final BreedingMethodDTO breedingMethodRequest = this.createBreedingMethod();
		this.breedingMethodValidator.validateEdition(this.breedingMethodDbId, breedingMethodRequest);
	}

	@Test
	public void validateEdition_Invalid_ProgenitorTypeDuo() {
		// Validate combinations from the request

		final BreedingMethodDTO breedingMethodRequest = this.createBreedingMethod();
		breedingMethodRequest.setNumberOfProgenitors(2);

		try {
			this.breedingMethodValidator.validateEdition(this.breedingMethodDbId, breedingMethodRequest);
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception.getErrors().get(0).getCode(), is("breeding.methods.invalid.numberOfProgenitors.derivative"));
		}

		breedingMethodRequest.setNumberOfProgenitors(-1);
		breedingMethodRequest.setType(MethodType.GENERATIVE.getCode());

		try {
			this.breedingMethodValidator.validateEdition(this.breedingMethodDbId, breedingMethodRequest);
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception.getErrors().get(0).getCode(), is("breeding.methods.invalid.numberOfProgenitors.generative"));
		}

		// Validate combinations from the request and the db

		breedingMethodRequest.setNumberOfProgenitors(-1);
		breedingMethodRequest.setType(MethodType.GENERATIVE.getCode());

		try {
			this.breedingMethodValidator.validateEdition(this.breedingMethodDbId, breedingMethodRequest);
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception.getErrors().get(0).getCode(), is("breeding.methods.invalid.numberOfProgenitors.generative"));
		}

		breedingMethodRequest.setNumberOfProgenitors(2);
		breedingMethodRequest.setType(MethodType.GENERATIVE.getCode());
		breedingMethodRequest.setMethodClass(MethodClass.CROSSING.getId());

		try {
			this.breedingMethodValidator.validateEdition(this.breedingMethodDbId, breedingMethodRequest);
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception.getErrors().get(0).getCode(), is("breeding.methods.invalid.numberOfProgenitors.derivative"));
		}
	}

	@Test
	public void validateEdition_Invalid_ClassTypeDuo() {
		// Validate combinations from the request

		final BreedingMethodDTO breedingMethodRequest = this.createBreedingMethod();
		breedingMethodRequest.setType(MethodType.GENERATIVE.getCode());
		breedingMethodRequest.setNumberOfProgenitors(2);
		breedingMethodRequest.setMethodClass(MethodClass.BULKING.getId());

		try {
			this.breedingMethodValidator.validateEdition(this.breedingMethodDbId, breedingMethodRequest);
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception.getErrors().get(0).getCode(), is("breeding.methods.invalid.typeclassduo"));
		}

		breedingMethodRequest.setType(MethodType.DERIVATIVE.getCode());
		breedingMethodRequest.setNumberOfProgenitors(-1);
		breedingMethodRequest.setMethodClass(MethodClass.CROSSING.getId());

		try {
			this.breedingMethodValidator.validateEdition(this.breedingMethodDbId, breedingMethodRequest);
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception.getErrors().get(0).getCode(), is("breeding.methods.invalid.typeclassduo"));
		}

		// Validate combinations from the request and the db

		breedingMethodRequest.setType(MethodType.GENERATIVE.getCode());
		breedingMethodRequest.setNumberOfProgenitors(2);
		breedingMethodRequest.setMethodClass(MethodClass.BULKING.getId());

		try {
			this.breedingMethodValidator.validateEdition(this.breedingMethodDbId, breedingMethodRequest);
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception.getErrors().get(0).getCode(), is("breeding.methods.invalid.typeclassduo"));
		}

		breedingMethodRequest.setType(MethodType.DERIVATIVE.getCode());
		breedingMethodRequest.setNumberOfProgenitors(-1);
		breedingMethodRequest.setMethodClass(MethodClass.CROSSING.getId());

		try {
			this.breedingMethodValidator.validateEdition(this.breedingMethodDbId, breedingMethodRequest);
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception.getErrors().get(0).getCode(), is("breeding.methods.invalid.typeclassduo"));
		}
	}

	@Test
	public void validateEdition_Invalid_Type() {
		final BreedingMethodDTO breedingMethodRequest = this.createBreedingMethod();
		breedingMethodRequest.setType(randomAlphanumeric(10));

		try {
			this.breedingMethodValidator.validateEdition(this.breedingMethodDbId, breedingMethodRequest);
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception.getErrors().get(0).getCode(), is("breeding.methods.invalid.type"));
		}
	}

	@Test
	public void validateEdition_Invalid_Group() {
		final BreedingMethodDTO breedingMethodRequest = this.createBreedingMethod();
		breedingMethodRequest.setGroup(randomAlphanumeric(10));

		try {
			this.breedingMethodValidator.validateEdition(this.breedingMethodDbId, breedingMethodRequest);
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception.getErrors().get(0).getCode(), is("breeding.methods.invalid.group"));
		}
	}

	private BreedingMethodNewRequest createBreedingMethodNewRequest() {
		final BreedingMethodNewRequest breedingMethod = new BreedingMethodNewRequest();
		breedingMethod.setCode(randomAlphanumeric(8));
		breedingMethod.setName(randomAlphanumeric(50));
		breedingMethod.setDescription(randomAlphanumeric(255));
		breedingMethod.setType(MethodType.DERIVATIVE.getCode());
		breedingMethod.setGroup(MethodGroup.ALL_SYSTEM.getCode());
		breedingMethod.setMethodClass(MethodClass.BULKING.getId());
		breedingMethod.setNumberOfProgenitors(-1);
		return breedingMethod;
	}

	private BreedingMethodDTO createBreedingMethod() {
		final BreedingMethodDTO breedingMethod = new BreedingMethodDTO();
		breedingMethod.setMid(new Random().nextInt());
		breedingMethod.setCode(randomAlphanumeric(8));
		breedingMethod.setName(randomAlphanumeric(50));
		breedingMethod.setDescription(randomAlphanumeric(255));
		breedingMethod.setType(MethodType.DERIVATIVE.getCode());
		breedingMethod.setGroup(MethodGroup.ALL_SYSTEM.getCode());
		breedingMethod.setMethodClass(MethodClass.BULKING.getId());
		breedingMethod.setNumberOfProgenitors(-1);
		return breedingMethod;
	}
}
