package org.ibp.api.java.impl.middleware.attribute;

import org.generationcp.middleware.api.brapi.v2.attribute.AttributeValueDto;
import org.generationcp.middleware.api.brapi.v2.attribute.AttributeValueServiceBrapi;
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
public class AttributeValueServiceBrapiBrapiImplTest {

	private static final int PAGE_SIZE = 10;
	private static final int PAGE = 1;
	private static final String PROGRAM_UUID = UUID.randomUUID().toString();

	@Mock
	private AttributeValueServiceBrapi middlewareAttributeValueServiceBrapi;

	@InjectMocks
	private org.ibp.api.brapi.AttributeValueServiceBrapi attributeValueServiceBrapi = new AttributeValueServiceBrapiImpl();

	@Test
	public void testCountAttributeValues() {
		final AttributeValueSearchRequestDto requestDTO = new AttributeValueSearchRequestDto();
		Mockito.when(this.middlewareAttributeValueServiceBrapi.countAttributeValues(requestDTO, PROGRAM_UUID)).thenReturn((long) 1);
		Assert.assertEquals((long) 1, this.attributeValueServiceBrapi.countAttributeValues(requestDTO, PROGRAM_UUID));
		Mockito.verify(this.middlewareAttributeValueServiceBrapi).countAttributeValues(requestDTO, PROGRAM_UUID);
	}

	@Test
	public void testGetAttributeValues() {
		final AttributeValueSearchRequestDto requestDTO = new AttributeValueSearchRequestDto();
		final String cropName = "MAIZE";
		final AttributeValueDto attributeValueDto = new AttributeValueDto();

		Mockito.when(this.middlewareAttributeValueServiceBrapi.getAttributeValues(requestDTO, new PageRequest(PAGE, PAGE_SIZE), PROGRAM_UUID))
			.thenReturn(Collections.singletonList(attributeValueDto));
		final List<AttributeValueDto> result =
			this.attributeValueServiceBrapi.getAttributeValues(requestDTO, new PageRequest(PAGE, PAGE_SIZE), PROGRAM_UUID);
		Mockito.verify(this.middlewareAttributeValueServiceBrapi).getAttributeValues(requestDTO, new PageRequest(PAGE, PAGE_SIZE), PROGRAM_UUID);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(attributeValueDto, result.get(0));
	}

}
