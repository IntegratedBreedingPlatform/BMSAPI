package org.ibp.api.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.SpringDataWebConfiguration;

@Configuration
@EnableConfigurationProperties
public class PaginationConfiguration extends SpringDataWebConfiguration {

	@Bean
	public PageableHandlerMethodArgumentResolver pageableResolver() {
		final PageableHandlerMethodArgumentResolver pageableHandlerMethodArgumentResolver =
			new PageableHandlerMethodArgumentResolver(this.sortResolver());

		pageableHandlerMethodArgumentResolver.setMaxPageSize(Integer.MAX_VALUE);

		return pageableHandlerMethodArgumentResolver;
	}

}
