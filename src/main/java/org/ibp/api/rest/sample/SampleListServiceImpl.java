package org.ibp.api.rest.sample;

import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.samplelist.SampleListDTO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class SampleListServiceImpl implements SampleListService {

	private static final Logger LOG = LoggerFactory.getLogger(SampleListServiceImpl.class);
	private static final String SAMPLE_LIST = "Sample List";
	private static final String SAMPLE_LIST_TYPE = "SAMPLE LIST";
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT);

	@Autowired private org.generationcp.middleware.service.api.SampleListService service;

	public Map<String, Object> createSampleList(final SampleListDto sampleListDto) {

		{
			LOG.debug(sampleListDto.toString());
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), SampleListServiceImpl.SAMPLE_LIST);
			final HashMap<String, Object> mapResponse = new HashMap<String, Object>();
			mapResponse.put("id", String.valueOf(0));
			try {
				final SampleListDTO sampleListdto = translateUserDetailsDtoToUserDto(sampleListDto);

				final Integer newSampleId = this.service.createOrUpdateSampleList(sampleListdto);
				mapResponse.put("id", String.valueOf(newSampleId));

			} catch (MiddlewareQueryException e) {
				LOG.info("Error on SampleListService.createOrUpdateSampleList " + e.getMessage());
			} catch (ParseException e) {
				LOG.info("Error on SampleListService.createOrUpdateSampleList " + e.getMessage());
			}

			return mapResponse;
		}

	}

	private SampleListDTO translateUserDetailsDtoToUserDto(final SampleListDto dto) throws ParseException {
		final SampleListDTO sampleListDTO = new SampleListDTO();

		sampleListDTO.setCreatedBy(dto.getCreatedBy());
		sampleListDTO.setCropName(dto.getCropName());
		sampleListDTO.setDescription(dto.getDescription());
		sampleListDTO.setInstanceIds(dto.getInstanceIds());
		sampleListDTO.setNotes(dto.getNotes());
		sampleListDTO.setSamplingDate(SIMPLE_DATE_FORMAT.parse(dto.getSamplingDate()));
		sampleListDTO.setSelectionVariableId(dto.getSelectionVariableId());
		sampleListDTO.setStudyId(dto.getStudyId());
		sampleListDTO.setTakenBy(dto.getTakenBy());
		sampleListDTO.setTrialName(dto.getTrialName());
		sampleListDTO.setType(SAMPLE_LIST_TYPE);

		return sampleListDTO;
	}

}
