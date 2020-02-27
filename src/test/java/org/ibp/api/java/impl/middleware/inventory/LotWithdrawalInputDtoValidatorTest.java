package org.ibp.api.java.impl.middleware.inventory;

import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotWithdrawalInputDtoValidator;
import org.ibp.api.java.ontology.VariableService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by clarysabel on 2/27/20.
 */
@RunWith(MockitoJUnitRunner.class)
public class LotWithdrawalInputDtoValidatorTest {

	@Mock
	private VariableService variableService;

	@InjectMocks
	private LotWithdrawalInputDtoValidator lotWithdrawalInputDtoValidator;

	@Test
	public void testValidateLotWithdrawalInputDtoValidatorIsNull() {
		try {
			this.lotWithdrawalInputDtoValidator.validate(null, null);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lot.withdrawal.input.null"));

		}
	}

}
