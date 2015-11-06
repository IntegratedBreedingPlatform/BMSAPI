
package org.ibp.api.rest.crop;

import java.util.List;

import org.ibp.api.java.crop.CropService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/crop")
public class CropResource {

	@Autowired
	private CropService cropService;

	@ApiOperation(value = "List all crops",
			notes = "List all crops for which crop databases are installed in this deployment instance of BMSAPI. "
					+ " The results are typically used to supply the *cropname* path parameter in other BMSAPI calls.")
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public ResponseEntity<List<String>> listAvailableCrops() {
		return new ResponseEntity<List<String>>(this.cropService.getInstalledCrops(), HttpStatus.OK);
	}
}
