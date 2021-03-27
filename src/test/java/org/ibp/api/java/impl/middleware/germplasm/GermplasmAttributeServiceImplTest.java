package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.attribute.AttributeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeRequestDto;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.java.impl.middleware.common.validator.AttributeValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmAttributeServiceImplTest {

	private static final Integer GID = 1;
	private static final Integer GERMPLASM_ATTRIBUTE_ID = 1;
	private static final String GERMPLASM_ATTRIBUTE_TYPE = "PASSPORT";
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
	private SecurityService securityService;

	@Captor
	private ArgumentCaptor<Set<String>> setArgumentCaptor;

	@InjectMocks
	private GermplasmAttributeServiceImpl germplasmAttributeServiceImpl;

	@Test
	public void testGetGermplasmAttributeDtos() {
		this.germplasmAttributeServiceImpl.getGermplasmAttributeDtos(GID, GERMPLASM_ATTRIBUTE_TYPE);
		Mockito.verify(this.attributeValidator).validateAttributeType(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(GERMPLASM_ATTRIBUTE_TYPE));
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(Collections.singletonList(GID)));
		Mockito.verify(this.germplasmAttributeService).getGermplasmAttributeDtos(GID, GERMPLASM_ATTRIBUTE_TYPE);
	}

	@Test
	public void testCreateGermplasmAttribute() {
		final WorkbenchUser workbenchUser = new WorkbenchUser();
		workbenchUser.setUserid(1);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(workbenchUser);
		final GermplasmAttributeRequestDto dto = this.createGermplasmAttributeRequestDto();

		this.germplasmAttributeServiceImpl.createGermplasmAttribute(GID, dto, GermplasmAttributeServiceImplTest.PROGRAM_UUID);

		Mockito.verify(this.attributeValidator).validateAttribute(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(GID),
			ArgumentMatchers.eq(dto), ArgumentMatchers.eq(null));
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
			ArgumentMatchers.eq(dto), ArgumentMatchers.eq(GERMPLASM_ATTRIBUTE_ID));
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

	@Test
	public void shouldFilterGermplasmAttributes() {
		final Set<String> codes = Collections.singleton("NOTE");

		final AttributeDTO attributeDTO = new AttributeDTO();
		attributeDTO.setCode("NOTE");
		attributeDTO.setId(new Random().nextInt());
		attributeDTO.setName("NOTES");

		Mockito.when(
			this.germplasmAttributeService.filterGermplasmAttributes(ArgumentMatchers.eq(codes), ArgumentMatchers.anySet()))
			.thenReturn(Arrays.asList(attributeDTO));

		final List<AttributeDTO> germplasmListTypes = this.germplasmAttributeServiceImpl.filterGermplasmAttributes(codes, null);
		assertNotNull(germplasmListTypes);
		assertThat(germplasmListTypes, hasSize(1));
		final AttributeDTO actualGermplasmListTypeDTO = germplasmListTypes.get(0);
		assertThat(actualGermplasmListTypeDTO.getCode(), is(attributeDTO.getCode()));
		assertThat(actualGermplasmListTypeDTO.getId(), is(attributeDTO.getId()));
		assertThat(actualGermplasmListTypeDTO.getName(), is(attributeDTO.getName()));

		Mockito.verify(this.germplasmAttributeService)
			.filterGermplasmAttributes(ArgumentMatchers.eq(codes), this.setArgumentCaptor.capture());
		final Set<String> actualTypes = this.setArgumentCaptor.getValue();
		assertNotNull(actualTypes);
		assertThat(actualTypes, hasSize(2));
		assertThat(actualTypes, contains(UDTableType.ATRIBUTS_ATTRIBUTE.getType(), UDTableType.ATRIBUTS_PASSPORT.getType()));
		Mockito.verifyNoMoreInteractions(this.germplasmAttributeService);
	}

	public GermplasmAttributeRequestDto createGermplasmAttributeRequestDto() {
		final GermplasmAttributeRequestDto germplasmAttributeRequestDto = new GermplasmAttributeRequestDto();
		germplasmAttributeRequestDto.setAttributeType(GERMPLASM_ATTRIBUTE_TYPE);
		germplasmAttributeRequestDto.setLocationId(0);
		return germplasmAttributeRequestDto;
	}

}
