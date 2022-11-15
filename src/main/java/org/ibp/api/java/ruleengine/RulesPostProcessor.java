
package org.ibp.api.java.ruleengine;

import org.generationcp.middleware.ruleengine.Rule;
import org.generationcp.middleware.ruleengine.RuleFactory;
import org.generationcp.middleware.ruleengine.coding.expression.BaseCodingExpression;
import org.generationcp.middleware.ruleengine.coding.expression.CodingExpressionFactory;
import org.generationcp.middleware.ruleengine.naming.expression.Expression;
import org.generationcp.middleware.ruleengine.naming.impl.ProcessCodeFactory;
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
	"org.generationcp.middleware.ruleengine.coding.expression",
	"org.generationcp.middleware.ruleengine.coding",
	"org.generationcp.middleware.ruleengine.stockid",
	"org.generationcp.middleware.ruleengine.naming"
})
public class RulesPostProcessor implements BeanPostProcessor {

	@Autowired
	private RuleFactory ruleFactory;

	@Autowired
	private ProcessCodeFactory processCodeFactory;

	@Autowired
	private CodingExpressionFactory codingExpressionFactory;

	@Override
	public Object postProcessBeforeInitialization(final Object o, final String s) throws BeansException {
		// do nothing
		return o;
	}

	@Override
	public Object postProcessAfterInitialization(final Object o, final String s) throws BeansException {

		if (o instanceof Rule) {
			final Rule rule = (Rule) o;
			this.ruleFactory.addRule(rule);
		}
		if (o instanceof Expression) {
			this.processCodeFactory.addExpression((Expression) o);
		}
		if (o instanceof BaseCodingExpression) {
			this.codingExpressionFactory.addExpression((org.generationcp.middleware.ruleengine.Expression) o);
		}

		return o;
	}
}
