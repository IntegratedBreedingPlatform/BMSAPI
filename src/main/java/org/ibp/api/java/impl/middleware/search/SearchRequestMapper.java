package org.ibp.api.java.impl.middleware.search;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.generationcp.middleware.pojos.search.BrapiSearchRequest;
import org.ibp.api.brapi.v1.search.SearchRequestDto;
import org.ibp.api.exception.ApiRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

/**
 * Created by clarysabel on 2/19/19.
 */
@Component
public class SearchRequestMapper {

	private final ObjectMapper jacksonMapper;

	@Autowired
	private ResourceBundleMessageSource messageSource;

	public SearchRequestMapper() {
		this.jacksonMapper = new ObjectMapper();
		this.jacksonMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
	}

	BrapiSearchRequest map(final SearchRequestDto searchRequestDto) {
		final BrapiSearchRequest brapiSearchRequest = new BrapiSearchRequest();
		//TODO
		//brapiSearchRequest.setRequestType(searchRequestDto.getClass());
		try {
			brapiSearchRequest.setParameters(this.jacksonMapper.writeValueAsString(searchRequestDto));
		} catch (final Exception e) {
			throw new ApiRuntimeException(
				this.messageSource.getMessage("search.request.mapping.internal.error", null, LocaleContextHolder.getLocale()));
		}
		return brapiSearchRequest;
	}

	SearchRequestDto map(final BrapiSearchRequest brapiSearchRequest) {
		final SearchRequestDto searchRequestDto;
		try {
			searchRequestDto = this.jacksonMapper.readValue(brapiSearchRequest.getParameters(), SearchRequestDto.class);
		} catch (final Exception e) {
			throw new ApiRuntimeException(this.messageSource.getMessage("search.request.mapping.internal.error", null, LocaleContextHolder.getLocale()));
		}
		return searchRequestDto;
	}

}
