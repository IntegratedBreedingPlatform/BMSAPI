package org.ibp.api.brapi;

import org.generationcp.middleware.api.brapi.v2.attribute.AttributeValueDto;
import org.generationcp.middleware.domain.search_request.brapi.v2.AttributeValueSearchRequestDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AttributeValueService {

	List<AttributeValueDto> getAttributeValues(AttributeValueSearchRequestDto attributeValueSearchDto,
		Pageable pageable, String programUUID);

	long countAttributeValues(AttributeValueSearchRequestDto attributeValueSearchDto, String programUUID);
}
