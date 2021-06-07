package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeRequestDto;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.java.impl.middleware.common.validator.AttributeValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.VariableValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmAttributeServiceImplTest {

	private static final Integer GID = 1;
	private static final Integer GERMPLASM_ATTRIBUTE_ID = 1;
	private static final Integer GERMPLASM_ATTRIBUTE_TYPE_ID = 1813;
	private static final Integer VARIABLE_ID = 101010;

	private static final String PROGRAM_UUID = RandomStringUtils.randomAlphanumeric(15);

	@Mock
	private org.generationcp.middleware.api.germplasm.GermplasmAttributeService germplasmAttributeService;

	@Mock
	private AttributeValidator attributeValidator;

	@Mock
	private GermplasmValidator germplasmValidator;

	@Mock
	private LocationValidator locationValidator;

	@Mock
	private VariableValidator variableValidator;

	@Mock
	private SecurityService securityService;

	@Captor
	private ArgumentCaptor<Set<String>> setArgumentCaptor;

	@InjectMocks
	private GermplasmAttributeServiceImpl germplasmAttributeServiceImpl;

	@Test
	public void testGetGermplasmAttributeDtos() {
		this.germplasmAttributeServiceImpl.getGermplasmAttributeDtos(GID, GERMPLASM_ATTRIBUTE_TYPE_ID);
		Mockito.verify(this.attributeValidator).validateAttributeType(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(GERMPLASM_ATTRIBUTE_TYPE_ID));
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(Collections.singletonList(GID)));
		Mockito.verify(this.germplasmAttributeService).getGermplasmAttributeDtos(GID, GERMPLASM_ATTRIBUTE_TYPE_ID);
	}

	@Test
	public void testCreateGermplasmAttribute() {
		final WorkbenchUser workbenchUser = new WorkbenchUser();
		workbenchUser.setUserid(1);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(workbenchUser);
		final GermplasmAttributeRequestDto dto = this.createGermplasmAttributeRequestDto();
		dto.setVariableId(VARIABLE_ID);
		this.germplasmAttributeServiceImpl
			.createGermplasmAttribute(GID, GERMPLASM_ATTRIBUTE_TYPE_ID, dto, GermplasmAttributeServiceImplTest.PROGRAM_UUID);

		Mockito.verify(this.attributeValidator).validateAttribute(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(GID),
			ArgumentMatchers.eq(dto), ArgumentMatchers.eq(GERMPLASM_ATTRIBUTE_TYPE_ID), Mockito.any());
		Mockito.verify(this.locationValidator).validateLocation(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(dto.getLocationId()), Mockito.any());
		Mockito.verify(this.securityService).getCurrentlyLoggedInUser();
		Mockito.verify(this.germplasmAttributeService).createGermplasmAttribute(ArgumentMatchers.eq(GID),
			ArgumentMatchers.eq(dto), ArgumentMatchers.eq(workbenchUser.getUserid()));
	}

	@Test
	public void testUpdateGermplasmAttribute() {
		final GermplasmAttributeRequestDto dto = this.createGermplasmAttributeRequestDto();
		this.germplasmAttributeServiceImpl.updateGermplasmAttribute(GID, GERMPLASM_ATTRIBUTE_ID, dto, GermplasmAttributeServiceImplTest.PROGRAM_UUID);

		Mockito.verify(this.attributeValidator).validateAttribute(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(GID),
			ArgumentMatchers.eq(dto), Mockito.any(), ArgumentMatchers.eq(GERMPLASM_ATTRIBUTE_ID));
		Mockito.verify(this.variableValidator)
			.checkVariableExist(Mockito.any(), ArgumentMatchers.eq(dto.getVariableId()), ArgumentMatchers.eq(CvId.VARIABLES.getId()),
				ArgumentMatchers.any(BindingResult.class));
		Mockito.verify(this.locationValidator).validateLocation(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(dto.getLocationId()), Mockito.any());
		Mockito.verify(this.germplasmAttributeService).updateGermplasmAttribute(ArgumentMatchers.eq(GERMPLASM_ATTRIBUTE_ID),
			ArgumentMatchers.eq(dto));
	}

	@Test
	public void testDeleteGermplasmAttribute() {
		this.germplasmAttributeServiceImpl.deleteGermplasmAttribute(GID, GERMPLASM_ATTRIBUTE_ID);
		Mockito.verify(this.attributeValidator).validateGermplasmAttributeExists(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(GID), ArgumentMatchers.eq(GERMPLASM_ATTRIBUTE_ID));
		Mockito.verify(this.germplasmAttributeService).deleteGermplasmAttribute(ArgumentMatchers.eq(GERMPLASM_ATTRIBUTE_ID));
	}

	public GermplasmAttributeRequestDto createGermplasmAttributeRequestDto() {
		final GermplasmAttributeRequestDto germplasmAttributeRequestDto = new GermplasmAttributeRequestDto();

		//germplasmAttributeRequestDto.setAttributeType(GERMPLASM_ATTRIBUTE_TYPE_ID);

		germplasmAttributeRequestDto.setLocationId(0);
		return germplasmAttributeRequestDto;
	}

}
