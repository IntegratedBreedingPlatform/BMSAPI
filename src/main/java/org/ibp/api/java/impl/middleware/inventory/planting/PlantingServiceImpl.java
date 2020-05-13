package org.ibp.api.java.impl.middleware.inventory.planting;

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

}
