package org.ibp.api.java.impl.middleware.crop;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.ibp.api.java.crop.CropService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CropServiceImpl implements CropService {

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Override
	public List<String> getInstalledCrops() {
		List<String> crops = new ArrayList<>();

		List<CropType> installedCropDatabses = this.workbenchDataManager.getInstalledCropDatabses();

		for (CropType cropType : installedCropDatabses) {
			crops.add(cropType.getCropName());
		}

		return crops;
	}

	@Override
	public List<String> getAvailableCropsForUser(final int workbenchUserId) {
		final List<String> crops = new ArrayList<>();

		final List<CropType> availableCropsForUser = this.workbenchDataManager.getAvailableCropsForUser(workbenchUserId);

		for (final CropType cropType : availableCropsForUser) {
			crops.add(cropType.getCropName());
		}

		return crops;
	}

	void setWorkbenchDataManager(final WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

}
