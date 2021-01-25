package org.ibp.api.java.impl.middleware.location;

import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.location.validator.LocationSearchRequestValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LocationSearchRequestValidatorTest {

	@Mock
	private ProgramValidator programValidator;

	@InjectMocks
	private LocationSearchRequestValidator validator = new LocationSearchRequestValidator();

	@Test
	public void testValidate_MissingProgramUUIDWhenFavoritesAreRequired() {
		final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
		locationSearchRequest.setFavoritesOnly(true);
		try {
			this.validator.validate("maize", locationSearchRequest);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("locations.favorite.requires.program"));
		}
	}

	@Test
	public void testValidate_InvalidProgram() {
		final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
		locationSearchRequest.setProgramUUID(UUID.randomUUID().toString());
		locationSearchRequest.setFavoritesOnly(false);
		try {
			Mockito.doAnswer(invocation -> {
				Object[] args = invocation.getArguments();
				((BindingResult) args[1]).reject("program.does.not.exist", "");
				return null; // void method, so return null
			}).when(this.programValidator).validate(Mockito.any(), Mockito.any());
			this.validator.validate("maize", locationSearchRequest);
		} catch (Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("program.does.not.exist"));
		}
	}

}
