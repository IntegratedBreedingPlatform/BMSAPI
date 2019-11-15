package org.ibp.api.java.impl.middleware.rpackage;

import com.google.common.base.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.pojos.workbench.RCall;
import org.generationcp.middleware.pojos.workbench.RCallParameter;
import org.generationcp.middleware.pojos.workbench.RPackage;
import org.ibp.api.domain.rpackage.RCallDTO;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.rpackage.RPackageService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
	public void testGetAllRCalls() {

		final RCall rCall = this.createTestRCall();

		when(this.rPackageMiddlewareService.getAllRCalls()).thenReturn(Arrays.asList(rCall));

		final List<RCallDTO> result = this.rPackageService.getAllRCalls();

		final RCallDTO rCallDTO = result.get(0);

		Assert.assertEquals(rCallDTO.getEndpoint(), rCall.getrPackage().getEndpoint());
		Assert.assertEquals(rCallDTO.getDescription(), rCall.getDescription());
		Assert.assertEquals(rCallDTO.getParameters().get(rCall.getrCallParameters().get(0).getKey()),
			rCall.getrCallParameters().get(0).getValue());

	}

	@Test
	public void testGetRCallsByPackageId() {

		final int packageId = this.random.nextInt();
		final RCall rCall = this.createTestRCall();

		when(this.rPackageMiddlewareService.getRPackageById(packageId)).thenReturn(Optional.of(new RPackage()));
		when(this.rPackageMiddlewareService.getRCallsByPackageId(packageId)).thenReturn(Arrays.asList(rCall));

		final List<RCallDTO> result = this.rPackageService.getRCallsByPackageId(packageId);

		final RCallDTO rCallDTO = result.get(0);

		Assert.assertEquals(rCallDTO.getEndpoint(), rCall.getrPackage().getEndpoint());
		Assert.assertEquals(rCallDTO.getDescription(), rCall.getDescription());
		Assert.assertEquals(rCallDTO.getParameters().get(rCall.getrCallParameters().get(0).getKey()),
			rCall.getrCallParameters().get(0).getValue());

	}

	@Test
	public void testGetRCallsByPackageId_RPackageDoesNotExist() {

		final int packageId = this.random.nextInt();

		when(this.rPackageMiddlewareService.getRPackageById(packageId)).thenReturn(Optional.absent());

		try {
			final List<RCallDTO> result = this.rPackageService.getRCallsByPackageId(packageId);
			Assert.fail("Method should throw an error");
		} catch (ResourceNotFoundException e) {
			verify(this.rPackageMiddlewareService, times(0)).getRCallsByPackageId(packageId);
		}

	}

	private RCall createTestRCall() {

		final RCall rCall = new RCall();
		rCall.setId(this.random.nextInt(BOUND));
		rCall.setDescription(RandomStringUtils.randomAlphanumeric(BOUND));
		final List<RCallParameter> rCallParameters = new ArrayList<>();
		rCall.setrCallParameters(Arrays
			.asList(new RCallParameter(this.random.nextInt(BOUND), RandomStringUtils.random(BOUND), RandomStringUtils.random(BOUND))));
		rCall.setrPackage(new RPackage());
		rCall.getrPackage().setEndpoint(RandomStringUtils.random(BOUND));

		return rCall;

	}

}
