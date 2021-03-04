package org.ibp.api.java.impl.middleware.call;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.commons.io.IOUtils;
import org.ibp.api.java.calls.CallService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CallServiceImpl implements CallService {

	private static final Logger LOG = LoggerFactory.getLogger(CallServiceImpl.class);

	@Value("classpath:brapi/calls.json")
	private Resource calls;

	@Override
	public List<Map<String, Object>> getAllCalls(final String dataType, final String version, final Integer pageSize, final Integer pageNumber) {
		try {
			List<Map<String, Object>> brapiCalls;
			final String jsonPath;
			// Look for the dataType parameter in "datatypes" (BrAPI 1.2) and "dataTypes" (BrAPI 1.3) sections
			if (dataType != null) {
				jsonPath = "$.data[?('" + dataType + "' in @['datatypes'] || '" + dataType + "' in @['dataTypes'])]";
			} else {
				jsonPath = "$.data.*";
			}

			final InputStream is = this.calls.getInputStream();
			final String jsonTxt = IOUtils.toString( is );
			brapiCalls = JsonPath.parse(jsonTxt).read(jsonPath);

			if (pageNumber != null || pageSize != null) {

				final int pNumber = pageNumber == null ? 0 : pageNumber;
				final int pSize = pageSize == null ? brapiCalls.size() : pageSize;
				int toIndex = pSize * (pNumber) + pSize;
				if (toIndex > brapiCalls.size()) {
					toIndex = brapiCalls.size();
				}


				int fromIndex = pNumber * pSize;
				if (fromIndex < 0) {
					fromIndex = 0;
				}

				brapiCalls = brapiCalls.subList(fromIndex, toIndex);
			}

			return this.filterCallsByVersion(brapiCalls, version);
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	List<Map<String, Object>> filterCallsByVersion(final List<Map<String, Object>> brapiCalls, final String version) {
		final List<Map<String, Object>> filteredBrapiCalls = new ArrayList<>();
		for(final Map<String, Object> call: brapiCalls) {
			final JSONArray versions = ((JSONArray) call.get("versions"));
			final ListIterator<Object> iterator = versions.listIterator();
			boolean implementedInTargetVersion = false;
			while (iterator.hasNext()) {
				if(!iterator.next().toString().startsWith(version)) {
					iterator.remove();
				} else {
					implementedInTargetVersion = true;
				}
			}
			if(implementedInTargetVersion) {
				filteredBrapiCalls.add(call);
			}
		}
		return filteredBrapiCalls;
	}
}
