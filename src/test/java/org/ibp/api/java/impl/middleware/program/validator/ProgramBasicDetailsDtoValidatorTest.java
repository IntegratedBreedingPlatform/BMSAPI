package org.ibp.api.java.impl.middleware.program.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.api.program.ProgramBasicDetailsDto;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.api.program.ProgramService;
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
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ProgramBasicDetailsDtoValidatorTest {

	private final String programUUID = RandomStringUtils.randomAlphabetic(16);
	private final String cropName = "maize";

	@Mock
	private ProgramService programService;

	@Mock
	private LocationService locationService;

	@InjectMocks
	private ProgramBasicDetailsDtoValidator programBasicDetailsDtoValidator;

	@Test
	public void testValidateCreation_throwsException_whenRequestBodyIsNull() {
		try {
			this.programBasicDetailsDtoValidator.validateCreation(this.cropName, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("param.null"));
		}
	}

	@Test
	public void testValidateCreation_throwsException_whenNameIsNull() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		programBasicDetailsDto.setStartDate("10102020");
		try {
			this.programBasicDetailsDtoValidator.validateCreation(this.cropName, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("param.null"));
		}
	}

	@Test
	public void testValidateCreation_throwsException_whenStartDateIsNull() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		programBasicDetailsDto.setName(RandomStringUtils.randomAlphabetic(10));
		try {
			this.programBasicDetailsDtoValidator.validateCreation(this.cropName, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("param.null"));
		}
	}

	@Test
	public void testValidateCreation_throwsException_whenStartDateIsInvalid() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		programBasicDetailsDto.setName(RandomStringUtils.randomAlphabetic(10));
		programBasicDetailsDto.setStartDate("4040202020");
		programBasicDetailsDto.setBreedingLocationDefaultId(1);
		programBasicDetailsDto.setStorageLocationDefaultId(6000);
		try {
			this.programBasicDetailsDtoValidator.validateCreation(this.cropName, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.start.date.invalid"));
		}
	}

	@Test
	public void testValidateCreation_throwsException_whenProgramNameLengthIsInvalid() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		programBasicDetailsDto.setName(RandomStringUtils.randomAlphabetic(300));
		programBasicDetailsDto.setStartDate("2020-10-10");
		programBasicDetailsDto.setBreedingLocationDefaultId(1);
		programBasicDetailsDto.setStorageLocationDefaultId(6000);
		try {
			this.programBasicDetailsDtoValidator.validateCreation(this.cropName, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.name.max.length.exceeded"));
		}
	}

	@Test
	public void testValidateCreation_throwsException_whenProgramNameExists() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		final String programName = RandomStringUtils.randomAlphabetic(20);
		final ProgramDTO programDTO = new ProgramDTO();
		programDTO.setName(programName);
		programBasicDetailsDto.setName(programName);
		programBasicDetailsDto.setStartDate("2020-10-10");
		programBasicDetailsDto.setBreedingLocationDefaultId(1);
		programBasicDetailsDto.setStorageLocationDefaultId(1);
		Mockito.when(this.programService.getProgramByCropAndName(this.cropName, programName)).thenReturn(Optional.of(programDTO));
		final LocationDTO locationDTO = new LocationDTO();
		locationDTO.setId(1);
		Mockito.when(this.locationService.searchLocations(ArgumentMatchers.any(LocationSearchRequest.class),
			ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(Collections.singletonList(locationDTO));
		try {
			this.programBasicDetailsDtoValidator.validateCreation(this.cropName, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.name.already.exists"));
		}
	}

	@Test
	public void testValidateCreation_throwsException_whenDefaultLocationIdIsNull() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		final String programName = RandomStringUtils.randomAlphabetic(20);
		final ProgramDTO programDTO = new ProgramDTO();
		programDTO.setName(programName);
		programBasicDetailsDto.setName(programName);
		programBasicDetailsDto.setStartDate("2020-10-10");
		try {
			this.programBasicDetailsDtoValidator.validateCreation(this.cropName, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("param.null"));
		}
	}

	@Test
	public void testValidateCreation_throwsException_whenBreedingLocationDefaultIdIsInvalid() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		final String programName = RandomStringUtils.randomAlphabetic(20);
		final ProgramDTO programDTO = new ProgramDTO();
		programDTO.setName(programName);
		programBasicDetailsDto.setName(programName);
		programBasicDetailsDto.setStartDate("2020-10-10");
		programBasicDetailsDto.setBreedingLocationDefaultId(1);
		programBasicDetailsDto.setStorageLocationDefaultId(6000);
		final LocationDTO locationDTO = new LocationDTO();
		locationDTO.setId(1);
		Mockito.when(this.locationService.searchLocations(ArgumentMatchers.any(LocationSearchRequest.class),
			ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(Collections.singletonList(locationDTO));
		try {
			this.programBasicDetailsDtoValidator.validateCreation(this.cropName, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.storage.location.default.id.invalid"));
		}
	}

	@Test
	public void testValidateCreation_throwsException_whenStorageLocationDefaultIdIsInvalid() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		final String programName = RandomStringUtils.randomAlphabetic(20);
		final ProgramDTO programDTO = new ProgramDTO();
		programDTO.setName(programName);
		programBasicDetailsDto.setName(programName);
		programBasicDetailsDto.setStartDate("2020-10-10");
		programBasicDetailsDto.setBreedingLocationDefaultId(1);
		programBasicDetailsDto.setStorageLocationDefaultId(6000);
		try {
			this.programBasicDetailsDtoValidator.validateCreation(this.cropName, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.breeding.location.default.id.invalid"));
		}
	}

	@Test
	public void testValidateEdition_throwsException_whenStartDateIsInvalid() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		programBasicDetailsDto.setStartDate("4040202020");
		try {
			this.programBasicDetailsDtoValidator.validateEdition(this.cropName, this.programUUID, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.start.date.invalid"));
		}
	}

	@Test
	public void testValidateEdition_throwsException_whenProgramNameLengthInvalid() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		programBasicDetailsDto.setName(RandomStringUtils.randomAlphabetic(300));
		try {
			this.programBasicDetailsDtoValidator.validateEdition(this.cropName, this.programUUID, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.name.max.length.exceeded"));
		}
	}

	@Test
	public void testValidateEdition_throwsException_whenProgramNameContainsInvalidCharacters() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		programBasicDetailsDto.setName("|");
		try {
			this.programBasicDetailsDtoValidator.validateEdition(this.cropName, this.programUUID, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.name.invalid.characters"));
		}
	}

	@Test
	public void testValidateEdition_throwsException_whenProgramNameExists() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		final String programName = RandomStringUtils.randomAlphabetic(20);
		final ProgramDTO programDTO = new ProgramDTO();
		programDTO.setName(programName);
		programBasicDetailsDto.setName(programName);
		programBasicDetailsDto.setStartDate("2020-10-10");
		Mockito.when(this.programService.getProgramByCropAndName(this.cropName, programName)).thenReturn(Optional.of(programDTO));
		try {
			this.programBasicDetailsDtoValidator.validateEdition(this.cropName, this.programUUID, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.name.already.exists"));
		}
	}

}
