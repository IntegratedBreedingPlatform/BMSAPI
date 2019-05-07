package org.ibp.api.brapi.v1.search;

import com.wordnik.swagger.annotations.Api;
import org.ibp.api.java.search.BrapiSearchRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Api(value = "BrAPI Search Services")
@Controller
public class SearchResourceBrapi {

	@Autowired
	private BrapiSearchRequestService germplasmService;


}
