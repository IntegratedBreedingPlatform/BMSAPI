package org.ibp.api.java.impl.middleware.germplasm.validator;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportDTO;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportRequestDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmInventoryImportDTO;
import org.generationcp.middleware.domain.inventory.manager.LotDto;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.olap4j.impl.ArrayMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmImportRequestDtoValidatorTest {

	@Mock
	private GermplasmService germplasmService;

	@Mock
	private BreedingMethodService breedingMethodService;

	@Mock
	private GermplasmNameService germplasmNameService;

	@Mock
	private LocationService locationService;

	@Mock
	private InventoryCommonValidator inventoryCommonValidator;

	@Mock
	private LotService lotService;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@InjectMocks
	private GermplasmImportRequestDtoValidator germplasmImportRequestDtoValidator;

	private final String programUUID = RandomStringUtils.randomAlphabetic(10);

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenRequestIsNull() {
		try {

			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.request.null"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenGermplasmListIsNull() {
		try {
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.list.null"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenGermplasmListIsEmpty() {
		try {
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			final List<GermplasmImportDTO> germplasmImportDTOList = new ArrayList<>();
			germplasmImportRequestDto.setGermplasmList(germplasmImportDTOList);
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.list.null"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenConnectUsingIsNull() {
		try {
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			final List<GermplasmImportDTO> germplasmImportDTOList = new ArrayList<>();
			final GermplasmImportDTO germplasmImportDTO = new GermplasmImportDTO();
			germplasmImportDTOList.add(germplasmImportDTO);
			germplasmImportRequestDto.setGermplasmList(germplasmImportDTOList);
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.connect.using.null"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenAtLeastOneGermplasmImportDtoIsNull() {
		try {
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.NONE);
			final List<GermplasmImportDTO> germplasmImportDTOList = new ArrayList<>();
			germplasmImportDTOList.add(null);
			germplasmImportRequestDto.setGermplasmList(germplasmImportDTOList);
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.germplasm.null"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenGermplasmImportDTONamesIsNull() {
		try {
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.NONE);
			final List<GermplasmImportDTO> germplasmImportDTOList = new ArrayList<>();
			final GermplasmImportDTO germplasmImportDTO = new GermplasmImportDTO();
			germplasmImportDTOList.add(germplasmImportDTO);
			germplasmImportRequestDto.setGermplasmList(germplasmImportDTOList);
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.names.null.or.empty"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenGermplasmImportDTONamesIsEmpty() {
		try {
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.NONE);
			final List<GermplasmImportDTO> germplasmImportDTOList = new ArrayList<>();
			final GermplasmImportDTO germplasmImportDTO = new GermplasmImportDTO();
			germplasmImportDTO.setNames(new ArrayMap<>());
			germplasmImportDTOList.add(germplasmImportDTO);
			germplasmImportRequestDto.setGermplasmList(germplasmImportDTOList);
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.names.null.or.empty"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenPreferredNameIsEmpty() {
		try {
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.NONE);
			final List<GermplasmImportDTO> germplasmImportDTOList = new ArrayList<>();
			final GermplasmImportDTO germplasmImportDTO = new GermplasmImportDTO();
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", "MYNAME");
			germplasmImportDTO.setNames(names);
			germplasmImportDTOList.add(germplasmImportDTO);
			germplasmImportRequestDto.setGermplasmList(germplasmImportDTOList);
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.preferred.name.null"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenReferenceIsInvalid() {
		try {
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.NONE);
			final List<GermplasmImportDTO> germplasmImportDTOList = new ArrayList<>();
			final GermplasmImportDTO germplasmImportDTO = new GermplasmImportDTO();
			germplasmImportDTO.setPreferredName("LNAME");
			germplasmImportDTO
				.setReference(RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH + 1));
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", "MYNAME");
			germplasmImportDTO.setNames(names);
			germplasmImportDTOList.add(germplasmImportDTO);
			germplasmImportRequestDto.setGermplasmList(germplasmImportDTOList);
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.reference.length.error"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenCreationDateIsNull() {
		try {
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.NONE);
			final List<GermplasmImportDTO> germplasmImportDTOList = new ArrayList<>();
			final GermplasmImportDTO germplasmImportDTO = new GermplasmImportDTO();
			germplasmImportDTO.setPreferredName("LNAME");
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", "MYNAME");
			germplasmImportDTO.setNames(names);
			germplasmImportDTOList.add(germplasmImportDTO);
			germplasmImportRequestDto.setGermplasmList(germplasmImportDTOList);
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.creation.date.null"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenCreationDateIsInvalid() {
		try {
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.NONE);
			final List<GermplasmImportDTO> germplasmImportDTOList = new ArrayList<>();
			final GermplasmImportDTO germplasmImportDTO = new GermplasmImportDTO();
			germplasmImportDTO.setPreferredName("LNAME");
			germplasmImportDTO.setCreationDate("2020");
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", "MYNAME");
			germplasmImportDTO.setNames(names);
			germplasmImportDTOList.add(germplasmImportDTO);
			germplasmImportRequestDto.setGermplasmList(germplasmImportDTOList);
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.creation.date.invalid"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenClientIdIsNull() {
		try {
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.NONE);
			final List<GermplasmImportDTO> germplasmImportDTOList = new ArrayList<>();
			final GermplasmImportDTO germplasmImportDTO = new GermplasmImportDTO();
			germplasmImportDTO.setPreferredName("LNAME");
			germplasmImportDTO.setCreationDate("20201212");
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", "MYNAME");
			germplasmImportDTO.setNames(names);
			germplasmImportDTOList.add(germplasmImportDTO);
			germplasmImportRequestDto.setGermplasmList(germplasmImportDTOList);
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.client.id.null"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenClientIdIsDuplicated() {
		try {
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.NONE);
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.NAME_MAX_LENGTH));
			final List<GermplasmImportDTO> germplasmImportDTOList = new ArrayList<>();
			final GermplasmImportDTO germplasmImportDTO1 = new GermplasmImportDTO(1, null, "ARG",
				"BM",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
				"20201212", null, null);
			germplasmImportDTOList.add(germplasmImportDTO1);

			final GermplasmImportDTO germplasmImportDTO2 = new GermplasmImportDTO(1, null, "ARG",
				"BM",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
				"20201212", null, null);
			germplasmImportDTOList.add(germplasmImportDTO2);
			germplasmImportRequestDto.setGermplasmList(germplasmImportDTOList);
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.client.id.duplicated"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenBreedingMethodIsNull() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.NAME_MAX_LENGTH));
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(
				new GermplasmImportDTO(1, null, "ARG",
					null,
					RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
					"20201212", null, null)));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.breeding.method.mandatory"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenBreedingMethodIsEmpty() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.NAME_MAX_LENGTH));
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(
				new GermplasmImportDTO(1, null, "ARG",
					"",
					RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
					"20201212", null, null)));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.breeding.method.mandatory"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenLocationIsNull() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.NAME_MAX_LENGTH));
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(
				new GermplasmImportDTO(1, null, null,
					"MUT",
					RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
					"20201212", null, null)));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.location.mandatory"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenLocationIsEmpty() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.NAME_MAX_LENGTH));
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(
				new GermplasmImportDTO(1, null, "",
					"MUT",
					RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
					"20201212", null, null)));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.location.mandatory"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenGermplasmPUIIsInvalid() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.NAME_MAX_LENGTH));
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(
				new GermplasmImportDTO(1, RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.NAME_MAX_LENGTH + 1), "ARG",
					"MUT",
					RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
					"20201212", null, null)));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.pui.invalid.length"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenNamesContainsNullKeys() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.NAME_MAX_LENGTH));
			names.put(null, RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.NAME_MAX_LENGTH));
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, null, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
				"20201212", null, null)));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.null.name.types"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenNamesContainsDuplicatedKeys() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.NAME_MAX_LENGTH));
			names.put("lname", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.NAME_MAX_LENGTH));
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, null, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
				"20201212", null, null)));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.duplicated.name.types"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenPreferredNameIsNotANameKey() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.NAME_MAX_LENGTH));
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, null, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME1", names, null,
				"20201212", null, null)));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.preferred.name.invalid"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenANameValueIsNull() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", null);
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, null, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
				"20201212", null, null)));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.name.type.value.null.empty"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenANameValueIsInvalid() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.NAME_MAX_LENGTH + 1));
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, null, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
				"20201212", null, null)));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.name.type.value.invalid.length"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenAnAttributeKeyIsNull() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", "MYNAME");
			final Map<String, String> attributes = new HashMap<>();
			attributes.put(null, RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.ATTRIBUTE_MAX_LENGTH));
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, null, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, attributes,
				"20201212", null, null)));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.null.attributes"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenAnAttributeKeyIsDuplicated() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", "MYNAME");
			final Map<String, String> attributes = new HashMap<>();
			attributes.put("NOTE", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.ATTRIBUTE_MAX_LENGTH));
			attributes.put("note", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.ATTRIBUTE_MAX_LENGTH));
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, null, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, attributes,
				"20201212", null, null)));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.duplicated.attributes"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenAnAttributeValueIsInvalid() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", "MYNAME");
			final Map<String, String> attributes = new HashMap<>();
			attributes.put("NOTE", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.ATTRIBUTE_MAX_LENGTH + 1));
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, null, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, attributes,
				"20201212", null, null)));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.attribute.value.invalid.length"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenConnectUsingIsNoneAndProgenitorsAreSpecified() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", "MYNAME");
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.NONE);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, null, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
				"20201212", "0", "0")));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.progenitors.must.be.empty"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenInvalidProgenitorsCombination() {
		try {
			final String germplasmUUID = RandomStringUtils.randomAlphabetic(36);
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", "MYNAME");
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, germplasmUUID, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
				"20201212", "0", null)));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.import.invalid.progenitors.combination"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WheConnectUsingIsGIDAndProgenitorsAreNotNumbers() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", "MYNAME");
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, null, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
				"20201212", "1", "a")));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.progenitor.must.be.numeric.when.connecting.by.gid"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenPUIExists() {
		try {
			final String germplasmPUI = RandomStringUtils.randomAlphabetic(100);
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", "MYNAME");
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, germplasmPUI, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
				"20201212", null, null)));

			Mockito.when(this.germplasmNameService.getExistingGermplasmPUIs(Mockito.anyList())).thenReturn(Collections.singletonList(germplasmPUI));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.existent.puis"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenPuiInNamesExists() {
		try {
			final String germplasmPUI = RandomStringUtils.randomAlphabetic(100);
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", "MYNAME");
			names.put(GermplasmImportRequest.PUI_NAME_TYPE, germplasmPUI);
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, null, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
				"20201212", null, null)));

			Mockito.when(this.germplasmNameService.getExistingGermplasmPUIs(Mockito.anyList())).thenReturn(Collections.singletonList(germplasmPUI));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.existent.puis"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenBreedingMethodIsNotFound() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", "MYNAME");
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, null, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
				"20201212", "1", "1")));
			Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
				.thenReturn(Collections.emptyList());
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.breeding.methods.not.exist"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenBreedingMethodIsMutantAndProgenitorsAreSpecified() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", "MYNAME");
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, null, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
				"20201212", "1", "1")));
			final BreedingMethodDTO breedingMethodDTO = new BreedingMethodDTO();
			breedingMethodDTO.setNumberOfProgenitors(1);
			breedingMethodDTO.setCode("MUT");
			Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
				.thenReturn(Collections.singletonList(breedingMethodDTO));
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.mutation.not.supported.when.saving.progenitors"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenLocationDoesNotExist() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", "MYNAME");
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, null, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, null,
				"20201212", null, null)));
			final BreedingMethodDTO breedingMethodDTO = new BreedingMethodDTO();
			breedingMethodDTO.setCode(RandomStringUtils.randomAlphabetic(3).toUpperCase());
			Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
				.thenReturn(Collections.singletonList(breedingMethodDTO));
			Mockito.when(this.locationService.searchLocations(Mockito.any(LocationSearchRequest.class), Mockito.isNull(), Mockito.isNull()))
				.thenReturn(Collections.emptyList());
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.location.abbreviations.not.exist"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenNameTypeDoesNotExist() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("INVAME", "MYNAME");
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, null, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "INVAME", names, null,
				"20201212", null, null)));
			final BreedingMethodDTO breedingMethodDTO = new BreedingMethodDTO();
			breedingMethodDTO.setCode(RandomStringUtils.randomAlphabetic(3).toUpperCase());
			Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
				.thenReturn(Collections.singletonList(breedingMethodDTO));
			Mockito.when(this.locationService.searchLocations(Mockito.any(LocationSearchRequest.class), Mockito.isNull(), Mockito.isNull()))
				.thenReturn(Collections.singletonList(new LocationDTO()));
			Mockito.when(this.germplasmService.filterGermplasmNameTypes(Mockito.anySet())).thenReturn(Collections.emptyList());
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.name.types.not.exist"));
		}
	}

	@Test
	public void testValidateBeforeSaving_ThrowsException_WhenAttributeIsNotFound() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("LNAME", "MYNAME");
			final Map<String, String> attributes = new HashMap<>();
			attributes.put("NOTE", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.ATTRIBUTE_MAX_LENGTH));
			final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
			germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
			germplasmImportRequestDto.setGermplasmList(Collections.singletonList(new GermplasmImportDTO(1, null, "ARG", "MUT",
				RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH), "LNAME", names, attributes,
				"20201212", null, null)));
			final BreedingMethodDTO breedingMethodDTO = new BreedingMethodDTO();
			breedingMethodDTO.setCode(RandomStringUtils.randomAlphabetic(3).toUpperCase());
			Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
				.thenReturn(Collections.singletonList(breedingMethodDTO));
			Mockito.when(this.locationService.searchLocations(Mockito.any(LocationSearchRequest.class), Mockito.isNull(), Mockito.isNull()))
				.thenReturn(Collections.singletonList(new LocationDTO()));
			Mockito.when(this.germplasmService.filterGermplasmNameTypes(Mockito.anySet()))
				.thenReturn(Collections.singletonList(new GermplasmNameTypeDTO()));
			Mockito.when(this.ontologyVariableDataManager.getWithFilter(Mockito.any())).thenReturn(
				Collections.emptyList());
			this.germplasmImportRequestDtoValidator.validateBeforeSaving(this.programUUID, germplasmImportRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.attributes.not.exist"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenListIsEmpty() {
		try {
			this.germplasmImportRequestDtoValidator.validateImportLoadedData(this.programUUID, new ArrayList<>());
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.list.null"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenListIsNull() {
		try {
			this.germplasmImportRequestDtoValidator.validateImportLoadedData(this.programUUID, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.list.null"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenGermplasmIsNull() {
		try {
			this.germplasmImportRequestDtoValidator.validateImportLoadedData(this.programUUID, Collections.singletonList(null));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.germplasm.null"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenReferenceIsInvalid() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO
				.setReference(RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.REFERENCE_MAX_LENGTH + 1));
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.reference.length.error"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenCreatedDateIsInvalid() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO.setCreationDate("2020");
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.creation.date.invalid"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenGermplasmPUIIsInvalid() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO
				.setGermplasmPUI(RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.NAME_MAX_LENGTH + 1));
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.pui.invalid.length"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenGermplasmPUIIsZero() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO.setGermplasmPUI("0");
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.pui.invalid.zero"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenClientIdIsDuplicated() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO1 = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO1.setClientId(1);
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO2 = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO2.setClientId(1);
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Lists.newArrayList(germplasmInventoryImportDTO1, germplasmInventoryImportDTO2));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.client.id.duplicated"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenNamesAreDuplicated() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO = new GermplasmInventoryImportDTO();
			final Map<String, String> names = new HashMap<>();
			names.put("lname", "a");
			names.put("Lname", "b");
			germplasmInventoryImportDTO.setNames(names);
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.duplicated.name.types"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenPreferredNameInvalid() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO = new GermplasmInventoryImportDTO();
			final Map<String, String> names = new HashMap<>();
			names.put("lname", "a");
			germplasmInventoryImportDTO.setNames(names);
			germplasmInventoryImportDTO.setPreferredName("invp");
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.preferred.name.invalid"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenNameValueIsNull() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO = new GermplasmInventoryImportDTO();
			final Map<String, String> names = new HashMap<>();
			names.put("lname", null);
			germplasmInventoryImportDTO.setNames(names);
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.name.type.value.null.empty"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenNameValueIsInvalid() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO = new GermplasmInventoryImportDTO();
			final Map<String, String> names = new HashMap<>();
			names.put("lname", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.NAME_MAX_LENGTH + 1));
			germplasmInventoryImportDTO.setNames(names);
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.name.type.value.invalid.length"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenAttributeKeyIsNull() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO = new GermplasmInventoryImportDTO();
			final Map<String, String> attributes = new HashMap<>();
			attributes.put(null, "");
			germplasmInventoryImportDTO.setAttributes(attributes);
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.null.attributes"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenAttributeKeyIsDuplicated() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO = new GermplasmInventoryImportDTO();
			final Map<String, String> attributes = new HashMap<>();
			attributes.put("note", "");
			attributes.put("NOTE", "");
			germplasmInventoryImportDTO.setAttributes(attributes);
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.duplicated.attributes"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenAttributeValueIsInvalid() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO = new GermplasmInventoryImportDTO();
			final Map<String, String> attributes = new HashMap<>();
			attributes.put("note", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.ATTRIBUTE_MAX_LENGTH + 1));
			germplasmInventoryImportDTO.setAttributes(attributes);
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.attribute.value.invalid.length"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenProgenitorsCombinationIsInvalid() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO.setProgenitor1("0");
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.invalid.progenitors.combination"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenAmountIsInvalid() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO.setAmount(-1D);
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.inventory.amount.invalid"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenGermplasmPUIIsDuplicated() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO1 = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO1.setGermplasmPUI("1");
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO2 = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO2.setGermplasmPUI("1");
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Lists.newArrayList(germplasmInventoryImportDTO1, germplasmInventoryImportDTO2));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.duplicated.puis"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenBreedingMethodIsNotFound() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO1 = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO1.setBreedingMethodAbbr("ABC");
			Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
				.thenReturn(Collections.emptyList());
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO1));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.breeding.methods.not.exist"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenBreedingMethodIsMutantAndProgenitorsAreSpecified() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO1 = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO1.setBreedingMethodAbbr("MUT");
			germplasmInventoryImportDTO1.setProgenitor1("0");
			germplasmInventoryImportDTO1.setProgenitor2("0");
			final BreedingMethodDTO breedingMethodDTO = new BreedingMethodDTO();
			breedingMethodDTO.setNumberOfProgenitors(1);
			breedingMethodDTO.setCode("MUT");
			Mockito.when(this.breedingMethodService.searchBreedingMethods(ArgumentMatchers.any(BreedingMethodSearchRequest.class), ArgumentMatchers.any(), ArgumentMatchers.isNull()))
				.thenReturn(Collections.singletonList(breedingMethodDTO));
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO1));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.mutation.not.supported.when.saving.progenitors"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenLocationDoesNotExist() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO1 = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO1.setLocationAbbr("LOC");
			Mockito.when(this.locationService.searchLocations(Mockito.any(LocationSearchRequest.class), Mockito.isNull(), Mockito.isNull()))
				.thenReturn(Collections.emptyList());
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO1));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.location.abbreviations.not.exist"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenStorageLocationDoesNotExist() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO1 = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO1.setStorageLocationAbbr("LOC");
			Mockito.when(this.locationService.searchLocations(Mockito.any(LocationSearchRequest.class), Mockito.isNull(), Mockito.isNull()))
				.thenReturn(Collections.emptyList());
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO1));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.storage.location.abbreviations.not.exist"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenNameTypeDoesNotExist() {
		try {
			final Map<String, String> names = new HashMap<>();
			names.put("INVAME", "MYNAME");
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO1 = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO1.setNames(names);
			Mockito.when(this.germplasmService.filterGermplasmNameTypes(Mockito.anySet())).thenReturn(Collections.emptyList());
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO1));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.name.types.not.exist"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenAttributeIsNotFound() {
		try {
			final Map<String, String> attributes = new HashMap<>();
			attributes.put("NOTE", RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.ATTRIBUTE_MAX_LENGTH));
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO1 = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO1.setAttributes(attributes);
			Mockito.when(this.ontologyVariableDataManager.getWithFilter(Mockito.any())).thenReturn(
				Collections.emptyList());
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO1));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.import.attributes.not.exist"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenStockIDIsDuplicated() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO1 = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO1.setStockId("A");
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO2 = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO2.setStockId("A");
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Lists.newArrayList(germplasmInventoryImportDTO1, germplasmInventoryImportDTO2));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("lot.input.list.stock.ids.duplicated"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenStockIDIsInvalid() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO1 = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO1
				.setStockId(RandomStringUtils.randomAlphabetic(GermplasmImportRequestDtoValidator.STOCK_ID_MAX_LENGTH + 1));
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO1));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("lot.stock.id.length.higher.than.maximum"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenStockIDIsFound() {
		try {
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO1 = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO1.setStockId("A");
			final LotDto lotDto = new LotDto();
			lotDto.setStockId("A");
			Mockito.when(this.lotService.getLotsByStockIds(Mockito.anyList())).thenReturn(Collections.singletonList(lotDto));
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO1));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("lot.input.list.stock.ids.invalid"));
		}
	}

	@Test
	public void testValidateImportLoadedData_ThrowsException_WhenUnitNameIsNotValid() {
		try {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), GermplasmInventoryImportDTO.class.getName());
			errors.reject("lot.input.invalid.units", "");
			final GermplasmInventoryImportDTO germplasmInventoryImportDTO1 = new GermplasmInventoryImportDTO();
			germplasmInventoryImportDTO1.setUnit("A");
			Mockito.doThrow(new ApiRequestValidationException(errors.getAllErrors())).when(this.inventoryCommonValidator)
				.validateUnitNames(Mockito.anyList(), Mockito.any(BindingResult.class));
			this.germplasmImportRequestDtoValidator
				.validateImportLoadedData(this.programUUID, Collections.singletonList(germplasmInventoryImportDTO1));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("lot.input.invalid.units"));
		}
	}

}
