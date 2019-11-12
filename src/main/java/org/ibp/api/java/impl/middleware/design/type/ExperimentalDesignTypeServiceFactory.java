package org.ibp.api.java.impl.middleware.design.type;

import org.ibp.api.java.design.type.ExperimentalDesignTypeService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ExperimentalDesignTypeServiceFactory {

	private Map<Integer, ExperimentalDesignTypeService> serviceMap = new HashMap<>();

	public ExperimentalDesignTypeService lookup(final Integer designTypeId) {
			return this.serviceMap.get(designTypeId);
	}

	public void addService(final ExperimentalDesignTypeService service) {
		this.serviceMap.put(service.getDesignTypeId(), service);
	}

}
