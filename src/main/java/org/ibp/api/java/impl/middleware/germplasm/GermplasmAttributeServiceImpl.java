package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.api.brapi.v1.attribute.AttributeDTO;
import org.generationcp.middleware.domain.shared.RecordAttributeDto;
import org.generationcp.middleware.domain.shared.AttributeRequestDto;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplasmAttributeService;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmAttributeValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class GermplasmAttributeServiceImpl implements GermplasmAttributeService {

	@Autowired
	private org.generationcp.middleware.api.germplasm.GermplasmAttributeService germplasmAttributeService;

	@Autowired
	private GermplasmAttributeValidator germplasmAttributeValidator;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@Autowired
	private LocationValidator locationValidator;

	private BindingResult errors;

	@Override
	public List<RecordAttributeDto> getGermplasmAttributeDtos(final Integer gid, final Integer variableTypeId, final String programUUID) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(errors, Collections.singletonList(gid));
		if (variableTypeId != null) {
			this.germplasmAttributeValidator.validateAttributeType(errors, variableTypeId);
		}
		return this.germplasmAttributeService.getGermplasmAttributeDtos(gid, variableTypeId, programUUID);
	}

	@Override
	public AttributeRequestDto createGermplasmAttribute(final Integer gid, final AttributeRequestDto dto,
		final String programUUID) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(errors, Collections.singletonList(gid));
		this.germplasmAttributeValidator.validateAttribute(errors, gid, dto, null);
		this.locationValidator.validateLocation(errors, dto.getLocationId());
		this.germplasmAttributeService.createGermplasmAttribute(gid, dto);
		return dto;
	}

	@Override
	public AttributeRequestDto updateGermplasmAttribute(final Integer gid, final Integer attributeId, final AttributeRequestDto dto, final String programUUID) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(errors, Collections.singletonList(gid));
		this.germplasmAttributeValidator.validateAttribute(errors, gid, dto, attributeId);
		this.locationValidator.validateLocation(errors, dto.getLocationId());
		this.germplasmAttributeService.updateGermplasmAttribute(attributeId, dto);
		return dto;
	}

	@Override
	public void deleteGermplasmAttribute(final Integer gid, final Integer attributeId) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(errors, Collections.singletonList(gid));
		this.germplasmAttributeValidator.validateGermplasmAttributeExists(errors, gid, attributeId);
		this.germplasmAttributeService.deleteGermplasmAttribute(attributeId);
	}

	@Override
	public List<AttributeDTO> getAttributesByGUID(
		final String germplasmUUID, final List<String> attributeDbIds, final Pageable pageable) {
		this.validateGuidAndAttributes(germplasmUUID, attributeDbIds);
		return this.germplasmAttributeService.getAttributesByGUID(germplasmUUID, attributeDbIds, pageable);

	}

	@Override
	public long countAttributesByGUID(final String germplasmUUID, final List<String> attributeDbIds) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		this.germplasmValidator.validateGermplasmUUID(this.errors, germplasmUUID);
		if (this.errors.hasErrors()) {
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}
		return this.germplasmAttributeService.countAttributesByGUID(germplasmUUID, attributeDbIds);
	}

	private void validateGuidAndAttributes(final String germplasmGUID, final List<String> attributeDbIds) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), AttributeDTO.class.getName());
		this.germplasmValidator.validateGermplasmUUID(this.errors, germplasmGUID);
		if (this.errors.hasErrors()) {
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}
		this.germplasmAttributeValidator.validateAttributeIds(this.errors, attributeDbIds);
		if (this.errors.hasErrors()) {
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}
	}

}
