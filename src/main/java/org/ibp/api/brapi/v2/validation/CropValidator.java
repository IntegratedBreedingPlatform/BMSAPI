package org.ibp.api.brapi.v2.validation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.ibp.api.Util;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CropValidator {

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private SecurityService securityService;

	public BindingResult validateCrop(final String cropName) {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());

		final Integer userId = this.securityService.getCurrentlyLoggedInUser().getUserid();

		if (!StringUtils.isEmpty(cropName)) {
			final List<CropType> cropTypeList = this.workbenchDataManager.getInstalledCropDatabses().stream().filter(cropType -> {
				return cropName.equalsIgnoreCase(cropType.getCropName());
			}).collect(Collectors.toList());

			if (CollectionUtils.isEmpty(cropTypeList)) {
				errors.reject("crop.does.not.exist", new String[] {cropName}, "");
			} else if (!Util.isNullOrEmpty(userId)) {
				final List<CropType> authorizedCrop =
					this.workbenchDataManager.getAvailableCropsForUser(userId).stream().filter(cropType -> {
						return cropName
							.equalsIgnoreCase(cropType.getCropName());
					}).collect(Collectors.toList());

				if (CollectionUtils.isEmpty(authorizedCrop)) {
					errors.reject("crop.user.has.no.access", "");
				}
			}
		}

		return errors;

	}

}
