package org.ibp.api.java.impl.middleware.germplasm.validator;

import com.google.common.collect.Sets;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.api.germplasmlist.GermplasmListVariableRequestDto;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmListVariableValidatorTest {

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private GermplasmListService germplasmListService;

	@InjectMocks
	private GermplasmListVariableValidator germplasmListVariableValidator;

	@Test
	public void testValidateAddVariableToList_throwsException_whenRequestIsNull() {
		try {
			germplasmListVariableValidator.validateAddVariableToList(null, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("param.null"));
		}
	}

	@Test
	public void testValidateAddVariableToList_throwsException_whenVariableIdIsNull() {
		try {
			final GermplasmListVariableRequestDto germplasmListVariableRequestDto = new GermplasmListVariableRequestDto();
			germplasmListVariableRequestDto.setVariableTypeId(1);
			germplasmListVariableValidator.validateAddVariableToList(null, germplasmListVariableRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("param.null"));
		}
	}

	@Test
	public void testValidateAddVariableToList_throwsException_whenVariableTypeIdIsNull() {
		try {
			final GermplasmListVariableRequestDto germplasmListVariableRequestDto = new GermplasmListVariableRequestDto();
			germplasmListVariableRequestDto.setVariableId(1);
			germplasmListVariableValidator.validateAddVariableToList(null, germplasmListVariableRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("param.null"));
		}
	}

	@Test
	public void testValidateAddVariableToList_throwsException_whenVariableTypeIdIsInvalid() {
		try {
			final GermplasmListVariableRequestDto germplasmListVariableRequestDto = new GermplasmListVariableRequestDto();
			germplasmListVariableRequestDto.setVariableId(1);
			germplasmListVariableRequestDto.setVariableTypeId(VariableType.OBSERVATION_UNIT.getId());
			germplasmListVariableValidator.validateAddVariableToList(null, germplasmListVariableRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.list.variable.type.invalid"));
		}
	}

	@Test
	public void testValidateAddVariableToList_throwsException_whenVariableIsInvalid() {
		try {
			Mockito.doReturn(new ArrayList<>()).when(ontologyVariableDataManager).getWithFilter(Mockito.any());
			final GermplasmListVariableRequestDto germplasmListVariableRequestDto = new GermplasmListVariableRequestDto();
			germplasmListVariableRequestDto.setVariableId(1);
			germplasmListVariableRequestDto.setVariableTypeId(VariableType.ENTRY_DETAIL.getId());
			germplasmListVariableValidator.validateAddVariableToList(null, germplasmListVariableRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.list.variable.does.not.exist"));
		}
	}

	@Test
	public void testValidateAddVariableToList_throwsException_whenVariableHasInvalidType() {
		try {

			final Variable variable = new Variable();
			variable.addVariableType(VariableType.TRAIT);
			Mockito.doReturn(Arrays.asList(variable)).when(ontologyVariableDataManager).getWithFilter(Mockito.any());
			final GermplasmListVariableRequestDto germplasmListVariableRequestDto = new GermplasmListVariableRequestDto();
			germplasmListVariableRequestDto.setVariableId(1);
			germplasmListVariableRequestDto.setVariableTypeId(VariableType.ENTRY_DETAIL.getId());
			germplasmListVariableValidator.validateAddVariableToList(null, germplasmListVariableRequestDto);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.list.variable.type.incompatible"));
		}
	}

	@Test
	public void testValidateVariableIsNotAssociatedToList_throwsException_whenVariableIsAssociatedToList() {
		try {
			final Integer listId = 1;
			final Integer variableId = 1;
			Mockito.doReturn(Arrays.asList(variableId)).when(germplasmListService).getListOntologyVariables(listId, null);
			germplasmListVariableValidator.validateVariableIsNotAssociatedToList(listId, variableId);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.list.variable.already.associated.to.list"));
		}
	}

	@Test
	public void testValidateVariableIsAssociatedToList_throwsException_whenVariableIsNoAssociatedToList() {
		try {
			final Integer listId = 1;
			final Integer variableId = 1;
			Mockito.doReturn(new ArrayList<>()).when(germplasmListService)
				.getListOntologyVariables(listId, GermplasmListVariableValidator.VALID_TYPES);
			germplasmListVariableValidator.validateVariableIsAssociatedToList(listId, variableId);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.list.variable.not.associated"));
		}
	}

	@Test
	public void testValidateAllVariableIdsAreAssociatedToList_throwsException_whenAnyVariableIsNoAssociatedToList() {
		try {
			final Integer listId = 1;
			final Integer variableId = 1;
			Mockito.doReturn(new ArrayList<>()).when(germplasmListService)
				.getListOntologyVariables(listId, GermplasmListVariableValidator.VALID_TYPES);
			germplasmListVariableValidator.validateAllVariableIdsAreAssociatedToList(listId, Arrays.asList(variableId));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.list.variables.not.associated"));
		}
	}

	@Test
	public void testValidateAllVariableIdsAreVariables_throwsException_whenAnyIdIsNotAVariable() {
		try {
			final Integer variableId = 1;
			Mockito.doReturn(new ArrayList<>()).when(ontologyVariableDataManager)
				.getWithFilter(Mockito.any());
			germplasmListVariableValidator.validateAllVariableIdsAreVariables(Sets.newHashSet(variableId));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.list.invalid.variables"));
		}
	}
}
