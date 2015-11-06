
package org.ibp.api.java.impl.middleware.crop;

import java.util.List;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

public class CropServiceImplTest {

	@Test
	public void testGetInstalledCrops() {

		CropServiceImpl cropService = new CropServiceImpl();
		WorkbenchDataManager workbenchDataManager = Mockito.mock(WorkbenchDataManager.class);
		List<CropType> cropTypeList = Lists.newArrayList(new CropType("wheat"), new CropType("maize"));
		Mockito.when(workbenchDataManager.getInstalledCropDatabses()).thenReturn(cropTypeList);
		cropService.setWorkbenchDataManager(workbenchDataManager);

		List<String> installedCrops = cropService.getInstalledCrops();
		Assert.assertEquals(cropTypeList.size(), installedCrops.size());
		Assert.assertTrue(installedCrops.contains("maize"));
		Assert.assertTrue(installedCrops.contains("wheat"));
	}
}
