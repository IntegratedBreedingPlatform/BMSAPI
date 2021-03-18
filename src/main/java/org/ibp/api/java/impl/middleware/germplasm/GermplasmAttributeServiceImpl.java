package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.attribute.AttributeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeRequestDto;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.java.germplasm.GermplasmAttributeService;
import org.ibp.api.java.impl.middleware.common.validator.AttributeValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class GermplasmAttributeServiceImpl implements GermplasmAttributeService {

	@Autowired
	private org.generationcp.middleware.api.germplasm.GermplasmAttributeService germplasmAttributeService;

	@Autowired
	private AttributeValidator attributeValidator;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@Autowired
	private LocationValidator locationValidator;

	@Autowired
	private SecurityService securityService;

	@Override
	public List<GermplasmAttributeDto> getGermplasmAttributeDtos(final Integer gid, final String attributeType) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(errors, Collections.singletonList(gid));
		this.attributeValidator.validateAttributeType(errors, attributeType);
		return this.germplasmAttributeService.getGermplasmAttributeDtos(gid, attributeType);
	}

	@Override
	public GermplasmAttributeRequestDto createGermplasmAttribute(final Integer gid, final GermplasmAttributeRequestDto dto) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.attributeValidator.validateAttribute(errors, gid, dto, null);
		this.locationValidator.validateLocation(errors, dto.getLocationId());

		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		this.germplasmAttributeService.createGermplasmAttribute(gid, dto, loggedInUser.getUserid());
		return  dto;
	}

	@Override
	public GermplasmAttributeRequestDto updateGermplasmAttribute(final Integer gid, final Integer attributeId,
		final GermplasmAttributeRequestDto dto) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.attributeValidator.validateAttribute(errors, gid, dto, attributeId);
		this.locationValidator.validateLocation(errors, dto.getLocationId());

		this.germplasmAttributeService.updateGermplasmAttribute(attributeId, dto);
		return  dto;
	}

	@Override
	public void deleteGermplasmAttribute(final Integer gid, final Integer attributeId) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.attributeValidator.validateGermplasmAttributeExists(errors, gid, attributeId);
		this.germplasmAttributeService.deleteGermplasmAttribute(attributeId);
	}

	@Override
	public List<AttributeDTO> filterGermplasmAttributes(final Set<String> codes, final String type) {

		final Set<String> types = new HashSet<>();
		if(StringUtils.isEmpty(type)) {
			types.add(UDTableType.ATRIBUTS_ATTRIBUTE.getType());
			types.add(UDTableType.ATRIBUTS_PASSPORT.getType());
		} else {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), org.generationcp.middleware.api.attribute.AttributeDTO.class.getName());
			this.attributeValidator.validateAttributeType(errors, type);
			types.add(type);
		}

		return this.germplasmAttributeService.filterGermplasmAttributes(codes, types);
	}
}
