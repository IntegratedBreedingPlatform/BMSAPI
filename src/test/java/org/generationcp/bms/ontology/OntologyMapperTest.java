package org.generationcp.bms.ontology;

import org.generationcp.bms.ontology.dto.outgoing.MethodDTO;
import org.generationcp.bms.ontology.services.OntologyMapper;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.domain.oms.Term;
import org.junit.Assert;
import org.modelmapper.ModelMapper;

import org.junit.Test;

public class OntologyMapperTest {

    @Test
    public void shouldMapHashMapToFoo()
    {
        Term term = new Term();
        term.setId(1);
        term.setName("name");
        term.setDefinition("def");

        Method method = new Method(term);

        ModelMapper mapper = OntologyMapper.methodMapper();

        MethodDTO methodDTO = mapper.map(method, MethodDTO.class);
        
        Assert.assertEquals((long) method.getId(), (long) methodDTO.getId());
        Assert.assertEquals(method.getName(), methodDTO.getName());
        Assert.assertEquals(method.getDefinition(), methodDTO.getDescription());
        
    }

}
