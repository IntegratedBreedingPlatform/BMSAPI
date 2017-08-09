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

	private static final String SAMPLE_LIST_TYPE = "SAMPLE LIST";

	@Autowired
	private org.generationcp.middleware.service.api.SampleListService service;

	@Autowired
	private SecurityService securityService;

	public Map<String, Object> createSampleList(final SampleListDto sampleListDto) {
		Preconditions.checkArgument(sampleListDto.getInstanceIds().isEmpty());
		Preconditions.checkNotNull(sampleListDto.getSamplingDate());
		Preconditions.checkNotNull(sampleListDto.getSelectionVariableId());
		Preconditions.checkNotNull(sampleListDto.getStudyId());

		final HashMap<String, Object> mapResponse = new HashMap<>();
		mapResponse.put("id", String.valueOf(0));
		try {
			final SampleListDTO sampleListdto = translateUserDetailsDtoToUserDto(sampleListDto);

			final Integer newSampleId = this.service.createOrUpdateSampleList(sampleListdto);
			mapResponse.put("id", String.valueOf(newSampleId));

		} catch (MiddlewareQueryException | ParseException e) {

			mapResponse.put("ERROR", "Error on SampleListService.createOrUpdateSampleList " + e.getMessage());
		}

		return mapResponse;
	}

	private SampleListDTO translateUserDetailsDtoToUserDto(final SampleListDto dto) throws ParseException {
		final SampleListDTO sampleListDTO = new SampleListDTO();

		sampleListDTO.setCreatedBy(securityService.getCurrentlyLoggedInUser().getName());
		sampleListDTO.setCropName(dto.getCropName());
		sampleListDTO.setDescription(dto.getDescription());
		sampleListDTO.setInstanceIds(dto.getInstanceIds());
		sampleListDTO.setNotes(dto.getNotes());
		sampleListDTO.setSamplingDate(DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT).parse(dto.getSamplingDate()));
		sampleListDTO.setSelectionVariableId(dto.getSelectionVariableId());
		sampleListDTO.setStudyId(dto.getStudyId());
		sampleListDTO.setTakenBy(dto.getTakenBy());
		sampleListDTO.setType(SAMPLE_LIST_TYPE);

		return sampleListDTO;
	}

}
