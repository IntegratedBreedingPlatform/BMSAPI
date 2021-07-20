
package org.ibp.api.java.impl.middleware.germplasm;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.pojos.UDTableType;
import org.ibp.api.domain.germplasm.GermplasmDeleteResponse;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmDeleteValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmServiceImplTest {

	@Mock
	private GermplasmService middlewareGermplasmService;

	@Mock
	private GermplasmDeleteValidator germplasmDeleteValidator;

	@Mock
	private GermplasmValidator germplasmValidator;

	@InjectMocks
	private GermplasmServiceImpl germplasmServiceImpl;

	@Mock
	private GermplasmNameTypeService germplasmNameTypeService;


	@Test
	public void shouldFilterGermplasmNameTypes() {

		final Set<String> codes = new HashSet() {{
			this.add("LNAME");
		}};

		final GermplasmNameTypeDTO nameTypeDTO = new GermplasmNameTypeDTO();
		nameTypeDTO.setCode("LNAME");
		nameTypeDTO.setId(new Random().nextInt());
		nameTypeDTO.setName("LINE NAME");

		final Set<String> types = Collections.singleton(UDTableType.NAMES_NAME.getType());
		Mockito.when(this.germplasmNameTypeService.filterGermplasmNameTypes(codes)).thenReturn(Arrays.asList(nameTypeDTO));

		final List<GermplasmNameTypeDTO> germplasmListTypes = this.germplasmServiceImpl.filterGermplasmNameTypes(codes);
		assertNotNull(germplasmListTypes);
		assertThat(germplasmListTypes, hasSize(1));
		final GermplasmNameTypeDTO actualGermplasmListTypeDTO = germplasmListTypes.get(0);
		assertThat(actualGermplasmListTypeDTO.getCode(), is(nameTypeDTO.getCode()));
		assertThat(actualGermplasmListTypeDTO.getId(), is(nameTypeDTO.getId()));
		assertThat(actualGermplasmListTypeDTO.getName(), is(nameTypeDTO.getName()));

		Mockito.verify(this.germplasmNameTypeService).filterGermplasmNameTypes(codes);
		Mockito.verifyNoMoreInteractions(this.middlewareGermplasmService);
	}

	@Test
	public void testDeleteGermplasm_WithValidGids() {

		final List<Integer> gids = Lists.newArrayList(1, 2, 3);
		Mockito.when(this.germplasmDeleteValidator.checkInvalidGidsForDeletion(gids)).thenReturn(Sets.newHashSet());
		final GermplasmDeleteResponse response = this.germplasmServiceImpl.deleteGermplasm(gids);

		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(), ArgumentMatchers.anyList());
		Mockito.verify(this.middlewareGermplasmService).deleteGermplasm(gids);
		Assert.assertThat(response.getDeletedGermplasm(), iterableWithSize(3));
		Assert.assertThat(response.getGermplasmWithErrors(), iterableWithSize(0));
	}

	@Test
	public void testDeleteGermplasm_WithInvalidGermplasmForDeletion() {

		final List<Integer> gids = Lists.newArrayList(1, 2, 3);
		Mockito.when(this.germplasmDeleteValidator.checkInvalidGidsForDeletion(gids)).thenReturn(new HashSet<>(gids));
		final GermplasmDeleteResponse response = this.germplasmServiceImpl.deleteGermplasm(gids);

		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(), ArgumentMatchers.anyList());
		Mockito.verify(this.middlewareGermplasmService, Mockito.times(0)).deleteGermplasm(ArgumentMatchers.anyList());
		Assert.assertThat(response.getDeletedGermplasm(), iterableWithSize(0));
		Assert.assertThat(response.getGermplasmWithErrors(), iterableWithSize(3));
	}

}
