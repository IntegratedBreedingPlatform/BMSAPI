package org.ibp.api.java.impl.middleware.ontology.validator;

import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class TermValidatorTest {

	@InjectMocks
	private TermValidator termValidator;

	@Mock
	private TermDataManager termDataManager;

	@Mock
	private BindingResult errors;

	@Test
	public void testValidateTermIds() {
		Mockito.when(this.termDataManager.getTermByIds(Mockito.anyList())).thenReturn(new ArrayList<>());
		this.termValidator.validateTermIds(Arrays.asList(1, 2, 3), this.errors);
		Mockito.verify(this.errors).reject("variable.does.not.exist", new Object[] {1}, "");
		Mockito.verify(this.errors).reject("variable.does.not.exist", new Object[] {2}, "");
		Mockito.verify(this.errors).reject("variable.does.not.exist", new Object[] {3}, "");
	}

	@Test
	public void testValidateTermIds_OK() {
		Mockito.when(this.termDataManager.getTermByIds(Mockito.anyList()))
			.thenReturn(Arrays.asList(new Term(1, "", ""), new Term(2, "", ""), new Term(3, "", "")));
		this.termValidator.validateTermIds(Arrays.asList(1, 2, 3), this.errors);
		Mockito.verifyNoInteractions(this.errors);
	}
}
