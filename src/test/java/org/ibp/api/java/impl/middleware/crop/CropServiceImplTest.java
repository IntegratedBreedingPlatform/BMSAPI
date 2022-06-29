
package org.ibp.api.java.impl.middleware.crop;

import com.google.common.collect.Lists;
import org.generationcp.middleware.api.crop.CropService;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class CropServiceImplTest {

	@InjectMocks
	private CropServiceImpl cropService;

	@Mock
	private CropService cropServiceMW;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testGetInstalledCrops() {

		List<CropType> cropTypeList = Lists.newArrayList(new CropType("wheat"), new CropType("maize"));
		Mockito.when(this.cropServiceMW.getInstalledCropDatabases()).thenReturn(cropTypeList);

		List<String> installedCrops = this.cropService.getInstalledCrops();
		Assert.assertEquals(cropTypeList.size(), installedCrops.size());
		Assert.assertTrue(installedCrops.contains("maize"));
		Assert.assertTrue(installedCrops.contains("wheat"));
	}

}
