package org.ibp.api.java.impl.middleware.sample;

import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.service.api.SampleService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SampleServiceImplTest {

	@Mock
	private SampleService middlewareSampleService;

	@Mock
	private GermplasmValidator germplasmValidator;

	@InjectMocks
	private SampleServiceImpl sampleService;

	@Test
	public void testGetGermplasmSamples_ThrowsException_WhenGIDIsInvalid() {
		final Integer gid = 999;
		try {
			Mockito.doThrow(new ApiRequestValidationException(Collections.EMPTY_LIST)).when(this.germplasmValidator)
				.validateGids(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(Collections.singletonList(gid)));
			this.sampleService.getGermplasmSamples(gid);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
				ArgumentMatchers.eq(Collections.singletonList(gid)));
			Mockito.verify(this.middlewareSampleService, Mockito.never()).getByGid(gid);
		}
	}

	@Test
	public void testGetGermplasmSamples_WithNoErrors() {
		final Integer gid = 1;
		final SampleDTO dto = new SampleDTO();
		Mockito.when(this.middlewareSampleService.getByGid(gid)).thenReturn(Collections.singletonList(dto));

		final List<SampleDTO> sampleDTOS = this.sampleService.getGermplasmSamples(gid);

		Assert.assertEquals(dto, sampleDTOS.get(0));
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(Collections.singletonList(gid)));
		Mockito.verify(this.middlewareSampleService).getByGid(gid);
	}

}
