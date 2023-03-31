
package org.ibp.api.rest.crop;

import io.swagger.annotations.ApiOperation;
import net.minidev.json.JSONUtil;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.ibp.api.java.crop.CropService;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/crop")
public class CropResource {

	@Autowired
	private CropService cropService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private HttpServletRequest request;

	@ApiOperation(value = "List all available crops for the current user",
		notes = "List all installed crops that are available for the current user."
			+ " The results are typically used to supply the *cropname* path parameter in other BMSAPI calls.")
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public ResponseEntity<List<String>> listAvailableCrops() {

		if (request.isUserInRole(PermissionsEnum.ADMIN.name())
			|| request.isUserInRole(PermissionsEnum.ADMINISTRATION.name())
			|| request.isUserInRole(PermissionsEnum.SITE_ADMIN.name())) {
			return new ResponseEntity<>(
				this.cropService.getInstalledCrops(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
				this.cropService.getAvailableCropsForUser(this.securityService.getCurrentlyLoggedInUser().getUserid()), HttpStatus.OK);
		}
	}
}
