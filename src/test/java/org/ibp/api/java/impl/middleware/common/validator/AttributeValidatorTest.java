
package org.ibp.api.java.impl.middleware.common.validator;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;

public class AttributeValidatorTest {

	@Mock
	private GermplasmDataManager germplasmDataManager;

	private AttributeValidator attributeValidator;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		this.attributeValidator = new AttributeValidator();
		this.attributeValidator.setGermplasmDataManager(this.germplasmDataManager);
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	@Test
	public void testForInvalidAttributeId() throws MiddlewareQueryException {

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Program");
		final Integer attributeById = Integer.valueOf(RandomStringUtils.randomNumeric(1));

		Mockito.doReturn(null).when(this.germplasmDataManager).getAttributeById(attributeById);

		this.attributeValidator.validateAttributeIds(bindingResult, Lists.newArrayList(String.valueOf(attributeById)));
		Assert.assertTrue(bindingResult.hasErrors());
	}

	@Test
	public void testForValidProgramId() throws MiddlewareQueryException {

		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Program");
		final Integer attributeById = Integer.valueOf(RandomStringUtils.randomNumeric(1));

		final Attribute attribute = new Attribute();
		attribute.setAid(attributeById);
		List<Attribute> attributeLists = Lists.newArrayList(attribute);
		Mockito.doReturn(attributeLists).when(this.germplasmDataManager).getAttributeByIds(Lists.newArrayList(attributeById));

		this.attributeValidator.validateAttributeIds(bindingResult, Lists.newArrayList(String.valueOf(attributeById)));

		Assert.assertFalse(bindingResult.hasErrors());
	}
}
