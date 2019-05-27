package org.ibp.api.java.impl.middleware.call;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.IOUtils;
import org.ibp.api.java.calls.CallService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CallServiceImpl implements CallService {

	private static final Logger LOG = LoggerFactory.getLogger(CallServiceImpl.class);

	@Override
	public List<Map<String, Object>> getAllCalls(final String dataType, final Integer pageSize, final Integer pageNumber) {
		try {
			List<Map<String, Object>> brapiCalls;
			final String jsonPath;
			// Look for the dataType parameter in "datatypes" (BrAPI 1.2) and "dataTypes" (BrAPI 1.3) sections
			if (dataType != null) {
				jsonPath = "$.data[?('" + dataType + "' in @['datatypes'] || '" + dataType + "' in @['dataTypes'])]";
			} else {
				jsonPath = "$.data.*";
			}

			final InputStream is = CallServiceImpl.class.getClassLoader().getResourceAsStream("brapi/calls.json");
			final String jsonTxt = IOUtils.toString( is );
			brapiCalls = JsonPath.parse(jsonTxt).read(jsonPath);

			if (pageNumber != null && pageSize != null) {

				int toIndex = pageSize * (pageNumber) + pageSize;
				if (toIndex > brapiCalls.size()) {
					toIndex = brapiCalls.size();
				}

				int fromIndex = pageNumber * pageSize;
				if (fromIndex < 0) {
					fromIndex = 0;
				}

				brapiCalls = brapiCalls.subList(fromIndex, toIndex);
			}

			return brapiCalls;
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
			return new ArrayList<>();
		}
	}
}
