package org.ibp.api.rest.sample;

import junit.framework.Assert;
import org.generationcp.middleware.pojos.SampleList;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class SampleListServiceImplTest {

	public static final String PROGRAM_UUID = "23487325-dwfkjfsfdsaf-32874829374";

	@Mock
	private org.generationcp.middleware.service.api.SampleListService sampleListServiceMW;

	@Mock
	private SecurityService securityService;

	private SampleListServiceImpl sampleListService;

	@Before
	public void init() {

		this.sampleListService = new SampleListServiceImpl();
		this.sampleListService.setSampleListServiceMW(sampleListServiceMW);

	}

	@Test
	public void testMoveSampleListFolder() {

		final int folderId = 1;
		final int newParentId = 2;
		final boolean isCropList = false;

		final SampleList sampleList = new SampleList();
		sampleList.setHierarchy(new SampleList());
		sampleList.getHierarchy().setId(newParentId);
		Mockito.when(this.sampleListServiceMW.moveSampleList(folderId, newParentId, isCropList, PROGRAM_UUID)).thenReturn(sampleList);

		final Map<String, Object> result = this.sampleListService.moveSampleListFolder(folderId, newParentId, isCropList, PROGRAM_UUID);

		Assert.assertEquals(String.valueOf(newParentId), result.get(SampleListServiceImpl.PARENT_ID));
	}

}
