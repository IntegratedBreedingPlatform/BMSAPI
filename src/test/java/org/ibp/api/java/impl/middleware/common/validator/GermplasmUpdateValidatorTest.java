package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.attribute.AttributeDTO;
import org.generationcp.middleware.api.germplasm.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.ibp.api.java.germplasm.GermplasmService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmUpdateValidatorTest {

	@Mock
	private GermplasmService germplasmService;

	@Mock
	private org.generationcp.middleware.api.germplasm.GermplasmService germplasmMiddlewareService;

	@Mock
	private LocationDataManager locationDataManager;

	@InjectMocks
	private GermplasmUpdateValidator germplasmUpdateValidator;

	@Test
	public void testValidation_Success() {

		final String programUUID = RandomStringUtils.random(10);
		final Germplasm germplasm = new Germplasm(1);
		germplasm.setGermplasmUUID(UUID.randomUUID().toString());

		final Location location = new Location(1);
		location.setLabbr("AFG");

		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.setGid(germplasm.getGid());
		germplasmUpdateDTO.setGermplasmUUID(germplasm.getGermplasmUUID());
		germplasmUpdateDTO.setLocationAbbreviation(location.getLabbr());
		germplasmUpdateDTO.setCreationDate("20200101");
		germplasmUpdateDTO.setBreedingMethodAbbr(null);
		germplasmUpdateDTO.getNames().put("DRVNM", "");
		germplasmUpdateDTO.getNames().put("LNAME", "");
		germplasmUpdateDTO.getAttributes().put("NOTE", "");
		germplasmUpdateDTO.getAttributes().put("ACQ_DATE", "");

		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);

		when(this.germplasmService.filterGermplasmNameTypes(Mockito.anySet()))
			.thenReturn(Arrays.asList(new GermplasmNameTypeDTO(null, "DRVNM", null), new GermplasmNameTypeDTO(null, "LNAME", null)));
		when(this.germplasmService.filterGermplasmAttributes(Mockito.anySet()))
			.thenReturn(Arrays.asList(new AttributeDTO(null, "NOTE", null), new AttributeDTO(null, "ACQ_DATE", null)));
		when(this.germplasmMiddlewareService.getGermplasmByGIDs(Mockito.anyList())).thenReturn(Arrays.asList(germplasm));
		when(this.germplasmMiddlewareService.getGermplasmByGUIDs(Mockito.anyList())).thenReturn(Arrays.asList(germplasm));
		when(this.locationDataManager
			.getFilteredLocations(programUUID, null, null, new ArrayList<>(Arrays.asList(germplasmUpdateDTO.getLocationAbbreviation())),
				false)).thenReturn(Arrays.asList(location));

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		this.germplasmUpdateValidator.validateEmptyList(errors, germplasmUpdateList);
		this.germplasmUpdateValidator.validateAttributeAndNameCodes(errors, germplasmUpdateList);
		this.germplasmUpdateValidator.validateGermplasmIdAndGermplasmUUID(errors, germplasmUpdateList);
		this.germplasmUpdateValidator.validateLocationAbbreviation(errors, programUUID, germplasmUpdateList);
		this.germplasmUpdateValidator.validateBreedingMethod(germplasmUpdateList);
		this.germplasmUpdateValidator.validateCreationDate(errors, germplasmUpdateList);

		assertFalse(errors.hasErrors());

	}

	@Test
	public void testValidate_EmptyList() {
		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateValidator.validateEmptyList(errors, new ArrayList<>());
		Mockito.verify(errors).reject("germplasm.update.empty.list", "");
	}

	@Test
	public void testValidate_InvalidAttributeAndNameCodes() {

		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.getNames().put("DRVNM", "");
		germplasmUpdateDTO.getNames().put("LNAME", "");
		germplasmUpdateDTO.getAttributes().put("NOTE", "");
		germplasmUpdateDTO.getAttributes().put("ACQ_DATE", "");

		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);

		when(this.germplasmService.filterGermplasmNameTypes(Mockito.anySet()))
			.thenReturn(Arrays.asList(new GermplasmNameTypeDTO(null, "DRVNM", null)));
		when(this.germplasmService.filterGermplasmAttributes(Mockito.anySet()))
			.thenReturn(Arrays.asList(new AttributeDTO(null, "NOTE", null)));

		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateValidator.validateAttributeAndNameCodes(errors, germplasmUpdateList);
		Mockito.verify(errors).reject("germplasm.update.invalid.name.code", new String[] {"LNAME"}, "");
		Mockito.verify(errors).reject("germplasm.update.invalid.attribute.code", new String[] {"ACQ_DATE"}, "");
	}

	@Test
	public void testValidate_NoGidsAndGUID() {
		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);
		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateValidator.validateGermplasmIdAndGermplasmUUID(errors, germplasmUpdateList);
		Mockito.verify(errors).reject("germplasm.update.missing.gid.and.uuid", "");
	}

	@Test
	public void testValidate_InvalidGidAndGUID() {
		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.setGid(1);
		germplasmUpdateDTO.setGermplasmUUID(UUID.randomUUID().toString());

		final Germplasm germplasm = new Germplasm(2);

		when(this.germplasmMiddlewareService.getGermplasmByGIDs(Mockito.anyList())).thenReturn(Arrays.asList(germplasm));
		when(this.germplasmMiddlewareService.getGermplasmByGUIDs(Mockito.anyList())).thenReturn(Arrays.asList(germplasm));

		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);
		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateValidator.validateGermplasmIdAndGermplasmUUID(errors, germplasmUpdateList);
		Mockito.verify(errors).reject("germplasm.update.invalid.gid", new String[] {germplasmUpdateDTO.getGid().toString()}, "");
		Mockito.verify(errors).reject("germplasm.update.invalid.uuid", new String[] {germplasmUpdateDTO.getGermplasmUUID()}, "");
	}

	@Test
	public void testValidate_LocationAbbr() {

		final String programUUID = RandomStringUtils.random(10);
		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.setLocationAbbreviation("AFG");

		when(this.locationDataManager
			.getFilteredLocations(programUUID, null, null, new ArrayList<>(Arrays.asList(germplasmUpdateDTO.getLocationAbbreviation())),
				false)).thenReturn(Arrays.asList(new Location()));

		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);
		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateValidator.validateLocationAbbreviation(errors, programUUID, germplasmUpdateList);
		Mockito.verify(errors).reject("germplasm.update.invalid.location.abbreviation", new String[] {"AFG"}, "");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testValidate_BreedingMethod() {
		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.setBreedingMethodAbbr("UAC");
		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);
		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateValidator.validateBreedingMethod(germplasmUpdateList);
	}

	@Test
	public void testValidate_CreationDate() {
		final String programUUID = RandomStringUtils.random(10);
		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.setCreationDate("AAAABBCC");
		final List<GermplasmUpdateDTO> germplasmUpdateList = Arrays.asList(germplasmUpdateDTO);
		final BindingResult errors = Mockito.mock(BindingResult.class);
		this.germplasmUpdateValidator.validateCreationDate(errors, germplasmUpdateList);
		Mockito.verify(errors).reject("germplasm.update.invalid.creation.date", "");
	}

}
