package org.ibp.api.rest.samplesubmission.service.mapper;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

@Component
public class GOBiiSampleMapper {

	private ObjectMapper jacksonMapper;

	@Autowired
	private ResourceBundleMessageSource messageSource;

	public GOBiiSampleMapper() {
		jacksonMapper = new ObjectMapper();
	}

}
