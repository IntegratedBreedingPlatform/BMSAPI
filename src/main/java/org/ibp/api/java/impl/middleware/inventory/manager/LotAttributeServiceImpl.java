package org.ibp.api.java.impl.middleware.inventory.manager;

import org.generationcp.middleware.domain.germplasm.AttributeRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;
import org.ibp.api.java.inventory.manager.LotAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LotAttributeServiceImpl implements LotAttributeService {

	@Autowired
	private org.generationcp.middleware.service.api.inventory.LotAttributeService lotAttributeService;

	@Override
	public List<GermplasmAttributeDto> getLotAttributeDtos(final Integer lotId, final String programUUID) {
		return this.lotAttributeService.getLotAttributeDtos(lotId, programUUID);
	}

	@Override
	public AttributeRequestDto createLotAttribute(final Integer lotId, final AttributeRequestDto dto) {
		this.lotAttributeService.createLotAttribute(lotId, dto);
		return dto;
	}

	@Override
	public AttributeRequestDto updateLotAttribute(final Integer attributeId, final AttributeRequestDto dto) {
		this.lotAttributeService.updateLotAttribute(attributeId, dto);
		return dto;
	}

	@Override
	public void deleteLotAttribute(final Integer attributeId) {
		this.lotAttributeService.deleteLotAttribute(attributeId);
	}

}
