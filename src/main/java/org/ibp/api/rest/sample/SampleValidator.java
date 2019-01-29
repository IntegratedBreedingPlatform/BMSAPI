package org.ibp.api.rest.sample;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SampleValidator {

	@Autowired
	private org.generationcp.middleware.service.api.SampleListService sampleListServiceMW;

	private BindingResult errors;

	public void validateSamples(final Integer listId, final List<SampleDTO> samples) {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), SampleDTO.class.getName());

		final Set<String> sampleBusinessKeys = new HashSet<>();

		for (final SampleDTO sampleDTO : samples) {

			if (StringUtils.isBlank(sampleDTO.getSampleBusinessKey())) {
				errors.reject("sample.record.not.include.sample.id.in.file", "");
			}
			if (sampleBusinessKeys.contains(sampleDTO.getSampleBusinessKey())) {
				errors.reject("sample.id.repeat.in.file");
			}
			if (StringUtils.isNotBlank(sampleDTO.getPlateId()) && sampleDTO.getPlateId().length() > 255) {
				errors.reject("sample.plate.id.exceed.length");
			}
			if (StringUtils.isNotBlank(sampleDTO.getWell()) && sampleDTO.getWell().length() > 255) {
				errors.reject("sample.well.exceed.length");
			}
			sampleBusinessKeys.add(sampleDTO.getSampleBusinessKey());
		}

		final long count = this.sampleListServiceMW.countSamplesByUIDs(sampleBusinessKeys, listId);

		if (sampleBusinessKeys.size() != count) {
			errors.reject("sample.sample.ids.not.present.in.file", "");
		}

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}

}
