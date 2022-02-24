package org.ibp.api.java.impl.middleware.attribute;

import org.generationcp.middleware.api.brapi.v2.attribute.AttributeValueDto;
import org.generationcp.middleware.api.brapi.v2.attribute.AttributeValueService;
import org.generationcp.middleware.domain.search_request.brapi.v2.AttributeValueSearchRequestDto;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class AttributeValueServiceImplTest {

	private static final int PAGE_SIZE = 10;
	private static final int PAGE = 1;
	private static final String PROGRAM_UUID = UUID.randomUUID().toString();

	@Mock
	private AttributeValueService middlewareAttributeValueService;

	@InjectMocks
	private org.ibp.api.brapi.AttributeValueService attributeValueService = new AttributeValueServiceImpl();

	@Test
	public void testCountAttributeValues() {
		final AttributeValueSearchRequestDto requestDTO = new AttributeValueSearchRequestDto();
		Mockito.when(this.middlewareAttributeValueService.countAttributeValues(requestDTO, PROGRAM_UUID)).thenReturn((long) 1);
		Assert.assertEquals((long) 1, this.attributeValueService.countAttributeValues(requestDTO, PROGRAM_UUID));
		Mockito.verify(this.middlewareAttributeValueService).countAttributeValues(requestDTO, PROGRAM_UUID);
	}

	@Test
	public void testGetAttributeValues() {
		final AttributeValueSearchRequestDto requestDTO = new AttributeValueSearchRequestDto();
		final String cropName = "MAIZE";
		final AttributeValueDto attributeValueDto = new AttributeValueDto();

		Mockito.when(this.middlewareAttributeValueService.getAttributeValues(requestDTO, new PageRequest(PAGE, PAGE_SIZE), PROGRAM_UUID))
			.thenReturn(Collections.singletonList(attributeValueDto));
		final List<AttributeValueDto> result =
			this.attributeValueService.getAttributeValues(requestDTO, new PageRequest(PAGE, PAGE_SIZE), PROGRAM_UUID);
		Mockito.verify(this.middlewareAttributeValueService).getAttributeValues(requestDTO, new PageRequest(PAGE, PAGE_SIZE), PROGRAM_UUID);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(attributeValueDto, result.get(0));
	}

}
