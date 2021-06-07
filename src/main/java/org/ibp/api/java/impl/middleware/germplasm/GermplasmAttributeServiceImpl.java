package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeRequestDto;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.java.germplasm.GermplasmAttributeService;
import org.ibp.api.java.impl.middleware.common.validator.AttributeValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.VariableValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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

	@Autowired
	private VariableValidator variableValidator;

	@Override
	public List<GermplasmAttributeDto> getGermplasmAttributeDtos(final Integer gid, final Integer variableTypeId) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(errors, Collections.singletonList(gid));
		this.attributeValidator.validateAttributeType(errors, variableTypeId);
		return this.germplasmAttributeService.getGermplasmAttributeDtos(gid, variableTypeId);
	}

	@Override
	public GermplasmAttributeRequestDto createGermplasmAttribute(final Integer gid, final GermplasmAttributeRequestDto dto,
		final String programUUID) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.attributeValidator.validateAttribute(errors, gid, dto, null);
		this.locationValidator.validateLocation(errors, dto.getLocationId(), programUUID);
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		this.germplasmAttributeService.createGermplasmAttribute(gid, dto, loggedInUser.getUserid());
		return dto;
	}

	@Override
	public GermplasmAttributeRequestDto updateGermplasmAttribute(final Integer gid, final Integer attributeId, final GermplasmAttributeRequestDto dto, final String programUUID) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.attributeValidator.validateAttribute(errors, gid, dto, attributeId);
		this.locationValidator.validateLocation(errors, dto.getLocationId(), programUUID);
		this.germplasmAttributeService.updateGermplasmAttribute(attributeId, dto);
		return dto;
	}

	@Override
	public void deleteGermplasmAttribute(final Integer gid, final Integer attributeId) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.attributeValidator.validateGermplasmAttributeExists(errors, gid, attributeId);
		this.germplasmAttributeService.deleteGermplasmAttribute(attributeId);
	}

}
