package org.ibp.api.java.impl.middleware.rpackage;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.rpackage.RPackageDTO;
import org.ibp.api.domain.rpackage.RCallDTO;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.rpackage.RPackageService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RPackageServiceImplTest {

	public static final int BOUND = 10;

	@Mock
	private org.generationcp.middleware.service.api.rpackage.RPackageService rPackageMiddlewareService;

	@InjectMocks
	private final RPackageService rPackageService = new RPackageServiceImpl();

	private final Random random = new Random();

	@Test
	public void testGetRCallsByPackageId() {

		final int packageId = this.random.nextInt();
		final org.generationcp.middleware.domain.rpackage.RCallDTO rCall = this.createTestRCallDTO();

		when(this.rPackageMiddlewareService.getRPackageById(packageId)).thenReturn(Optional.of(new RPackageDTO()));
		when(this.rPackageMiddlewareService.getRCallsByPackageId(packageId)).thenReturn(Arrays.asList(rCall));

		final List<RCallDTO> result = this.rPackageService.getRCallsByPackageId(packageId);

		final RCallDTO rCallDTO = result.get(0);

		Assert.assertEquals(rCallDTO.getEndpoint(), rCall.getEndpoint());
		Assert.assertEquals(rCallDTO.getDescription(), rCall.getDescription());
		Assert.assertEquals(rCallDTO.getrCallId(), rCall.getrCallId());
		Assert.assertEquals(rCallDTO.isAggregate(), rCall.isAggregate());
		Assert.assertEquals(rCallDTO.getParameters(), rCall.getParameters());

	}

	@Test
	public void testGetRCallsByPackageId_RPackageDoesNotExist() {

		final int packageId = this.random.nextInt();

		when(this.rPackageMiddlewareService.getRPackageById(packageId)).thenReturn(Optional.empty());

		try {
			final List<RCallDTO> result = this.rPackageService.getRCallsByPackageId(packageId);
			Assert.fail("Method should throw an error");
		} catch (final ResourceNotFoundException e) {
			verify(this.rPackageMiddlewareService, times(0)).getRCallsByPackageId(packageId);
		}

	}

	private org.generationcp.middleware.domain.rpackage.RCallDTO createTestRCallDTO() {

		final org.generationcp.middleware.domain.rpackage.RCallDTO rCall = new org.generationcp.middleware.domain.rpackage.RCallDTO();
		rCall.setDescription(RandomStringUtils.randomAlphanumeric(BOUND));
		rCall.setEndpoint(RandomStringUtils.randomAlphanumeric(BOUND));
		final Map<String, String> parameters = new HashMap<>();
		parameters.put(RandomStringUtils.randomAlphanumeric(BOUND), RandomStringUtils.randomAlphanumeric(BOUND));
		rCall.setParameters(parameters);
		rCall.setrCallId(1);
		rCall.setAggregate(true);

		return rCall;

	}

}
