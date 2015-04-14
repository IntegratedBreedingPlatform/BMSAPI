package org.ibp.api.java.impl.middleware.ontology;

import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.ibp.api.domain.ontology.MethodSummary;
import org.ibp.api.domain.ontology.PropertySummary;
import org.junit.Assert;
import org.junit.Test;
import org.modelmapper.ModelMapper;

public class OntologyMapperTest {

	@Test
	public void methodMapperTest() {
		Term term = new Term();
		term.setId(1);
		term.setName("name");
		term.setDefinition("def");

		Method method = new Method(term);

		ModelMapper mapper = OntologyMapper.methodMapper();

		MethodSummary methodSummary = mapper.map(method, MethodSummary.class);

		Assert.assertEquals(String.valueOf(method.getId()), methodSummary.getId());
		Assert.assertEquals(method.getName(), methodSummary.getName());
		Assert.assertEquals(method.getDefinition(), methodSummary.getDescription());

	}

	@Test
	public void propertyMapperTest() {
		Term term = new Term();

		term.setId(1);
		term.setName("name");
		term.setDefinition("definition");

		Property property = new Property(term);

		ModelMapper mapper = OntologyMapper.propertyMapper();

		PropertySummary propertySummary = mapper.map(property, PropertySummary.class);

		Assert.assertEquals((long) property.getId(), (long) propertySummary.getId());
		Assert.assertEquals(property.getName(), propertySummary.getName());
		Assert.assertEquals(property.getDefinition(), propertySummary.getDescription());

	}
}
