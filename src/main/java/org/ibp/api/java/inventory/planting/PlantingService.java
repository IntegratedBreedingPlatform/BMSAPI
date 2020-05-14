package org.ibp.api.java.inventory.planting;

import org.generationcp.middleware.domain.inventory.planting.PlantingMetadata;
import org.generationcp.middleware.domain.inventory.planting.PlantingRequestDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;

public interface PlantingService {

	PlantingMetadata getPlantingMetadata(PlantingRequestDto plantingRequestDto);

	void generatePlanting(PlantingRequestDto plantingRequestDto, TransactionStatus transactionStatus);

}
