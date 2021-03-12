package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.api.brapi.v1.attribute.AttributeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmNameDto;
import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmNameValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

@Service
@Transactional
public class germplasmNameServiceImpl implements GermplasmNameService {

	@Autowired
	private GermplasmNameValidator germplasmNameValidator;

	@Autowired
	GermplasmValidator germplasmValidator;

	@Autowired
	org.generationcp.middleware.api.germplasm.GermplasmNameService germplasmNameService;

	@Override
	public void deleteName(final GermplasmNameRequestDto germplasmNameRequestDto) {
		germplasmNameValidator.validate(germplasmNameRequestDto);


		germplasmNameService.deleteName(germplasmNameRequestDto.getId());
	}

	@Override
	public void updateName(final GermplasmNameRequestDto germplasmNameRequestDto) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), GermplasmNameRequestDto.class.getName());
		germplasmNameValidator.validateUpdateName(errors, germplasmNameRequestDto);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		germplasmNameService.updateName(germplasmNameRequestDto);
	}

	@Override
	public Integer createName(final GermplasmNameRequestDto germplasmNameRequestDto) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), GermplasmNameRequestDto.class.getName());
		germplasmNameValidator.validateCreateName(errors, germplasmNameRequestDto);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		return germplasmNameService.createName(germplasmNameRequestDto);
	}
}
