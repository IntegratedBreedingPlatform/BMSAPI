package org.ibp.api.java.impl.middleware.inventory.manager;

import org.generationcp.middleware.domain.shared.AttributeRequestDto;
import org.generationcp.middleware.domain.shared.RecordAttributeDto;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.impl.middleware.common.validator.LotAttributeValidator;
import org.ibp.api.java.inventory.manager.LotAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class LotAttributeServiceImpl implements LotAttributeService {

	@Autowired
	private org.generationcp.middleware.service.api.inventory.LotAttributeService lotAttributeService;

	@Autowired
	private LotAttributeValidator lotAttributeValidator;

	@Autowired
	private LocationValidator locationValidator;

	@Override
	public List<RecordAttributeDto> getLotAttributeDtos(final Integer lotId, final String programUUID) {
		return this.lotAttributeService.getLotAttributeDtos(lotId, programUUID);
	}

	@Override
	public AttributeRequestDto createLotAttribute(final Integer lotId, final AttributeRequestDto dto) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.lotAttributeValidator.validateAttribute(errors, lotId, dto, null);
		this.locationValidator.validateLocation(errors, dto.getLocationId());
		this.lotAttributeService.createLotAttribute(lotId, dto);
		return dto;
	}

	@Override
	public AttributeRequestDto updateLotAttribute(final Integer lotId, final Integer attributeId, final AttributeRequestDto dto) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.lotAttributeValidator.validateAttribute(errors, lotId, dto, attributeId);
		this.locationValidator.validateLocation(errors, dto.getLocationId());
		this.lotAttributeService.updateLotAttribute(attributeId, dto);
		return dto;
	}

	@Override
	public void deleteLotAttribute(final Integer lotId, final Integer attributeId) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.lotAttributeValidator.validateLotAttributeExists(errors, lotId, attributeId);
		this.lotAttributeService.deleteLotAttribute(attributeId);
	}

}
