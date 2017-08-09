package org.ibp.api.rest.sample;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Api(value = "Sample Services")
@Controller
@RequestMapping("/sample")
public class SampleListResource {

	public static final String NULL = "null";
	public static final String ERROR = "ERROR";
	@Autowired public SampleListService sampleListService;

	@ApiOperation(value = "Create sample list", notes = "Create sample list. ")
	@RequestMapping(value = "/{crop}/sampleList", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> createSampleList(@PathVariable final String crop, @RequestBody SampleListDto dto) {
		dto.setCropName(crop);
		final Map<String, Object> map = this.sampleListService.createSampleList(dto);

		if (map.get(ERROR) != null || NULL.equals(map.get("id"))) {
			return new ResponseEntity<>(map, HttpStatus.CONFLICT);
		}
		return new ResponseEntity<>(map, HttpStatus.CREATED);
	}
}
