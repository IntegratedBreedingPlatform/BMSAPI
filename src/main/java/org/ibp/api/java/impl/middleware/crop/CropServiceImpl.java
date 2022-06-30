package org.ibp.api.java.impl.middleware.crop;

import org.generationcp.middleware.pojos.workbench.CropType;
import org.ibp.api.java.crop.CropService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CropServiceImpl implements CropService {

	@Autowired
	@Lazy
	private org.generationcp.middleware.api.crop.CropService cropServiceMW;

	@Override
	public List<String> getInstalledCrops() {
		return this.cropServiceMW.getInstalledCropDatabases().stream().map(CropType::getCropName).collect(Collectors.toList());
	}

	@Override
	public List<String> getAvailableCropsForUser(final int workbenchUserId) {
		return this.cropServiceMW.getAvailableCropsForUser(workbenchUserId).stream().map(CropType::getCropName).collect(Collectors.toList());
	}

}
