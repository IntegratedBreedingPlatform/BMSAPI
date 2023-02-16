package org.ibp.api.rest.audit;

import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.audit.ObservationAuditService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Random;

public class ObservationAuditResourceTest extends ApiUnitTestBase {

	@Autowired
	private ObservationAuditService observationAuditService;


	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public ObservationAuditService studyObservationAuditService() {
			return Mockito.mock(ObservationAuditService.class);
		}
	}

	@Before
	public void setup() throws Exception {
		super.setUp();
	}

	@Test
	public void testGetPhenotypeAuditList() throws Exception {
		final Random random = new Random();
		final String observationUnitId = random.nextInt(10000) + "";
		final int variableId = random.nextInt(10000);

		final Pageable pageable = new PageRequest(0, 20);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.get(
					"/crops/{crop}/observationUnits/{observationUnitId}/variable/{variableId}/changes",
					this.cropName, observationUnitId, variableId, pageable)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(this.observationAuditService).getObservationAuditList(observationUnitId, variableId, pageable);
		Mockito.verify(this.observationAuditService).countObservationAudit(observationUnitId, variableId);
	}

}
