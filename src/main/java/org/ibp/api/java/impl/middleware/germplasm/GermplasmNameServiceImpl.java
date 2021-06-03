package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.commons.service.GermplasmCodeGenerationService;
import org.generationcp.middleware.domain.germplasm.GermplasmNameBatchRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.GermplasmGroupNamingResult;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmNameRequestValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.Map;

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

	@Autowired
	private GermplasmCodeGenerationService germplasmCodeGenerationService;

	@Override
	public void deleteName(final Integer gid, final Integer nameId) {
		this.germplasmNameRequestValidator.validateNameDeletable(gid, nameId);
		this.germplasmNameService.deleteName(nameId);
	}

	@Override
	public void updateName(final String programUUID, final GermplasmNameRequestDto germplasmNameRequestDto, final Integer gid,
		final Integer nameId) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), GermplasmNameRequestDto.class.getName());
		this.germplasmNameRequestValidator.validate(programUUID, germplasmNameRequestDto, gid, nameId);
		if (germplasmNameRequestDto.getLocationId() != null) {
			this.locationValidator.validateLocation(errors, germplasmNameRequestDto.getLocationId(), programUUID);
		}
		this.germplasmNameService.updateName(germplasmNameRequestDto, gid, nameId);
	}

	@Override
	public Integer createName(final String programUUID, final GermplasmNameRequestDto germplasmNameRequestDto, final Integer gid) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), GermplasmNameRequestDto.class.getName());
		this.germplasmNameRequestValidator.validate(programUUID, germplasmNameRequestDto, gid, null);
		this.locationValidator.validateLocation(errors, germplasmNameRequestDto.getLocationId(), programUUID);
		return this.germplasmNameService.createName(germplasmNameRequestDto, gid);
	}

	@Override
	public Map<Integer, GermplasmGroupNamingResult> createNames(final String programUUID,
		final GermplasmNameBatchRequestDto germplasmNameBatchRequestDto) {
		try {
			return this.germplasmCodeGenerationService.createCodeNames(germplasmNameBatchRequestDto);
		} catch (final Exception e) {
			throw new ApiRuntimeException("An error has occurred when trying generate code names", e);
		}
	}
}
