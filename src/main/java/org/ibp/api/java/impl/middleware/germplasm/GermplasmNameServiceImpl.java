package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.ruleengine.namingdeprecated.service.DeprecatedGermplasmNamingService;
import org.generationcp.middleware.domain.germplasm.GermplasmCodeNameBatchRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.exceptions.InvalidGermplasmNameSettingException;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.generationcp.middleware.service.api.GermplasmCodingResult;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.germplasm.GermplasmCodeGenerationService;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmCodeNameBatchRequestValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmNameRequestValidator;
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
	private GermplasmCodeGenerationService germplasmCodeGenerationService;

	@Autowired
	private DeprecatedGermplasmNamingService germplasmNamingService;

	@Override
	public void deleteName(final Integer gid, final Integer nameId) {
		this.germplasmNameRequestValidator.validateNameDeletable(gid, nameId);
		this.germplasmNameService.deleteName(nameId);
	}

	@Override
	public void updateName(final String programUUID, final GermplasmNameRequestDto germplasmNameRequestDto, final Integer gid,
		final Integer nameId) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), GermplasmNameRequestDto.class.getName());
		this.germplasmNameRequestValidator.validate(germplasmNameRequestDto, gid, nameId);
		if (germplasmNameRequestDto.getLocationId() != null) {
			this.locationValidator.validateLocation(errors, germplasmNameRequestDto.getLocationId());
		}
		this.germplasmNameService.updateName(germplasmNameRequestDto, gid, nameId);
	}

	@Override
	public Integer createName(final String programUUID, final GermplasmNameRequestDto germplasmNameRequestDto, final Integer gid) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), GermplasmNameRequestDto.class.getName());
		this.germplasmNameRequestValidator.validate(germplasmNameRequestDto, gid, null);
		this.locationValidator.validateLocation(errors, germplasmNameRequestDto.getLocationId());
		return this.germplasmNameService.createName(germplasmNameRequestDto, gid);
	}

	@Override
	public List<GermplasmCodingResult> createCodeNames(final GermplasmCodeNameBatchRequestDto germplasmCodeNameBatchRequestDto) {
		this.germplasmCodeNameBatchRequestValidator.validate(germplasmCodeNameBatchRequestDto);
		return this.germplasmCodeGenerationService.createCodeNames(germplasmCodeNameBatchRequestDto);

	}

	@Override
	public String getNextNameInSequence(final GermplasmNameSetting germplasmNameSetting) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), GermplasmNameSetting.class.getName());
		this.germplasmCodeNameBatchRequestValidator.validateGermplasmNameSetting(errors, germplasmNameSetting);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		try {
			return this.germplasmNamingService.getNextNameInSequence(germplasmNameSetting);
		} catch (final InvalidGermplasmNameSettingException e) {
			throw new ApiRuntimeException("An error has occurred when trying generate next name in sequence", e);
		}
	}
}
