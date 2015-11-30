
package org.ibp.api.security.xauth;

import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Configures x-auth-token security.
 */
// **Important note for developers** : This class is central to the authentication framework of BMSAPI. Please do not alter it without a
// good understanding of Spring Security in general and BMS X-Auth-Token based authentication workflow in particular, otherwise there will
// be MAJOR breakages in the functioning of BMS components. Consult your friendly senior developer first if you are unsure.
@Configuration
public class XAuthConfiguration implements EnvironmentAware {

	private RelaxedPropertyResolver propertyResolver;

	@Override
	public void setEnvironment(final Environment environment) {
		this.propertyResolver = new RelaxedPropertyResolver(environment, "authentication.xauth.");
	}

	@Bean
	public TokenProvider tokenProvider() {
		final String secret = this.propertyResolver.getProperty("secret", String.class, "bmsXAuthSecret");
		final int validityInSeconds = this.propertyResolver.getProperty("tokenValidityInSeconds", Integer.class, 3600);
		return new TokenProvider(secret, validityInSeconds);
	}
}
