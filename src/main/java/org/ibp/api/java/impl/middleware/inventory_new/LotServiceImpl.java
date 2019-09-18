package org.ibp.api.java.impl.middleware.inventory_new;

import org.ibp.api.java.inventory_new.LotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LotServiceImpl implements LotService {

	@Autowired
	private org.generationcp.middleware.service.api.inventory.LotService lotService;

}
