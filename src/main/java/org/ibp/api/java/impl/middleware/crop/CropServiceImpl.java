package org.ibp.api.java.impl.middleware.crop;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.ibp.api.java.crop.CropService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CropServiceImpl implements CropService {

	@Autowired
	@Lazy
	private WorkbenchDataManager workbenchDataManager;

	@Override
	public List<String> getInstalledCrops() {
		final List<String> crops = new ArrayList<>();

		final List<CropType> installedCropDatabses = this.workbenchDataManager.getInstalledCropDatabses();

		for (final CropType cropType : installedCropDatabses) {
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
