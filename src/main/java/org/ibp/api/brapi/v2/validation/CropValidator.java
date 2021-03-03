package org.ibp.api.brapi.v2.validation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.crop.CropService;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.ObjectError;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CropValidator {

	@Autowired
	private CropService cropService;

	@Autowired
	private SecurityService securityService;

	public void validateCrop(final String cropName) {

		final Integer userId = this.securityService.getCurrentlyLoggedInUser().getUserid();

		if (!StringUtils.isEmpty(cropName)) {
			final List<String> cropTypeList = this.cropService.getInstalledCrops().stream().filter(cropType -> {
				return cropName.equalsIgnoreCase(cropType);
			}).collect(Collectors.toList());

			if (CollectionUtils.isEmpty(cropTypeList)) {
				throw new ApiRequestValidationException(
					Arrays.asList(new ObjectError("", new String[] {"crop.does.not.exist"}, new String[] {cropName}, "")));
			}

			if (!Util.isNullOrEmpty(userId)) {
				final List<String> authorizedCrop =
					this.cropService.getAvailableCropsForUser(userId).stream().filter(cropType -> {
						return cropName
							.equalsIgnoreCase(cropType);
					}).collect(Collectors.toList());

				if (CollectionUtils.isEmpty(authorizedCrop)) {
					throw new ApiRequestValidationException(
						Arrays.asList(new ObjectError("", new String[] {"crop.user.has.no.access"}, null, "")));
				}
			}
		}

	}

}
