package org.ibp.api.brapi.v2.germplasm;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmUpdateRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.Synonym;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.location.LocationService;
import org.hamcrest.CoreMatchers;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.AttributeValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Optional;

import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

public class GermplasmUpdateRequestValidatorTest {

	@Mock
	private BreedingMethodService breedingMethodService;

	@Mock
	private LocationService locationService;

	@InjectMocks
	private GermplasmUpdateRequestValidator germplasmUpdateRequestValidator;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}


	@Test
	public void testValidate_Ok(){
		final GermplasmUpdateRequest updateRequest = new GermplasmUpdateRequest();
		updateRequest.setAcquisitionDate("2021-02-21");
		updateRequest.setBreedingMethodDbId("1012");
		updateRequest.setCountryOfOriginCode("XYZ");
		updateRequest.setAccessionNumber(RandomStringUtils.randomAlphabetic(200));
		updateRequest.setGermplasmOrigin(RandomStringUtils.randomAlphabetic(200));
		updateRequest.getSynonyms().add(new Synonym(RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(10)));
		updateRequest.getAdditionalInfo().put(RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(50));

		Mockito.doReturn(Optional.of(new BreedingMethodDTO())).when(this.breedingMethodService).getBreedingMethod(ArgumentMatchers.anyInt());
		Mockito.doReturn(1L).when(this.locationService).countFilteredLocations(ArgumentMatchers.any(), ArgumentMatchers.isNull());

		this.germplasmUpdateRequestValidator.validate(updateRequest);
	}

	@Test
	public void testValidate_NullUpdateRequest(){
		try {
			this.germplasmUpdateRequestValidator.validate(null);
			Assert.fail("Should have thrown validation exception for null request but did not.");
		} catch (final ApiRequestValidationException exception) {
			assertThat(Arrays.asList(exception.getErrors().get(0).getCodes()), CoreMatchers.hasItem("germplasm.update.request.null"));
		}
	}


	@Test
	public void testValidate_InvalidParameters(){
		final GermplasmUpdateRequest updateRequest = new GermplasmUpdateRequest();
		updateRequest.setAcquisitionDate("20210221");
		updateRequest.setBreedingMethodDbId("1012");
		updateRequest.setCountryOfOriginCode("XYZ");
		updateRequest.setGermplasmOrigin(RandomStringUtils.randomAlphabetic(AttributeValidator.GERMPLASM_ATTRIBUTE_VALUE_MAX_LENGTH + 1));
		updateRequest.getSynonyms().add(new Synonym("", ""));
		final String nameType = RandomStringUtils.randomAlphabetic(10);
		updateRequest.getSynonyms().add(new Synonym(RandomStringUtils.randomAlphabetic(20), nameType));
		updateRequest.getSynonyms().add(new Synonym(RandomStringUtils.randomAlphabetic(260), nameType));
		updateRequest.getAdditionalInfo().put("", RandomStringUtils.randomAlphabetic(AttributeValidator.GERMPLASM_ATTRIBUTE_VALUE_MAX_LENGTH + 1));

		Mockito.doReturn(Optional.empty()).when(this.breedingMethodService).getBreedingMethod(ArgumentMatchers.anyInt());
		Mockito.doReturn(0L).when(this.locationService).countFilteredLocations(ArgumentMatchers.any(), ArgumentMatchers.isNull());

		try {
			this.germplasmUpdateRequestValidator.validate(updateRequest);
			Assert.fail("Should have thrown validation exception for null request but did not.");
		} catch (final ApiRequestValidationException exception) {
			assertThat(Arrays.asList(exception.getErrors().get(0).getCodes()), CoreMatchers.hasItem("germplasm.update.acquisition.date.invalid.format"));
			assertThat(Arrays.asList(exception.getErrors().get(1).getCodes()), CoreMatchers.hasItem("germplasm.update.breeding.method.invalid"));
			assertThat(Arrays.asList(exception.getErrors().get(2).getCodes()), CoreMatchers.hasItem("germplasm.update.country.origin.invalid"));
			assertThat(Arrays.asList(exception.getErrors().get(3).getCodes()), CoreMatchers.hasItem("germplasm.update.null.name.types"));
			assertThat(Arrays.asList(exception.getErrors().get(4).getCodes()), CoreMatchers.hasItem("germplasm.update.null.synonym"));
			assertThat(Arrays.asList(exception.getErrors().get(5).getCodes()), CoreMatchers.hasItem("germplasm.update.duplicated.name.types"));
			assertThat(Arrays.asList(exception.getErrors().get(6).getCodes()), CoreMatchers.hasItem("germplasm.update.name.exceeded.length"));
			assertThat(Arrays.asList(exception.getErrors().get(6).getArguments()), CoreMatchers.hasItem("synonyms"));
			assertThat(Arrays.asList(exception.getErrors().get(7).getCodes()), CoreMatchers.hasItem("germplasm.update.attribute.exceeded.length"));
			assertThat(Arrays.asList(exception.getErrors().get(7).getArguments()), CoreMatchers.hasItem("germplasmOrigin"));
		}
	}

	@Test
	public void testValidate_InvalidNameLengths(){
		final GermplasmUpdateRequest updateRequest = new GermplasmUpdateRequest();
		updateRequest.setAcquisitionDate("2021-02-21");
		updateRequest.setBreedingMethodDbId("1012");
		updateRequest.setCountryOfOriginCode("XYZ");
		// Invalid name lengths for custom name fields
		updateRequest.setDefaultDisplayName(RandomStringUtils.randomAlphabetic(260));
		updateRequest.setAccessionNumber(RandomStringUtils.randomAlphabetic(260));
		updateRequest.setGenus(RandomStringUtils.randomAlphabetic(260));
		updateRequest.setPedigree(RandomStringUtils.randomAlphabetic(260));
		updateRequest.setGermplasmPUI(RandomStringUtils.randomAlphabetic(260));

		Mockito.doReturn(Optional.of(new BreedingMethodDTO())).when(this.breedingMethodService).getBreedingMethod(ArgumentMatchers.anyInt());
		Mockito.doReturn(1L).when(this.locationService).countFilteredLocations(ArgumentMatchers.any(), ArgumentMatchers.isNull());

		try {
			this.germplasmUpdateRequestValidator.validate(updateRequest);
			Assert.fail("Should have thrown validation exception for null request but did not.");
		} catch (final ApiRequestValidationException exception) {
			assertThat(Arrays.asList(exception.getErrors().get(0).getCodes()), CoreMatchers.hasItem("germplasm.update.name.exceeded.length"));
			assertThat(Arrays.asList(exception.getErrors().get(0).getArguments()), CoreMatchers.hasItem("defaultDisplayName"));
			assertThat(Arrays.asList(exception.getErrors().get(1).getCodes()), CoreMatchers.hasItem("germplasm.update.name.exceeded.length"));
			assertThat(Arrays.asList(exception.getErrors().get(1).getArguments()), CoreMatchers.hasItem("accessionNumber"));
			assertThat(Arrays.asList(exception.getErrors().get(2).getCodes()), CoreMatchers.hasItem("germplasm.update.name.exceeded.length"));
			assertThat(Arrays.asList(exception.getErrors().get(2).getArguments()), CoreMatchers.hasItem("genus"));
			assertThat(Arrays.asList(exception.getErrors().get(3).getCodes()), CoreMatchers.hasItem("germplasm.update.name.exceeded.length"));
			assertThat(Arrays.asList(exception.getErrors().get(3).getArguments()), CoreMatchers.hasItem("pedigree"));
			assertThat(Arrays.asList(exception.getErrors().get(4).getCodes()), CoreMatchers.hasItem("germplasm.update.name.exceeded.length"));
			assertThat(Arrays.asList(exception.getErrors().get(4).getArguments()), CoreMatchers.hasItem("germplasmPUI"));
		}
	}



}
