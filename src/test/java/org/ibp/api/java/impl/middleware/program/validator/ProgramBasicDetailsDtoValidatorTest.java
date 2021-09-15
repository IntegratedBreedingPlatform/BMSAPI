package org.ibp.api.java.impl.middleware.program.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.program.ProgramBasicDetailsDto;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.api.program.ProgramService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ProgramBasicDetailsDtoValidatorTest {

	private final String programUUID = RandomStringUtils.randomAlphabetic(16);
	private final String cropName = "maize";

	@Mock
	private ProgramService programService;

	@InjectMocks
	private ProgramBasicDetailsDtoValidator programBasicDetailsDtoValidator;

	@Test
	public void testValidateCreation_throwsException_whenRequestBodyIsNull() {
		try {
			programBasicDetailsDtoValidator.validateCreation(cropName, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("param.null"));
		}
	}

	@Test
	public void testValidateCreation_throwsException_whenNameIsNull() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		programBasicDetailsDto.setStartDate("10102020");
		try {
			programBasicDetailsDtoValidator.validateCreation(cropName, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("param.null"));
		}
	}

	@Test
	public void testValidateCreation_throwsException_whenStartDateIsNull() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		programBasicDetailsDto.setName(RandomStringUtils.randomAlphabetic(10));
		try {
			programBasicDetailsDtoValidator.validateCreation(cropName, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("param.null"));
		}
	}

	@Test
	public void testValidateCreation_throwsException_whenStartDateIsInvalid() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		programBasicDetailsDto.setName(RandomStringUtils.randomAlphabetic(10));
		programBasicDetailsDto.setStartDate("4040202020");
		try {
			programBasicDetailsDtoValidator.validateCreation(cropName, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.start.date.invalid"));
		}
	}

	@Test
	public void testValidateCreation_throwsException_whenProgramNameLengthIsInvalid() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		programBasicDetailsDto.setName(RandomStringUtils.randomAlphabetic(300));
		programBasicDetailsDto.setStartDate("2020-10-10");
		try {
			programBasicDetailsDtoValidator.validateCreation(cropName, programBasicDetailsDto);
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
		Mockito.when(programService.getProgram(cropName, programName)).thenReturn(Optional.of(programDTO));
		try {
			programBasicDetailsDtoValidator.validateCreation(cropName, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.name.already.exists"));
		}
	}

	@Test
	public void testValidateEdition_throwsException_whenStartDateIsInvalid() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		programBasicDetailsDto.setStartDate("4040202020");
		try {
			programBasicDetailsDtoValidator.validateEdition(cropName, programUUID, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.start.date.invalid"));
		}
	}

	@Test
	public void testValidateEdition_throwsException_whenProgramNameLengthInvalid() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		programBasicDetailsDto.setName(RandomStringUtils.randomAlphabetic(300));
		try {
			programBasicDetailsDtoValidator.validateEdition(cropName, programUUID, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.name.max.length.exceeded"));
		}
	}

	@Test
	public void testValidateEdition_throwsException_whenProgramNameContainsInvalidCharacters() {
		final ProgramBasicDetailsDto programBasicDetailsDto = new ProgramBasicDetailsDto();
		programBasicDetailsDto.setName("|");
		try {
			programBasicDetailsDtoValidator.validateEdition(cropName, programUUID, programBasicDetailsDto);
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
		Mockito.when(programService.getProgram(cropName, programName)).thenReturn(Optional.of(programDTO));
		try {
			programBasicDetailsDtoValidator.validateEdition(cropName, programUUID, programBasicDetailsDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.name.already.exists"));
		}
	}

}