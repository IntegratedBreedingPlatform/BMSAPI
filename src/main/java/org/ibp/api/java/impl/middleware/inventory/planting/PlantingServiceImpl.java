package org.ibp.api.java.impl.middleware.inventory.planting;

import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.planting.PlantingMetadata;
import org.generationcp.middleware.domain.inventory.planting.PlantingRequestDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsSearchDTO;
import org.generationcp.middleware.service.impl.inventory.PlantingPreparationDTO;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.java.impl.middleware.inventory.common.InventoryLock;
import org.ibp.api.java.impl.middleware.inventory.planting.validator.PlantingRequestDtoValidator;
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
	private StudyValidator studyValidator;

	@Autowired
	private DatasetValidator datasetValidator;

	@Autowired
	private PlantingRequestDtoValidator plantingRequestDtoValidator;

	@Autowired
	private org.generationcp.middleware.service.api.inventory.PlantingService plantingService;

	@Override
	public PlantingPreparationDTO searchPlantingPreparation(
		final Integer studyId,
		final Integer datasetId,
		final SearchCompositeDto<ObservationUnitsSearchDTO, Integer> searchCompositeDto) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.datasetValidator.validatePlotDatasetType(datasetId);
		return this.plantingService.searchPlantingPreparation(studyId, datasetId, searchCompositeDto);
	}

	@Override
	public PlantingMetadata getPlantingMetadata(final Integer studyId, final Integer datasetId,
		final PlantingRequestDto plantingRequestDto) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.datasetValidator.validatePlotDatasetType(datasetId);
		this.plantingRequestDtoValidator.validatePlantingRequestDto(studyId, datasetId, plantingRequestDto);
		return plantingService.getPlantingMetadata(studyId, datasetId, plantingRequestDto);
	}

	@Override
	public void generatePlanting(final Integer studyId, final Integer datasetId,
		final PlantingRequestDto plantingRequestDto, final TransactionStatus transactionStatus) {
		try {
			inventoryLock.lockWrite();
			this.studyValidator.validate(studyId, false);
			this.datasetValidator.validateDataset(studyId, datasetId);
			this.datasetValidator.validatePlotDatasetType(datasetId);
			this.plantingRequestDtoValidator.validatePlantingRequestDto(studyId, datasetId, plantingRequestDto);
			final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
			plantingService.savePlanting(loggedInUser.getUserid(), studyId, datasetId, plantingRequestDto, transactionStatus);
		} finally {
			inventoryLock.unlockWrite();
		}
	}

}
