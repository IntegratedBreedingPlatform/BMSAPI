package org.ibp.api.java.impl.middleware.germplasm.brapi;

import org.generationcp.middleware.api.brapi.VariableServiceBrapi;
import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.java.impl.middleware.ontology.brapi.VariableServiceBrapiImpl;
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
public class VariableServiceBrapiImplTest {

    @Mock
    private VariableServiceBrapi middlewareVariableServiceBrapi;

    @InjectMocks
    private org.ibp.api.brapi.VariableServiceBrapi variableServiceBrapi = new VariableServiceBrapiImpl();

    @Test
    public void testCountObservationVariables() {
        final VariableSearchRequestDTO requestDTO = new VariableSearchRequestDTO();
        Mockito.when(this.middlewareVariableServiceBrapi.countObservationVariables(requestDTO)).thenReturn((long)1);
        Assert.assertEquals((long)1, this.variableServiceBrapi.countObservationVariables(requestDTO));
        Mockito.verify(this.middlewareVariableServiceBrapi).countObservationVariables(requestDTO);
    }

    @Test
    public void testGetObservationVariables() {
        final VariableSearchRequestDTO requestDTO = new VariableSearchRequestDTO();
        final String cropName = "MAIZE";
        final VariableDTO variableDTO = new VariableDTO();
        variableDTO.setCommonCropName(cropName);
        Mockito.when(this.middlewareVariableServiceBrapi.getObservationVariables(cropName, requestDTO, null))
                .thenReturn(Collections.singletonList(variableDTO));
        final List<VariableDTO> result = this.variableServiceBrapi.getObservationVariables(cropName, requestDTO, null);
        Mockito.verify(this.middlewareVariableServiceBrapi).getObservationVariables(cropName, requestDTO, null);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(variableDTO, result.get(0));
    }
}
