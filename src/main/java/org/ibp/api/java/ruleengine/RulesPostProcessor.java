
package org.ibp.api.java.ruleengine;

import org.generationcp.middleware.ruleengine.Rule;
import org.generationcp.middleware.ruleengine.RuleFactory;
import org.generationcp.middleware.ruleengine.coding.expression.BaseCodingExpression;
import org.generationcp.middleware.ruleengine.coding.expression.CodingExpressionFactory;
import org.generationcp.middleware.ruleengine.naming.deprecated.expression.DeprecatedExpression;
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
	public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
		// do nothing
		return o;
	}

	@Override
	public Object postProcessAfterInitialization(Object o, String s) throws BeansException {

		if (o instanceof Rule) {
			Rule rule = (Rule) o;
			this.ruleFactory.addRule(rule);
		}
		if (o instanceof DeprecatedExpression) {
			this.processCodeFactory.addExpression((DeprecatedExpression) o);
		}
		if (o instanceof BaseCodingExpression) {
			this.codingExpressionFactory.addExpression((org.generationcp.middleware.ruleengine.Expression) o);
		}

		return o;
	}
}
