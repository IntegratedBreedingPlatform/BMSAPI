package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.germplasm.GermplasmServiceImpl;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmUpdateDtoValidatorTest {

	@Mock
	private GermplasmService germplasmService;

	@Mock
	private org.generationcp.middleware.api.germplasm.GermplasmService germplasmMiddlewareService;

	@Mock
	private LocationService locationService;

	@Mock
	private BreedingMethodService breedingMethodService;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@InjectMocks
	private GermplasmUpdateDtoValidator germplasmUpdateDtoValidator;

	@Test
	public void testValidation_Success() {

		final String programUUID = RandomStringUtils.random(10);
		final Germplasm germplasm = new Germplasm(1);
		germplasm.setGermplasmUUID(UUID.randomUUID().toString());

		final LocationDTO location = new LocationDTO();
		location.setId(1);
		location.setAbbreviation("AFG");

		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.setGid(germplasm.getGid());
		germplasmUpdateDTO.setGermplasmUUID(germplasm.getGermplasmUUID());
		germplasmUpdateDTO.setLocationAbbreviation(location.getAbbreviation());
		germplasmUpdateDTO.setCreationDate("20200101");
		germplasmUpdateDTO.setBreedingMethodAbbr(null);
		germplasmUpdateDTO.getNames().put("DRVNM", randomAlphanumeric(10));
		germplasmUpdateDTO.getNames().put("LNAME", randomAlphanumeric(10));
		germplasmUpdateDTO.getAttributes().put("NOTE", randomAlphanumeric(10));
		germplasmUpdateDTO.getAttributes().put("ACQ_DATE", randomAlphanumeric(10));
		germplasmUpdateDTO.getProgenitors().put(GermplasmServiceImpl.PROGENITOR_1, 3);
		germplasmUpdateDTO.getProgenitors().put(GermplasmServiceImpl.PROGENITOR_2, 4);

		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);

		when(this.germplasmService.filterGermplasmNameTypes(Mockito.anySet()))
			.thenReturn(Arrays.asList(new GermplasmNameTypeDTO(null, "DRVNM", null), new GermplasmNameTypeDTO(null, "LNAME", null)));

		final Variable variable1 = new Variable();
		variable1.setName("NOTE");

		final Variable variable2 = new Variable();
		variable2.setName("ACQ_DATE");

		Mockito.when(this.ontologyVariableDataManager.getWithFilter(Mockito.any())).thenReturn(
			Arrays.asList(variable1, variable2));

		when(this.germplasmMiddlewareService.getGermplasmByGIDs(Mockito.anyList())).thenReturn(Arrays.asList(germplasm));
		when(this.germplasmMiddlewareService.getGermplasmByGUIDs(Mockito.anyList())).thenReturn(Arrays.asList(germplasm));
		when(this.germplasmMiddlewareService.getGermplasmByGIDs(Arrays.asList(3, 4)))
			.thenReturn(Arrays.asList(new Germplasm(3), new Germplasm(4)));

		when(this.locationService
			.searchLocations(new LocationSearchRequest(null, null,
				new ArrayList<>(Arrays.asList(germplasmUpdateDTO.getLocationAbbreviation())), null), null, null))
			.thenReturn(Arrays.asList(location));

		try {
			this.germplasmUpdateDtoValidator.validate(programUUID, germplasmUpdateList);
		} catch (final ApiRequestValidationException exception) {
			fail("Should not throw an exception");
		}

	}

	@Test
	public void testValidate_EmptyList() {
		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateDtoValidator.validateEmptyList(errors, new ArrayList<>());
		Mockito.verify(errors).reject("germplasm.update.empty.list", "");
	}

	@Test
	public void testValidate_InvalidAttributeAndNameCodes() {
		final String programUUID = RandomStringUtils.random(10);

		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.getNames().put("DRVNM", "");
		germplasmUpdateDTO.getNames().put("LNAME", "");
		germplasmUpdateDTO.getAttributes().put("NOTE", "");
		germplasmUpdateDTO.getAttributes().put("ACQ_DATE", "");

		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);

		when(this.germplasmService.filterGermplasmNameTypes(Mockito.anySet()))
			.thenReturn(Arrays.asList(new GermplasmNameTypeDTO(null, "DRVNM", null)));
		final Variable variable1 = new Variable();
		variable1.setName("NOTE");
		Mockito.when(this.ontologyVariableDataManager.getWithFilter(Mockito.any())).thenReturn(
			Arrays.asList(variable1));

		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateDtoValidator.validateAttributeAndNameCodes(errors, programUUID, germplasmUpdateList);
		Mockito.verify(errors).reject("germplasm.update.invalid.name.code", new String[] {"LNAME"}, "");
		Mockito.verify(errors).reject("germplasm.update.invalid.attribute.code", new String[] {"ACQ_DATE"}, "");
	}

	@Test
	public void testValidate_InvalidAttributeAndNameValues() {
		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.getNames().put("DRVNM", randomAlphanumeric(5001));
		germplasmUpdateDTO.getNames().put("LNAME", "");

		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);

		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateDtoValidator.validateAttributeAndNameValues(errors, germplasmUpdateList);
		Mockito.verify(errors).reject("germplasm.import.name.type.value.invalid.length", "");
	}

	@Test
	public void testValidate_NoGidsAndGUID() {
		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);
		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateDtoValidator.validateGermplasmIdAndGermplasmUUID(errors, germplasmUpdateList);
		Mockito.verify(errors).reject("germplasm.update.missing.gid.and.uuid", "");
	}

	@Test
	public void testValidate_InvalidGidAndGUID() {
		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.setGid(1);
		germplasmUpdateDTO.setGermplasmUUID(UUID.randomUUID().toString());

		final Germplasm germplasm = new Germplasm(2);

		when(this.germplasmMiddlewareService.getGermplasmByGIDs(Mockito.anyList())).thenReturn(Arrays.asList(germplasm));
		when(this.germplasmMiddlewareService.getGermplasmByGUIDs(Mockito.anyList())).thenReturn(Arrays.asList(germplasm));

		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);
		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateDtoValidator.validateGermplasmIdAndGermplasmUUID(errors, germplasmUpdateList);
		Mockito.verify(errors).reject("germplasm.update.invalid.gid", new String[] {germplasmUpdateDTO.getGid().toString()}, "");
		Mockito.verify(errors).reject("germplasm.update.invalid.uuid", new String[] {germplasmUpdateDTO.getGermplasmUUID()}, "");
	}

	@Test
	public void testValidate_LocationAbbr() {

		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.setLocationAbbreviation("AFG");

		when(this.locationService
			.searchLocations(new LocationSearchRequest(null, null,
				new ArrayList<>(Arrays.asList(germplasmUpdateDTO.getLocationAbbreviation())), null), null, null))
			.thenReturn(Arrays.asList(new LocationDTO()));

		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);
		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateDtoValidator.validateLocationAbbreviation(errors, germplasmUpdateList);
		Mockito.verify(errors).reject("germplasm.update.invalid.location.abbreviation", new String[] {"AFG"}, "");
	}

	@Test
	public void testValidate_BreedingMethod() {
		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.setBreedingMethodAbbr("UAC");

		when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class),
			ArgumentMatchers.any(),
			ArgumentMatchers.isNull()))
			.thenReturn(Arrays.asList(new BreedingMethodDTO()));

		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);
		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateDtoValidator.validateBreedingMethod(errors, germplasmUpdateList);
		Mockito.verify(errors).reject("germplasm.update.invalid.breeding.method", new String[] {"UAC"}, "");
	}

	@Test
	public void testValidate_CreationDate() {
		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.setCreationDate("AAAABBCC");
		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);
		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateDtoValidator.validateCreationDate(errors, germplasmUpdateList);
		Mockito.verify(errors).reject("germplasm.update.invalid.creation.date", "");
	}

	@Test
	public void testValidateProgenitorsBothMustBeSpecified_OneOfProgenitorsIsNotSpecified() {
		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.getProgenitors().put(GermplasmServiceImpl.PROGENITOR_1, null);
		germplasmUpdateDTO.getProgenitors().put(GermplasmServiceImpl.PROGENITOR_2, 1);
		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);
		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateDtoValidator.validateProgenitorsBothMustBeSpecified(errors, germplasmUpdateList);

		germplasmUpdateDTO.getProgenitors().put(GermplasmServiceImpl.PROGENITOR_1, 1);
		germplasmUpdateDTO.getProgenitors().put(GermplasmServiceImpl.PROGENITOR_2, null);
		this.germplasmUpdateDtoValidator.validateProgenitorsBothMustBeSpecified(errors, germplasmUpdateList);

		Mockito.verify(errors, times(2)).reject("germplasm.update.invalid.progenitors", "");
	}

	@Test
	public void testValidateProgenitorsBothMustBeSpecified_Valid() {
		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.getProgenitors().put(GermplasmServiceImpl.PROGENITOR_1, 1);
		germplasmUpdateDTO.getProgenitors().put(GermplasmServiceImpl.PROGENITOR_2, 2);
		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);
		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateDtoValidator.validateProgenitorsBothMustBeSpecified(errors, germplasmUpdateList);
		Mockito.verifyZeroInteractions(errors);
	}

	@Test
	public void testValidate_ProgenitorsGid_GidsDoNotExist() {
		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.getProgenitors().put(GermplasmServiceImpl.PROGENITOR_1, 1);
		germplasmUpdateDTO.getProgenitors().put(GermplasmServiceImpl.PROGENITOR_2, 2);
		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);
		final BindingResult errors = Mockito.mock(BindingResult.class);

		when(this.germplasmMiddlewareService.getGermplasmByGIDs(Mockito.anyList()))
			.thenReturn(Arrays.asList(new Germplasm(3), new Germplasm(4)));

		this.germplasmUpdateDtoValidator.validateProgenitorsGids(errors, germplasmUpdateList);
		Mockito.verify(errors).reject("germplasm.update.invalid.progenitors.gids", new String[] {"1,2"}, "");
	}

	@Test
	public void testValidate_ProgenitorsGid_Valid() {
		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.getProgenitors().put(GermplasmServiceImpl.PROGENITOR_1, 1);
		germplasmUpdateDTO.getProgenitors().put(GermplasmServiceImpl.PROGENITOR_2, 2);
		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);
		final BindingResult errors = Mockito.mock(BindingResult.class);

		when(this.germplasmMiddlewareService.getGermplasmByGIDs(Mockito.anyList()))
			.thenReturn(Arrays.asList(new Germplasm(1), new Germplasm(2)));

		this.germplasmUpdateDtoValidator.validateProgenitorsGids(errors, germplasmUpdateList);
		Mockito.verifyZeroInteractions(errors);
	}

	@Test
	public void testValidate_ProgenitorSameAsGid() {
		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.setGid(1);
		germplasmUpdateDTO.getProgenitors().put(GermplasmServiceImpl.PROGENITOR_1, 1);
		germplasmUpdateDTO.getProgenitors().put(GermplasmServiceImpl.PROGENITOR_2, 2);
		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);
		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateDtoValidator.validateProgenitorMustNotBeGid(errors, germplasmUpdateList);
		Mockito.verify(errors).reject("germplasm.update.progenitors.can.not.be.equals.to.gid");
	}
}
