package org.ibp.api.java.impl.middleware.cop;

import org.ibp.api.java.cop.CalculateCOPService;
import org.ibp.api.rest.cop.COPPermissions;
import org.ibp.api.rest.cop.COPPermissionsResponce;
import org.ibp.api.rest.cop.COPExportStudy;
import org.ibp.api.rest.cop.COPExportStudyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Service
public class CalculateCOPServiceImpl implements CalculateCOPService {

	private static final Logger LOG = LoggerFactory.getLogger(CalculateCOPService.class);

	@Autowired
	private HttpServletRequest request;

	private RestTemplate restTemplate;

	public CalculateCOPServiceImpl(){
		this.restTemplate = new RestTemplate();
		this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

	}

	@Override
	public COPExportStudyResponse exportStudy(final COPExportStudy COPExportStudy) {
		final String awsExportStudyUrl = "http://ecs-services-496103597.us-east-1.elb.amazonaws.com/api/study-export/";

		try {

			final HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(new MediaType[] {MediaType.APPLICATION_JSON}));
			headers.setContentType(MediaType.APPLICATION_JSON);

			final HttpEntity<COPExportStudy> request = new HttpEntity(COPExportStudy, headers);
			ResponseEntity<COPExportStudyResponse> response =
				restTemplate.exchange(awsExportStudyUrl, HttpMethod.POST, request, COPExportStudyResponse.class);

			return response.getBody();

		} catch (RestClientException e) {
			LOG.debug("Error during exportStudy", e.getMessage());
			throw e;
		}
	}

	@Override
	public COPPermissionsResponce permissions(final COPPermissions COPPermissions) {
		final String awsPermissionsUrl = "http://ecs-services-496103597.us-east-1.elb.amazonaws.com/api/brapi-permissions/";

		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(new MediaType[] {MediaType.APPLICATION_JSON}));
			headers.setContentType(MediaType.APPLICATION_JSON);

			final HttpEntity<COPPermissions> apiPermissionsCOPRequest = new HttpEntity(COPPermissions, headers);
			ResponseEntity<COPPermissionsResponce> response = restTemplate.exchange(awsPermissionsUrl, HttpMethod.POST, apiPermissionsCOPRequest, COPPermissionsResponce.class);
			return response.getBody();

		} catch (RestClientException e) {
			LOG.debug("Error during permissions", e.getMessage());
			throw e;
		}
	}
}
