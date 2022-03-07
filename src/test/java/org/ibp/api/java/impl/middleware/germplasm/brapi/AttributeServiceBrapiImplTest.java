package org.ibp.api.java.impl.middleware.germplasm.brapi;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.VariableServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableTypeGroup;
import org.generationcp.middleware.api.brapi.v2.attribute.AttributeDTO;
import org.generationcp.middleware.domain.search_request.brapi.v2.AttributeSearchRequestDTO;
import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.service.api.study.VariableDTO;
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
	private final AttributeServiceBrapi attributeServiceBrapi = new AttributeServiceBrapiImpl();

	@Test
	public void testCountGermplasmAttributes() {
		final AttributeSearchRequestDTO requestDTO = new AttributeSearchRequestDTO();
		Mockito.when(this.middlewareVariableServiceBrapi
			.countVariables(new VariableSearchRequestDTO(requestDTO), VariableTypeGroup.GERMPLASM_ATTRIBUTES)).thenReturn((long)1);
		Assert.assertEquals((long)1, this.attributeServiceBrapi.countGermplasmAttributes(requestDTO));
		Mockito.verify(this.middlewareVariableServiceBrapi)
			.countVariables(new VariableSearchRequestDTO(requestDTO), VariableTypeGroup.GERMPLASM_ATTRIBUTES);
	}

	@Test
	public void testGetGermplasmAttributes() {
		final AttributeSearchRequestDTO requestDTO = new AttributeSearchRequestDTO();
		final String cropName = "MAIZE";
		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(RandomStringUtils.randomAlphabetic(10));
		variableDTO.setObservationVariableName(RandomStringUtils.randomAlphabetic(10));
		Mockito.when(this.middlewareVariableServiceBrapi.
			getVariables(new VariableSearchRequestDTO(requestDTO), null, VariableTypeGroup.GERMPLASM_ATTRIBUTES))
			.thenReturn(Collections.singletonList(variableDTO));
		final List<AttributeDTO> result = this.attributeServiceBrapi.getGermplasmAttributes(cropName, requestDTO, null);
		Mockito.verify(this.middlewareVariableServiceBrapi)
			.getVariables(new VariableSearchRequestDTO(requestDTO), null, VariableTypeGroup.GERMPLASM_ATTRIBUTES);
		Assert.assertEquals(1, result.size());
		final AttributeDTO attributeDTO = result.get(0);
		Assert.assertEquals(variableDTO.getObservationVariableDbId(), attributeDTO.getAttributeDbId());
		Assert.assertEquals(variableDTO.getObservationVariableName(), attributeDTO.getAttributeName());
		Assert.assertEquals(cropName, attributeDTO.getCommonCropName());
	}
}
