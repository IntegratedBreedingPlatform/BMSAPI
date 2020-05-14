package org.ibp.api.java.impl.middleware.inventory.planting;

import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.planting.PlantingMetadata;
import org.generationcp.middleware.domain.inventory.planting.PlantingRequestDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsSearchDTO;
import org.generationcp.middleware.service.impl.inventory.PlantingPreparationDTO;
import org.ibp.api.java.impl.middleware.inventory.common.InventoryLock;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.inventory.planting.PlantingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlantingServiceImpl implements PlantingService {

	@Autowired
	private InventoryLock inventoryLock;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private InventoryCommonValidator inventoryCommonValidator;

	@Autowired
	private org.generationcp.middleware.service.api.inventory.PlantingService plantingService;

	@Override
	public PlantingPreparationDTO searchPlantingPreparation(
		final Integer studyId,
		final Integer datasetId,
		final SearchCompositeDto<ObservationUnitsSearchDTO, Integer> searchCompositeDto) {

		return this.plantingService.searchPlantingPreparation(studyId, datasetId, searchCompositeDto);
	}

	@Override
	public PlantingMetadata getPlantingMetadata(final PlantingRequestDto plantingRequestDto) {
		return null;
	}

	@Override
	public void generatePlanting(final PlantingRequestDto plantingRequestDto, final TransactionStatus transactionStatus) {

	}

}
