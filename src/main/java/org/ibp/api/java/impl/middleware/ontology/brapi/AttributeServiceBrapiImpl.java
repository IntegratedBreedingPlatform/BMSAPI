package org.ibp.api.java.impl.middleware.ontology.brapi;

import org.generationcp.middleware.api.brapi.VariableTypeGroup;
import org.generationcp.middleware.api.brapi.v2.attribute.AttributeDTO;
import org.generationcp.middleware.domain.search_request.brapi.v2.AttributeSearchRequestDTO;
import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.brapi.v2.AttributeServiceBrapi;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AttributeServiceBrapiImpl implements AttributeServiceBrapi {

	@Autowired
	private org.generationcp.middleware.api.brapi.VariableServiceBrapi middlewareVariableServiceBrapi;

	@Override
	public List<AttributeDTO> getGermplasmAttributes(final String crop, final AttributeSearchRequestDTO requestDTO,
		final Pageable pageable) {
		final List<VariableDTO> observationVariables = this.middlewareVariableServiceBrapi
			.getVariables(new VariableSearchRequestDTO(requestDTO), pageable, VariableTypeGroup.GERMPLASM_ATTRIBUTES);

		final ModelMapper modelMapper = AttributeMapper.getInstance();
		final List<AttributeDTO> attributeDTOS = new ArrayList<>();
		for (final VariableDTO dto : observationVariables) {
			final AttributeDTO attributeDTO = modelMapper.map(dto, AttributeDTO.class);
			attributeDTO.setCommonCropName(crop);
			attributeDTOS.add(attributeDTO);
		}
		return attributeDTOS;
	}

	@Override
	public long countGermplasmAttributes(final AttributeSearchRequestDTO requestDTO) {
		return this.middlewareVariableServiceBrapi
			.countVariables(new VariableSearchRequestDTO(requestDTO), VariableTypeGroup.GERMPLASM_ATTRIBUTES);
	}

}
