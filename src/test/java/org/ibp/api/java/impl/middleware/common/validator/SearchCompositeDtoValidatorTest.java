package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.hamcrest.CoreMatchers;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.HashMap;

public class SearchCompositeDtoValidatorTest {

	private SearchCompositeDtoValidator searchCompositeDtoValidator;

	@Before
	public void beforeEachTest() {
		this.searchCompositeDtoValidator = new SearchCompositeDtoValidator();
	}

	@Test
	public void test_validateSearchCompositeDto_throwsException() {
		try {
			final SearchCompositeDto<Integer, Integer> searchCompositeDto = new SearchCompositeDto<>();
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Program");
			this.searchCompositeDtoValidator.validateSearchCompositeDto(searchCompositeDto, errors);
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), CoreMatchers.hasItem("search.composite.invalid"));
		}
	}
}
