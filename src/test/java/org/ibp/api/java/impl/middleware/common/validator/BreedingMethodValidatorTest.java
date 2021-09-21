package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.api.breedingmethod.BreedingMethodNewRequest;
import org.generationcp.middleware.pojos.MethodClass;
import org.generationcp.middleware.pojos.MethodGroup;
import org.generationcp.middleware.pojos.MethodType;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class BreedingMethodValidatorTest {

	@InjectMocks
	private BreedingMethodValidator breedingMethodValidator;

	@Test
	public void validate_Ok() {
		final BreedingMethodNewRequest breedingMethod = new BreedingMethodNewRequest();
		breedingMethod.setCode(randomAlphanumeric(8));
		breedingMethod.setName(randomAlphanumeric(50));
		breedingMethod.setDescription(randomAlphanumeric(255));
		breedingMethod.setType(MethodType.DERIVATIVE.getCode());
		breedingMethod.setGroup(MethodGroup.ALL_SYSTEM.getCode());
		breedingMethod.setMethodClass(MethodClass.SEED_ACQUISITION.getId());
		breedingMethod.setNumberOfProgenitors(-1);
		this.breedingMethodValidator.validate(breedingMethod);
	}

	@Test
	public void validate_NumberOfProgenitors_Generative_Ok() {
		final BreedingMethodNewRequest breedingMethod = new BreedingMethodNewRequest();
		breedingMethod.setCode(randomAlphanumeric(8));
		breedingMethod.setName(randomAlphanumeric(50));
		breedingMethod.setDescription(randomAlphanumeric(255));
		breedingMethod.setType(MethodType.GENERATIVE.getCode());
		breedingMethod.setGroup(MethodGroup.ALL_SYSTEM.getCode());
		breedingMethod.setMethodClass(MethodClass.SEED_ACQUISITION.getId());
		breedingMethod.setNumberOfProgenitors(2);
		this.breedingMethodValidator.validate(breedingMethod);
	}

	@Test
	public void validate_NumberOfProgenitors_Generative_Fail() {
		final BreedingMethodNewRequest breedingMethod = new BreedingMethodNewRequest();
		breedingMethod.setCode(randomAlphanumeric(8));
		breedingMethod.setName(randomAlphanumeric(50));
		breedingMethod.setDescription(randomAlphanumeric(255));
		breedingMethod.setType(MethodType.GENERATIVE.getCode());
		breedingMethod.setGroup(MethodGroup.ALL_SYSTEM.getCode());
		breedingMethod.setMethodClass(MethodClass.SEED_ACQUISITION.getId());
		breedingMethod.setNumberOfProgenitors(-1);

		try {
			this.breedingMethodValidator.validate(breedingMethod);
		} catch (final ApiRequestValidationException ex) {
			assertThat(ex.getErrors().get(0).getCode(), is("breeding.methods.invalid.numberOfProgenitors.generative"));
		}

	}
}
