package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.commons.service.GermplasmCodeGenerationService;
import org.generationcp.commons.service.GermplasmNamingService;
import org.generationcp.middleware.domain.germplasm.GermplasmCodeNameBatchRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.generationcp.middleware.service.api.GermplasmGroupNamingResult;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmCodeNameBatchRequestValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmNameRequestValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class GermplasmNameServiceImpl implements GermplasmNameService {

	@Autowired
	private org.generationcp.middleware.api.germplasm.GermplasmNameService germplasmNameService;

	@Autowired
	private GermplasmNameRequestValidator germplasmNameRequestValidator;

	@Autowired
	private GermplasmCodeNameBatchRequestValidator germplasmCodeNameBatchRequestValidator;

	@Autowired
	private LocationValidator locationValidator;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private GermplasmCodeGenerationService germplasmCodeGenerationService;

	@Autowired
	private GermplasmNamingService germplasmNamingService;

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
	public List<GermplasmGroupNamingResult> createCodeNames(final GermplasmCodeNameBatchRequestDto germplasmCodeNameBatchRequestDto) {
		this.germplasmCodeNameBatchRequestValidator.validate(germplasmCodeNameBatchRequestDto);
		try {
			return this.germplasmCodeGenerationService.createCodeNames(germplasmCodeNameBatchRequestDto);
		} catch (final Exception e) {
			throw new ApiRuntimeException("An error has occurred when trying generate code names", e);
		}
	}

	@Override
	public String getNextNameInSequence(final GermplasmNameSetting germplasmNameSetting) {
		try {
			return this.germplasmNamingService.getNextNameInSequence(germplasmNameSetting);
		} catch (final Exception e) {
			throw new ApiRuntimeException("An error has occurred when trying generate next name in sequence", e);
		}
	}
}
