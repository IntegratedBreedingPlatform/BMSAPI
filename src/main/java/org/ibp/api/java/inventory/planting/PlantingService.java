package org.ibp.api.java.inventory.planting;

import org.generationcp.middleware.domain.inventory.planting.PlantingMetadata;
import org.generationcp.middleware.domain.inventory.planting.PlantingRequestDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;

import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsSearchDTO;
import org.generationcp.middleware.service.impl.inventory.PlantingPreparationDTO;

public interface PlantingService {

	PlantingPreparationDTO searchPlantingPreparation(final Integer studyId,
		final Integer datasetId, SearchCompositeDto<ObservationUnitsSearchDTO, Integer> searchCompositeDto);

	PlantingMetadata getPlantingMetadata(PlantingRequestDto plantingRequestDto);

	void generatePlanting(PlantingRequestDto plantingRequestDto, TransactionStatus transactionStatus);

}
