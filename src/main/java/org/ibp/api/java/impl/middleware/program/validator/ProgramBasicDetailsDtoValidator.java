package org.ibp.api.java.impl.middleware.program.validator;

import org.generationcp.middleware.api.program.ProgramBasicDetailsDto;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.domain.workbench.AddProgramMemberRequestDto;
import org.generationcp.middleware.util.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.Optional;

@Component
public class ProgramBasicDetailsDtoValidator {

	private static final Integer PROGRAM_NAME_MAX_LENGTH = 255;

	@Autowired
	private ProgramService programService;

	public void validate(final String cropName, final ProgramBasicDetailsDto programBasicDetailsDto) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), AddProgramMemberRequestDto.class.getName());

		BaseValidator.checkNotNull(programBasicDetailsDto, "param.null", new String[] {"request body"});
		BaseValidator.checkNotEmpty(programBasicDetailsDto.getName(), "param.null", new String[] {"name"});
		BaseValidator.checkNotEmpty(programBasicDetailsDto.getStartDate(), "param.null", new String[] {"startDate"});

		if (programBasicDetailsDto.getName().trim().isEmpty()) {
			errors.reject("program.name.empty", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (Util.tryParseDate(programBasicDetailsDto.getStartDate(), Util.FRONTEND_DATE_FORMAT) == null) {
			errors.reject("program.start.date.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (programBasicDetailsDto.getName().length() > PROGRAM_NAME_MAX_LENGTH) {
			errors.reject("program.name.max.length.exceeded", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final Optional<ProgramDTO> programDTOOptional = this.programService.getProject(cropName, programBasicDetailsDto.getName());
		if (programDTOOptional.isPresent()) {
			errors.reject("program.name.already.exists", new String[] {programBasicDetailsDto.getName(), cropName}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}

}
