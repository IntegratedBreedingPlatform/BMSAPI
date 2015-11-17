
package org.ibp.api.security.xauth;

import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Configures x-auth-token security.
 */
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
