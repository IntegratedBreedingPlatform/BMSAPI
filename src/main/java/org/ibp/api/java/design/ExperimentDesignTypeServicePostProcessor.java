package org.ibp.api.java.design;

import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.java.impl.middleware.design.type.ExperimentDesignTypeServiceFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

/**
 * Spring utility class that allows Rule objects to be automatically registered into the RuleFactory
 * object
 */
@Component
@ComponentScan(basePackages = {
	"org.ibp.api.java.impl.middleware.design.type"})
public class ExperimentDesignTypeServicePostProcessor implements BeanPostProcessor {

	@Autowired
	private ExperimentDesignTypeServiceFactory serviceFactory;

	@Override
	public Object postProcessBeforeInitialization(final Object o, final String s) throws BeansException {
		return o;
	}

	@Override
	public Object postProcessAfterInitialization(final Object o, final String s) throws BeansException {
		if (o instanceof ExperimentDesignTypeService) {
			serviceFactory.addService((ExperimentDesignTypeService)o);
		}
		return o;
	}
}
