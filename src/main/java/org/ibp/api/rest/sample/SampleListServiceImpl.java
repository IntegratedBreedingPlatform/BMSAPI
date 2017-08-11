package org.ibp.api.rest.sample;

import com.google.common.base.Preconditions;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.samplelist.SampleListDTO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class SampleListServiceImpl implements SampleListService {

	@Autowired
	private org.generationcp.middleware.service.api.SampleListService service;

	@Autowired
	private SecurityService securityService;

	public Map<String, Object> createSampleList(final SampleListDto sampleListDto) {
		Preconditions.checkArgument(sampleListDto.getInstanceIds() != null, "The Instance List must not be null");
		Preconditions.checkArgument(!sampleListDto.getInstanceIds().isEmpty(), "The Instance List must not be empty");
		Preconditions.checkNotNull(sampleListDto.getSelectionVariableId(), "The Selection Variable Id must not be empty");
		Preconditions.checkNotNull(sampleListDto.getStudyId(), "The Study Id must not be empty");

		final HashMap<String, Object> mapResponse = new HashMap<>();
		mapResponse.put("id", String.valueOf(0));
		try {
			final SampleListDTO sampleListdto = translateToSampleListDto(sampleListDto);

			final Integer newSampleId = this.service.createOrUpdateSampleList(sampleListdto);

			if (newSampleId != null) {
				mapResponse.put("id", String.valueOf(newSampleId));
			} else {
				mapResponse.put("ERROR", "Error on SampleListService.createOrUpdateSampleList");
			}

		} catch (MiddlewareQueryException | ParseException e) {
			mapResponse.put("ERROR", "Error on SampleListService.createOrUpdateSampleList " + e.getMessage());
		}

		return mapResponse;
	}

	private SampleListDTO translateToSampleListDto(final SampleListDto dto) throws ParseException {
		final SampleListDTO sampleListDTO = new SampleListDTO();

		sampleListDTO.setCreatedBy(securityService.getCurrentlyLoggedInUser().getName());
		sampleListDTO.setCropName(dto.getCropName());
		sampleListDTO.setDescription(dto.getDescription());
		sampleListDTO.setInstanceIds(dto.getInstanceIds());
		sampleListDTO.setNotes(dto.getNotes());

		if (!dto.getSamplingDate().isEmpty()) {
			sampleListDTO.setSamplingDate(DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT).parse(dto.getSamplingDate()));
		}

		sampleListDTO.setSelectionVariableId(dto.getSelectionVariableId());
		sampleListDTO.setStudyId(dto.getStudyId());
		sampleListDTO.setTakenBy(dto.getTakenBy());

		return sampleListDTO;
	}

	public void setService(org.generationcp.middleware.service.api.SampleListService service) {
		this.service = service;
	}

	public void setSecurityService(SecurityService securityService) {

		this.securityService = securityService;
	}
}
