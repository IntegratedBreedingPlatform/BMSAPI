package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmNameValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
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
	org.generationcp.middleware.api.germplasm.GermplasmNameService germplasmNameService;

	@Autowired
	private SecurityService securityService;

	@Override
	public void deleteName(final GermplasmNameRequestDto germplasmNameRequestDto) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), GermplasmNameRequestDto.class.getName());
		this.germplasmNameValidator.validateDeleteName(germplasmNameRequestDto);
		germplasmNameService.deleteName(germplasmNameRequestDto.getId());
	}

	@Override
	public void updateName(final GermplasmNameRequestDto germplasmNameRequestDto, final String programUUID) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), GermplasmNameRequestDto.class.getName());
		germplasmNameValidator.validate(germplasmNameRequestDto, programUUID);
		germplasmNameService.updateName(germplasmNameRequestDto);
	}

	@Override
	public Integer createName(final GermplasmNameRequestDto germplasmNameRequestDto, final String programUUID) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), GermplasmNameRequestDto.class.getName());
		germplasmNameValidator.validate(germplasmNameRequestDto, programUUID);
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		return germplasmNameService.createName(germplasmNameRequestDto, loggedInUser.getUserid());
	}
}
