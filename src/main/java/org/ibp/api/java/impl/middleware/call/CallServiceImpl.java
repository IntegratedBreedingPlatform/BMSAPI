package org.ibp.api.java.impl.middleware.call;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.IOUtils;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.ibp.api.java.calls.CallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class CallServiceImpl implements CallService {

	@Override
	public List<Map<String, Object>> getAllCalls(final String dataType, final Integer pageSize, final Integer pageNumber) {
		try {
			List<Map<String, Object>> brapiCalls;
			final String jsonPath;
			if (dataType != null) {
				jsonPath = "$.data[?('" + dataType + "' in @['datatypes'])]";
			} else {
				jsonPath = "$.data.*";
			}

			final InputStream is = CallServiceImpl.class.getClassLoader().getResourceAsStream("brapi/calls.json");
			final String jsonTxt = IOUtils.toString( is );
			brapiCalls = JsonPath.parse(jsonTxt).read(jsonPath);

			if (pageNumber != null && pageSize != null) {

				int maxLenght = pageSize * (pageNumber) + pageSize;
				if (maxLenght > brapiCalls.size()) {
					maxLenght = brapiCalls.size();
				}

				int minLenght = pageNumber * pageSize;
				if (minLenght < 0) {
					minLenght = 0;
				}

				brapiCalls = brapiCalls.subList(minLenght, maxLenght);
			}

			return brapiCalls;
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
