package org.ibp.api.java.impl.middleware.germplasm.brapi;

import org.generationcp.middleware.api.brapi.VariableServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableTypeGroup;
import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.brapi.v2.variable.VariableUpdateResponse;
import org.ibp.api.java.impl.middleware.common.validator.VariableUpdateValidator;
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

    @Mock
    private VariableUpdateValidator variableUpdateValidator;

    @InjectMocks
    private final org.ibp.api.brapi.VariableServiceBrapi variableServiceBrapi = new VariableServiceBrapiImpl();

    @Test
    public void testCountObservationVariables() {
        final VariableSearchRequestDTO requestDTO = new VariableSearchRequestDTO();
        Mockito.when(this.middlewareVariableServiceBrapi.countVariables(requestDTO, VariableTypeGroup.TRAIT)).thenReturn((long)1);
        Assert.assertEquals((long)1, this.variableServiceBrapi.countObservationVariables(requestDTO));
        Mockito.verify(this.middlewareVariableServiceBrapi).countVariables(requestDTO, VariableTypeGroup.TRAIT);
    }

    @Test
    public void testGetObservationVariables() {
        final VariableSearchRequestDTO requestDTO = new VariableSearchRequestDTO();
        final String cropName = "MAIZE";
        final VariableDTO variableDTO = new VariableDTO();
        variableDTO.setCommonCropName(cropName);
        Mockito.when(this.middlewareVariableServiceBrapi.getVariables(requestDTO, null, VariableTypeGroup.TRAIT))
                .thenReturn(Collections.singletonList(variableDTO));
        final List<VariableDTO> result = this.variableServiceBrapi.getObservationVariables(cropName, requestDTO, null);
        Mockito.verify(this.middlewareVariableServiceBrapi).getVariables(requestDTO, null, VariableTypeGroup.TRAIT);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(variableDTO, result.get(0));
    }

    @Test
    public void testUpdateObservationVariable() {
        final VariableDTO variableDTO = new VariableDTO();
        Mockito.when(this.middlewareVariableServiceBrapi.updateObservationVariable(variableDTO)).thenReturn(variableDTO);
        final VariableUpdateResponse variableUpdateResponse = this.variableServiceBrapi.updateObservationVariable(null, variableDTO);
        Assert.assertEquals(variableDTO, variableUpdateResponse.getEntityObject());
        Mockito.verify(this.middlewareVariableServiceBrapi).updateObservationVariable(variableDTO);

    }
}
