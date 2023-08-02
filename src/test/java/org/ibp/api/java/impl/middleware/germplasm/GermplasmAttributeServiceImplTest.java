package org.ibp.api.java.impl.middleware.germplasm;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.germplasm.search.GermplasmAttributeSearchRequest;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.shared.AttributeRequestDto;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmAttributeValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmAttributeServiceImplTest {

	private static final Integer GID = 1;
	private static final Integer GERMPLASM_ATTRIBUTE_ID = 1;
	private static final Integer GERMPLASM_ATTRIBUTE_TYPE_ID = VariableType.GERMPLASM_ATTRIBUTE.getId();
	private static final Integer VARIABLE_ID = 101010;

	private static final String PROGRAM_UUID = RandomStringUtils.randomAlphanumeric(15);

	@Mock
	private org.generationcp.middleware.api.germplasm.GermplasmAttributeService germplasmAttributeService;

	@Mock
	private GermplasmAttributeValidator germplasmAttributeValidator;

	@Mock
	private GermplasmValidator germplasmValidator;

	@Mock
	private LocationValidator locationValidator;

	@InjectMocks
	private GermplasmAttributeServiceImpl germplasmAttributeServiceImpl;

	@Test
	public void testGetGermplasmAttributeDtos() {
		final GermplasmAttributeSearchRequest germplasmAttributeSearchRequest = new GermplasmAttributeSearchRequest();
		germplasmAttributeSearchRequest.setGids(Sets.newHashSet(GID));
		germplasmAttributeSearchRequest.setVariableTypeIds(Collections.singletonList(GERMPLASM_ATTRIBUTE_TYPE_ID));
		this.germplasmAttributeServiceImpl.getGermplasmAttributeDtos(germplasmAttributeSearchRequest);
		Mockito.verify(this.germplasmAttributeValidator).validateAttributeType(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(GERMPLASM_ATTRIBUTE_TYPE_ID));
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(Collections.singletonList(GID)));
		Mockito.verify(this.germplasmAttributeService).getGermplasmAttributeDtos(germplasmAttributeSearchRequest);
	}

	@Test
	public void testCreateGermplasmAttribute() {
		final AttributeRequestDto dto = this.createGermplasmAttributeRequestDto();
		dto.setVariableId(VARIABLE_ID);
		this.germplasmAttributeServiceImpl
			.createGermplasmAttribute(GID, dto, GermplasmAttributeServiceImplTest.PROGRAM_UUID);
		Mockito.verify(this.germplasmAttributeValidator)
			.validateAttribute(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(GID),
				ArgumentMatchers.eq(dto), Mockito.any());
		Mockito.verify(this.locationValidator).validateLocation(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(dto.getLocationId()));
		Mockito.verify(this.germplasmAttributeService).createGermplasmAttribute(ArgumentMatchers.eq(GID),
			ArgumentMatchers.eq(dto));
	}

	@Test
	public void testUpdateGermplasmAttribute() {
		final AttributeRequestDto dto = this.createGermplasmAttributeRequestDto();
		this.germplasmAttributeServiceImpl.updateGermplasmAttribute(GID, GERMPLASM_ATTRIBUTE_ID, dto,
			GermplasmAttributeServiceImplTest.PROGRAM_UUID);
		Mockito.verify(this.germplasmAttributeValidator)
			.validateAttribute(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(GID),
				ArgumentMatchers.eq(dto), ArgumentMatchers.eq(GERMPLASM_ATTRIBUTE_ID));
		Mockito.verify(this.locationValidator).validateLocation(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(dto.getLocationId()));
		Mockito.verify(this.germplasmAttributeService).updateGermplasmAttribute(ArgumentMatchers.eq(GERMPLASM_ATTRIBUTE_ID),
			ArgumentMatchers.eq(dto));
	}

	@Test
	public void testDeleteGermplasmAttribute() {
		this.germplasmAttributeServiceImpl.deleteGermplasmAttribute(GID, GERMPLASM_ATTRIBUTE_ID);
		Mockito.verify(this.germplasmAttributeValidator).validateGermplasmAttributeExists(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(GID), ArgumentMatchers.eq(GERMPLASM_ATTRIBUTE_ID));
		Mockito.verify(this.germplasmAttributeService).deleteGermplasmAttribute(ArgumentMatchers.eq(GERMPLASM_ATTRIBUTE_ID));
	}

	public AttributeRequestDto createGermplasmAttributeRequestDto() {
		final AttributeRequestDto germplasmAttributeRequestDto = new AttributeRequestDto();
		germplasmAttributeRequestDto.setLocationId(0);
		return germplasmAttributeRequestDto;
	}

}
