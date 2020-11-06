package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public class EntryTypeValidatorTest {

	@Mock
	private OntologyDataManager ontologyDataManager;

	private EntryTypeValidator entryTypeValidator;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		this.entryTypeValidator = new EntryTypeValidator();
		this.entryTypeValidator.setOntologyDataManager(this.ontologyDataManager);
		ContextHolder.setCurrentProgram(RandomStringUtils.randomAlphabetic(10));
	}

	@Test
	public void testValidateEntryType() {
		final StandardVariable standardVariable = new StandardVariable();
		final List<Enumeration> enumerations = new ArrayList<>();
		enumerations.add(new Enumeration(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId(),
			SystemDefinedEntryType.TEST_ENTRY.getEntryTypeName(),SystemDefinedEntryType.TEST_ENTRY.getEntryTypeValue(), 1));
		enumerations.add(new Enumeration(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId(),
			SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeName(),SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeValue(), 2));
		standardVariable.setEnumerations(enumerations);
		Mockito.when(this.ontologyDataManager.getStandardVariable(ArgumentMatchers.eq(TermId.ENTRY_TYPE.getId()),
			ArgumentMatchers.anyString())).thenReturn(standardVariable);

		try {
			this.entryTypeValidator.validateEntryType(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());
		} catch (Exception e) {
			Assert.fail("should not throw a validation error.");
		}
		try {
			this.entryTypeValidator.validateEntryType(SystemDefinedEntryType.DISEASE_CHECK.getEntryTypeCategoricalId());
			Assert.fail("should throw a validation error.");
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("entry.type.does.not.exist"));
		}
	}
}
