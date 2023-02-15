package org.ibp.api.java.impl.middleware.audit;

import org.generationcp.middleware.service.api.audit.ObservationAuditService;
import org.generationcp.middleware.service.api.dataset.ObservationAuditDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ObservationAuditServiceImplTest {

	@Mock
	private ObservationAuditService mwObservationAuditService;

	@InjectMocks
	private ObservationAuditServiceImpl observationAuditService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetPhenotypeAuditList() {
		final List<ObservationAuditDTO> auditList = new ArrayList<>();

		final Random random = new Random();
		final int variableId = random.nextInt();
		final String obsUnitId = random.nextInt() + "";
		final Pageable pageable = new PageRequest(0, 1000);

		Mockito.doReturn(auditList).when(this.mwObservationAuditService)
			.getObservationAuditList(ArgumentMatchers.eq(obsUnitId), ArgumentMatchers.eq(variableId),
				ArgumentMatchers.eq(pageable));
		final List<ObservationAuditDTO> result =
			this.observationAuditService.getObservationAuditList(obsUnitId, variableId, pageable);

		Assert.assertSame(auditList, result);

	}
}
