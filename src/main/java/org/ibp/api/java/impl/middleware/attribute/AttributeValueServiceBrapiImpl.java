package org.ibp.api.java.impl.middleware.attribute;

import org.generationcp.middleware.api.brapi.v2.attribute.AttributeValueDto;
import org.generationcp.middleware.domain.search_request.brapi.v2.AttributeValueSearchRequestDto;
import org.ibp.api.brapi.AttributeValueServiceBrapi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AttributeValueServiceBrapiImpl implements AttributeValueServiceBrapi {

	@Autowired
	private org.generationcp.middleware.api.brapi.v2.attribute.AttributeValueServiceBrapi attributeValueServiceBrapi;

	@Override
	public List<AttributeValueDto> getAttributeValues(final AttributeValueSearchRequestDto requestDTO,
		final Pageable pageable, final String programUUID) {
		return this.attributeValueServiceBrapi.getAttributeValues(requestDTO, pageable, programUUID);
	}

	@Override
	public long countAttributeValues(final AttributeValueSearchRequestDto requestDTO,
		final String programUUID) {
		return this.attributeValueServiceBrapi.countAttributeValues(requestDTO, programUUID);
	}
}
