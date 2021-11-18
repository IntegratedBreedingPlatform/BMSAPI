package org.ibp.api.java.impl.middleware.sample.brapi;

import com.google.common.collect.Lists;
import org.generationcp.middleware.api.brapi.SampleServiceBrapi;
import org.generationcp.middleware.domain.search_request.brapi.v2.SampleSearchRequestDTO;
import org.generationcp.middleware.service.api.sample.SampleObservationDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SampleServiceBrapiImplTest {

	@Mock
	private SampleServiceBrapi middlewareSampleServiceBrapi;

	@InjectMocks
	private SampleServiceBrapiImpl sampleService;

	@Test
	public void testGetSampleObservations() {
		final SampleSearchRequestDTO requestDTO = new SampleSearchRequestDTO();
		Mockito.when(this.middlewareSampleServiceBrapi.getSampleObservations(requestDTO, null))
				.thenReturn(Lists.newArrayList(new SampleObservationDto()));

		this.sampleService.getSampleObservations(requestDTO, null);
		Mockito.verify(this.middlewareSampleServiceBrapi).getSampleObservations(requestDTO, null);
	}

	@Test
	public void testCountSampleObservations() {
		final SampleSearchRequestDTO requestDTO = new SampleSearchRequestDTO();
		Mockito.when(this.middlewareSampleServiceBrapi.countSampleObservations(requestDTO))
				.thenReturn(new Long(0));
		this.sampleService.countSampleObservations(requestDTO);
		Mockito.verify(this.middlewareSampleServiceBrapi).countSampleObservations(requestDTO);
	}

}
