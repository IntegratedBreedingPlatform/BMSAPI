package org.ibp.api.java.crop;

import java.util.List;

public interface CropService {

	List<String> getInstalledCrops();

	List<String> getAvailableCropsForUser(int workbenchUserId);
}
