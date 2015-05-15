package org.ibp.api.java.impl.middleware.ontology;

import java.text.ParseException;
import java.util.Date;

import org.generationcp.middleware.domain.oms.OntologyVariableSummary;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.util.ISO8601DateParser;
import org.ibp.api.domain.ontology.MethodDetails;
import org.ibp.api.domain.ontology.MethodSummary;
import org.ibp.api.domain.ontology.PropertySummary;
import org.ibp.api.domain.ontology.ScaleSummary;
import org.ibp.api.domain.ontology.VariableSummary;
import org.ibp.builders.MethodBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.modelmapper.ModelMapper;

/**
 * Test to check mapping between middleware domain and bmsapi domain
 */
//TODO Add tests for mapping detailed entities.
public class OntologyMapperTest {
	
	@Test
	public void methodSummaryMapperTest() throws ParseException {
		Term term = new Term();
		term.setId(1);
		term.setName("name");
		term.setDefinition("def");

		Method method = new Method(term);
		method.setDateCreated(new Date());
		method.setDateLastModified(new Date());

		ModelMapper mapper = OntologyMapper.getInstance();

		MethodSummary methodSummary = mapper.map(method, MethodSummary.class);

		Assert.assertEquals(String.valueOf(method.getId()), methodSummary.getId());
		Assert.assertEquals(method.getName(), methodSummary.getName());
		Assert.assertEquals(method.getDefinition(), methodSummary.getDescription());
		Assert.assertEquals(method.getDateCreated(), ISO8601DateParser.parse(methodSummary.getMetadata().getDateCreated()));
		Assert.assertEquals(method.getDateLastModified(), ISO8601DateParser.parse(methodSummary.getMetadata().getDateLastModified()));
	}
	
	@Test
	public void methodDetailsMapperTest() throws ParseException {
		Method method = new MethodBuilder().build(1, "Method name", "Method description.", new Date(), new Date());
		ModelMapper mapper = OntologyMapper.getInstance();

		MethodDetails methodDetails = mapper.map(method, MethodDetails.class);

		Assert.assertEquals(String.valueOf(method.getId()), methodDetails.getId());
		Assert.assertEquals(method.getName(), methodDetails.getName());
		Assert.assertEquals(method.getDefinition(), methodDetails.getDescription());
		Assert.assertEquals(method.getDateCreated(), ISO8601DateParser.parse(methodDetails.getMetadata().getDateCreated()));
		Assert.assertEquals(method.getDateLastModified(), ISO8601DateParser.parse(methodDetails.getMetadata().getDateLastModified()));
		Assert.assertTrue(methodDetails.getMetadata().getEditableFields().isEmpty());
		Assert.assertFalse(methodDetails.getMetadata().isDeletable());
		Assert.assertTrue(methodDetails.getMetadata().getUsage().getVariables().isEmpty());
	}

	@Test
	public void propertySummaryMapperTest() throws ParseException {
		Term term = new Term();

		term.setId(1);
		term.setName("name");
		term.setDefinition("definition");

		Property property = new Property(term);
		property.setDateCreated(new Date());
		property.setDateLastModified(new Date());

		ModelMapper mapper = OntologyMapper.getInstance();

		PropertySummary propertySummary = mapper.map(property, PropertySummary.class);

		Assert.assertEquals(String.valueOf(property.getId()), propertySummary.getId());
		Assert.assertEquals(property.getName(), propertySummary.getName());
		Assert.assertEquals(property.getDefinition(), propertySummary.getDescription());
		Assert.assertEquals(property.getDateCreated(), ISO8601DateParser.parse(propertySummary.getMetadata().getDateCreated()));
		Assert.assertEquals(property.getDateLastModified(), ISO8601DateParser.parse(propertySummary.getMetadata().getDateLastModified()));
	}
	
	@Test
	public void scaleSummaryMapperTest() throws ParseException {
		Term term = new Term();

		term.setId(1);
		term.setName("name");
		term.setDefinition("definition");

		Scale scale = new Scale(term);
		scale.setDateCreated(new Date());
		scale.setDateLastModified(new Date());

		ModelMapper mapper = OntologyMapper.getInstance();

		ScaleSummary scaleSummary = mapper.map(scale, ScaleSummary.class);

		Assert.assertEquals(String.valueOf(scale.getId()), scaleSummary.getId());
		Assert.assertEquals(scale.getName(), scaleSummary.getName());
		Assert.assertEquals(scale.getDefinition(), scaleSummary.getDescription());
		Assert.assertEquals(scale.getDateCreated(), ISO8601DateParser.parse(scaleSummary.getMetadata().getDateCreated()));
		Assert.assertEquals(scale.getDateLastModified(), ISO8601DateParser.parse(scaleSummary.getMetadata().getDateLastModified()));
	}
	
	@Test
	public void variableSummaryMapperTest() throws ParseException {

		TermSummary methodSummary = new TermSummary(11, "Method Name", "Method Description");
		TermSummary propertySummary = new TermSummary(10, "Property Name", "Property Description");

		OntologyVariableSummary variable = new OntologyVariableSummary(1, "name", "description");
		variable.setDateCreated(new Date());
		variable.setDateLastModified(new Date());
		variable.setMethodSummary(methodSummary);
		variable.setPropertySummary(propertySummary);

		ModelMapper mapper = OntologyMapper.getInstance();

		VariableSummary variableSummary = mapper.map(variable, VariableSummary.class);

		Assert.assertEquals(String.valueOf(variable.getId()), variableSummary.getId());
		Assert.assertEquals(variable.getName(), variableSummary.getName());
		Assert.assertEquals(variable.getDescription(), variableSummary.getDescription());
		Assert.assertEquals(variable.getDateCreated(), ISO8601DateParser.parse(variableSummary.getMetadata().getDateCreated()));
		Assert.assertEquals(variable.getDateLastModified(), ISO8601DateParser.parse(variableSummary.getMetadata().getDateLastModified()));
	}
}
