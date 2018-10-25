package org.ibp.api.rest.dataset;

import com.wordnik.swagger.annotations.Api;
import org.ibp.api.java.impl.middleware.dataset.DatasetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Api(value = "Dataset Services")
@Controller
public class DatasetResource {

	private static final Logger LOG = LoggerFactory.getLogger(DatasetResource.class);

	@Autowired
	private DatasetService datasetService;

}
