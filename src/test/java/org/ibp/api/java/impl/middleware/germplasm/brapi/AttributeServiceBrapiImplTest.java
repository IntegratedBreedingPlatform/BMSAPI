package org.ibp.api.java.impl.middleware.germplasm.brapi;

import org.generationcp.middleware.api.brapi.VariableServiceBrapi;
import org.generationcp.middleware.api.brapi.v2.attribute.AttributeDTO;
import org.generationcp.middleware.domain.search_request.brapi.v2.AttributeSearchRequestDTO;
import org.ibp.api.brapi.v2.AttributeServiceBrapi;
import org.ibp.api.java.impl.middleware.ontology.brapi.AttributeServiceBrapiImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class AttributeServiceBrapiImplTest {

	@Mock
	private VariableServiceBrapi middlewareVariableServiceBrapi;

	@InjectMocks
	private AttributeServiceBrapi attributeServiceBrapi = new AttributeServiceBrapiImpl();

	@Test
	public void testCountGermplasmAttributes() {
		final AttributeSearchRequestDTO requestDTO = new AttributeSearchRequestDTO();
		Mockito.when(this.middlewareVariableServiceBrapi.countGermplasmAttributes(requestDTO)).thenReturn((long)1);
		Assert.assertEquals((long)1, this.attributeServiceBrapi.countGermplasmAttributes(requestDTO));
		Mockito.verify(this.middlewareVariableServiceBrapi).countGermplasmAttributes(requestDTO);
	}

	@Test
	public void testGetObservationVariables() {
		final AttributeSearchRequestDTO requestDTO = new AttributeSearchRequestDTO();
		final String cropName = "MAIZE";
		final AttributeDTO attributeDTO = new AttributeDTO();
		attributeDTO.setCommonCropName(cropName);
		Mockito.when(this.middlewareVariableServiceBrapi.getGermplasmAttributes(requestDTO, null))
			.thenReturn(Collections.singletonList(attributeDTO));
		final List<AttributeDTO> result = this.attributeServiceBrapi.getGermplasmAttributes(cropName, requestDTO, null);
		Mockito.verify(this.middlewareVariableServiceBrapi).getGermplasmAttributes(requestDTO, null);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(attributeDTO, result.get(0));
	}
}
