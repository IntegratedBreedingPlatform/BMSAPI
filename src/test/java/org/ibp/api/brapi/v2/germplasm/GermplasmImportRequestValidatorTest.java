package org.ibp.api.brapi.v2.germplasm;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.Synonym;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.pojos.Location;
import org.hamcrest.CoreMatchers;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

public class GermplasmImportRequestValidatorTest {

	public static final int MID = 1012;
	public static final String COUNTRY_OF_ORIGIN_CODE = "XYZ";
	@Mock
	private BreedingMethodService breedingMethodService;

	@Mock
	private LocationService locationService;

	@Mock
	private GermplasmNameService germplasmNameService;

	@InjectMocks
	private GermplasmImportRequestValidator germplasmImportRequestValidator;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.setupBreedingMethodAndLocationMocks();
	}


	@Test
	public void testPruneGermplasmInvalidForImport_OK(){
		final GermplasmImportRequest importRequest1 = new GermplasmImportRequest();
		importRequest1.setDefaultDisplayName(RandomStringUtils.randomAlphabetic(200));
		importRequest1.setAcquisitionDate("2021-02-21");
		importRequest1.setBreedingMethodDbId(String.valueOf(MID));
		importRequest1.setCountryOfOriginCode(COUNTRY_OF_ORIGIN_CODE);
		importRequest1.setAccessionNumber(RandomStringUtils.randomAlphabetic(200));
		importRequest1.setGermplasmOrigin(RandomStringUtils.randomAlphabetic(200));
		importRequest1.getSynonyms().add(new Synonym(RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(10)));
		importRequest1.getAdditionalInfo().put(RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(50));

		final List<GermplasmImportRequest> list = new ArrayList<>();
		list.add(importRequest1);
		final BindingResult bindingResult = this.germplasmImportRequestValidator.pruneGermplasmInvalidForImport(list);
		Assert.assertEquals(0, bindingResult.getErrorCount());
		Assert.assertEquals(1, list.size());
	}

	private void setupBreedingMethodAndLocationMocks() {
		final BreedingMethodDTO breedingMethodDTO = new BreedingMethodDTO();
		breedingMethodDTO.setMid(MID);
		Mockito.doReturn(Collections.singletonList(breedingMethodDTO)).when(this.breedingMethodService).getBreedingMethods(ArgumentMatchers.any(), ArgumentMatchers.any());
		final Location location = new Location();
		location.setLabbr(COUNTRY_OF_ORIGIN_CODE);
		Mockito.doReturn(Collections.singletonList(location)).when(this.locationService).getFilteredLocations(ArgumentMatchers.any(), ArgumentMatchers.any());
	}

	@Test
	public void testPruneGermplasmInvalidForImport_NullRequest(){
		try {
			this.germplasmImportRequestValidator.pruneGermplasmInvalidForImport(null);
			Assert.fail("Should have thrown validation exception for null request but did not.");
		} catch (final ApiRequestValidationException exception) {
			assertThat(Arrays.asList(exception.getErrors().get(0).getCodes()), CoreMatchers.hasItem("germplasm.import.list.null"));
		}
	}


	@Test
	public void testPruneGermplasmInvalidForImport_InvalidBasicParameters(){
		// No default name
		final GermplasmImportRequest importRequest1 = new GermplasmImportRequest();
		importRequest1.setAcquisitionDate("20210221");

		// No germplasm date
		final GermplasmImportRequest importRequest2 = new GermplasmImportRequest();
		importRequest2.setDefaultDisplayName(RandomStringUtils.randomAlphabetic(20));

		// Invalid date format
		final GermplasmImportRequest importRequest3 = new GermplasmImportRequest();
		importRequest3.setDefaultDisplayName(RandomStringUtils.randomAlphabetic(20));
		importRequest3.setAcquisitionDate("20210221");

		// Invalid breeding method
		final GermplasmImportRequest importRequest4 = new GermplasmImportRequest();
		importRequest4.setDefaultDisplayName(RandomStringUtils.randomAlphabetic(20));
		importRequest4.setAcquisitionDate("2021-02-21");
		importRequest4.setBreedingMethodDbId(String.valueOf(MID));

		// No germplasm location (countryOfOrigin)
		final GermplasmImportRequest importRequest5 = new GermplasmImportRequest();
		importRequest5.setDefaultDisplayName(RandomStringUtils.randomAlphabetic(20));
		importRequest5.setAcquisitionDate("2021-02-21");

		// Invalid germplasm location (countryOfOrigin)
		final GermplasmImportRequest importRequest6 = new GermplasmImportRequest();
		importRequest6.setDefaultDisplayName(RandomStringUtils.randomAlphabetic(20));
		importRequest6.setAcquisitionDate("2021-02-21");
		importRequest6.setCountryOfOriginCode(COUNTRY_OF_ORIGIN_CODE);

		Mockito.doReturn(Collections.emptyList()).when(this.breedingMethodService).getBreedingMethods(ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.doReturn(Collections.emptyList()).when(this.locationService).getFilteredLocations(ArgumentMatchers.any(), ArgumentMatchers.any());

		final List<GermplasmImportRequest> list = new ArrayList<>();
		list.add(importRequest1);
		list.add(importRequest2);
		list.add(importRequest3);
		list.add(importRequest4);
		list.add(importRequest5);
		list.add(importRequest6);
		final BindingResult bindingResult = this.germplasmImportRequestValidator.pruneGermplasmInvalidForImport(list);
		Assert.assertEquals(0, list.size());
		Assert.assertEquals("germplasm.create.null.name.types", bindingResult.getAllErrors().get(0).getCode());
		Assert.assertEquals(new Object[]{"1"}, bindingResult.getAllErrors().get(0).getArguments());
		Assert.assertEquals("germplasm.create.acquisition.date.null", bindingResult.getAllErrors().get(1).getCode());
		Assert.assertEquals(new Object[]{"2"}, bindingResult.getAllErrors().get(1).getArguments());
		Assert.assertEquals("germplasm.create.acquisition.date.invalid.format", bindingResult.getAllErrors().get(2).getCode());
		Assert.assertEquals(new Object[]{"3"}, bindingResult.getAllErrors().get(2).getArguments());
		Assert.assertEquals("germplasm.create.breeding.method.invalid", bindingResult.getAllErrors().get(3).getCode());
		Assert.assertEquals(new Object[]{"4"}, bindingResult.getAllErrors().get(3).getArguments());
		Assert.assertEquals("germplasm.create.country.origin.null", bindingResult.getAllErrors().get(4).getCode());
		Assert.assertEquals(new Object[]{"5"}, bindingResult.getAllErrors().get(4).getArguments());
		Assert.assertEquals("germplasm.create.country.origin.invalid", bindingResult.getAllErrors().get(5).getCode());
		Assert.assertEquals(new Object[]{"6"}, bindingResult.getAllErrors().get(5).getArguments());
	}

	@Test
	public void testPruneGermplasmInvalidForImport_DuplicatedPUI(){
		final GermplasmImportRequest importRequest1 = new GermplasmImportRequest();
		importRequest1.setDefaultDisplayName(RandomStringUtils.randomAlphabetic(40));
		importRequest1.setGermplasmPUI(RandomStringUtils.randomAlphanumeric(20));

		final GermplasmImportRequest importRequest2 = new GermplasmImportRequest();
		importRequest2.setDefaultDisplayName(RandomStringUtils.randomAlphabetic(40));
		importRequest2.setGermplasmPUI(RandomStringUtils.randomAlphanumeric(20));

		// Use the same PUI as 1st germplasm
		final GermplasmImportRequest importRequest3 = new GermplasmImportRequest();
		importRequest3.setDefaultDisplayName(RandomStringUtils.randomAlphabetic(40));
		importRequest3.setGermplasmPUI(importRequest1.getGermplasmPUI());

		final GermplasmImportRequest importRequest4 = new GermplasmImportRequest();
		importRequest4.setDefaultDisplayName(RandomStringUtils.randomAlphabetic(40));
		importRequest4.getSynonyms().add(new Synonym(importRequest2.getGermplasmPUI(), GermplasmImportRequest.PUI_NAME_TYPE));

		final List<GermplasmImportRequest> list = new ArrayList<>();
		list.add(importRequest1);
		list.add(importRequest2);
		list.add(importRequest3);
		list.add(importRequest4);
		final BindingResult bindingResult = this.germplasmImportRequestValidator.pruneGermplasmInvalidForImport(list);
		Assert.assertEquals(0, list.size());
		Assert.assertEquals(4, bindingResult.getErrorCount());
		for (final ObjectError error : bindingResult.getAllErrors()) {
			Assert.assertEquals("germplasm.create.duplicated.pui", error.getCode());
		}
	}

	@Test
	public void testPruneGermplasmInvalidForImport_ExistingPUI(){
		final String pui1 = RandomStringUtils.randomAlphanumeric(20);
		final GermplasmImportRequest importRequest1 = new GermplasmImportRequest();
		importRequest1.setDefaultDisplayName(RandomStringUtils.randomAlphabetic(40));
		importRequest1.setGermplasmPUI(pui1);

		final GermplasmImportRequest importRequest2 = new GermplasmImportRequest();
		importRequest2.setDefaultDisplayName(RandomStringUtils.randomAlphabetic(200));
		importRequest2.setAcquisitionDate("2021-02-21");
		importRequest2.setBreedingMethodDbId(String.valueOf(MID));
		importRequest2.setCountryOfOriginCode(COUNTRY_OF_ORIGIN_CODE);
		importRequest2.setAccessionNumber(RandomStringUtils.randomAlphabetic(200));
		importRequest2.setGermplasmOrigin(RandomStringUtils.randomAlphabetic(200));
		importRequest2.setGermplasmPUI(RandomStringUtils.randomAlphanumeric(20));

		final String pui2 = RandomStringUtils.randomAlphanumeric(20);
		final GermplasmImportRequest importRequest3 = new GermplasmImportRequest();
		importRequest3.setDefaultDisplayName(RandomStringUtils.randomAlphabetic(40));
		importRequest3.getSynonyms().add(new Synonym(pui2, GermplasmImportRequest.PUI_NAME_TYPE));

		Mockito.doReturn(Arrays.asList(pui1, pui2)).when(this.germplasmNameService).getExistingGermplasmPUIs(ArgumentMatchers.anyList());

		final List<GermplasmImportRequest> list = new ArrayList<>();
		list.add(importRequest1);
		list.add(importRequest2);
		list.add(importRequest3);
		final BindingResult bindingResult = this.germplasmImportRequestValidator.pruneGermplasmInvalidForImport(list);
		Assert.assertEquals(1, list.size());
		Assert.assertTrue(list.contains(importRequest2));
		Assert.assertEquals(2, bindingResult.getErrorCount());
		for (final ObjectError error : bindingResult.getAllErrors()) {
			Assert.assertEquals("germplasm.create.existing.pui", error.getCode());
		}
	}


}
