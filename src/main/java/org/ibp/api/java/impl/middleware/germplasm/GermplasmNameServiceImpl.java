package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmNameRequestValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

@Service
@Transactional
public class GermplasmNameServiceImpl implements GermplasmNameService {

	@Autowired
	private org.generationcp.middleware.api.germplasm.GermplasmNameService germplasmNameService;

	@Autowired
	private GermplasmNameRequestValidator germplasmNameRequestValidator;

	@Autowired
	private LocationValidator locationValidator;

	@Autowired
	private SecurityService securityService;

	@Override
	public void deleteName(final Integer gid, final Integer nameId) {
		this.germplasmNameRequestValidator.validateNameDeletable(gid, nameId);
		germplasmNameService.deleteName(nameId);
	}

	@Override
	public void updateName(final String programUUID, final GermplasmNameRequestDto germplasmNameRequestDto, final Integer gid, final Integer nameId) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), GermplasmNameRequestDto.class.getName());
		germplasmNameRequestValidator.validate(programUUID, germplasmNameRequestDto, gid, nameId);
		if (germplasmNameRequestDto.getLocationId() != null) {
			locationValidator.validateLocation(errors, germplasmNameRequestDto.getLocationId(), programUUID);
		}
		germplasmNameService.updateName(germplasmNameRequestDto, gid, nameId);
	}

	@Override
	public Integer createName(final String programUUID, final GermplasmNameRequestDto germplasmNameRequestDto, final Integer gid) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), GermplasmNameRequestDto.class.getName());
		germplasmNameRequestValidator.validate(programUUID, germplasmNameRequestDto, gid, null);
		locationValidator.validateLocation(errors, germplasmNameRequestDto.getLocationId(), programUUID);
		return germplasmNameService.createName(germplasmNameRequestDto, gid);
	}
}
