package org.ibp.api.java.design.type;

import org.ibp.api.java.impl.middleware.design.type.ExperimentalDesignTypeServiceFactory;
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
public class ExperimentalDesignTypeServicePostProcessor implements BeanPostProcessor {

	@Autowired
	private ExperimentalDesignTypeServiceFactory serviceFactory;

	@Override
	public Object postProcessBeforeInitialization(final Object o, final String s) throws BeansException {
		return o;
	}

	@Override
	public Object postProcessAfterInitialization(final Object o, final String s) throws BeansException {
		if (o instanceof ExperimentalDesignTypeService) {
			serviceFactory.addService((ExperimentalDesignTypeService)o);
		}
		return o;
	}
}
