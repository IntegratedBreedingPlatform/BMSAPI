package org.ibp.api.java.impl.middleware.design.type;

import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ExperimentDesignTypeServiceFactory {

	private Map<Integer, ExperimentDesignTypeService> serviceMap = new HashMap<>();

	public ExperimentDesignTypeService lookup(final Integer designTypeId) {
			return this.serviceMap.get(designTypeId);
	}

	public void addService(final ExperimentDesignTypeService service) {
		this.serviceMap.put(service.getDesignTypeId(), service);
	}

}
