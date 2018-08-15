package org.ibp.api.rest.sample;

import junit.framework.Assert;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.service.impl.study.SamplePlateInfo;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class SampleListServiceImplTest {

	public static final String PROGRAM_UUID = "23487325-dwfkjfsfdsaf-32874829374";

	@Mock
	private org.generationcp.middleware.service.api.SampleListService sampleListServiceMW;

	private ResourceBundleMessageSource messageSource;

	@InjectMocks
	private SampleListServiceImpl sampleListService;

	@Before
	public void init() {

	}


	@Test
	public void testMoveSampleListFolder() {

		final int folderId = 1;
		final int newParentId = 2;
		final boolean isCropList = false;

		final SampleList sampleList = new SampleList();
		sampleList.setHierarchy(new SampleList());
		sampleList.getHierarchy().setId(newParentId);
		Mockito.when(this.sampleListServiceMW.moveSampleList(folderId, newParentId, isCropList, PROGRAM_UUID)).thenReturn(sampleList);

		final Map<String, Object> result = this.sampleListService.moveSampleListFolder(folderId, newParentId, isCropList, PROGRAM_UUID);

		Assert.assertEquals(String.valueOf(newParentId), result.get(SampleListServiceImpl.PARENT_ID));
	}

	@Test
	public void testImportSamplePlateInformationSuccess() {

		final PlateInformationDto plateInformationDto = this.createPlateInformationDto();
		Mockito.when(this.sampleListServiceMW.countSamplesByUIDs(Mockito.anySet(), Mockito.anyInt())).thenReturn(2l);
		try {
			this.sampleListService.importSamplePlateInformation(plateInformationDto);
			Mockito.verify(this.sampleListServiceMW).updateSamplePlateInfo(Mockito.eq(plateInformationDto.getListId()), Mockito.anyMap());
		} catch (ApiRequestValidationException e) {
			Assert.fail("InvalidValuesException should not be thrown.");
		}

	}

	@Test
	public void testImportSamplePlateInformationError() {

		final PlateInformationDto plateInformationDto = this.createPlateInformationDto();

		Mockito.when(this.sampleListServiceMW.countSamplesByUIDs(Mockito.anySet(), Mockito.anyInt())).thenReturn(1l);

		try {
			this.sampleListService.importSamplePlateInformation(plateInformationDto);
			Assert.fail("InvalidValuesException should be thrown.");
		} catch (ApiRequestValidationException e) {
			Assert.assertEquals("sample.sample.ids.not.present.in.file",  ((FieldError)e.getErrors().get(0)).getField());
		}

	}

	@Test
	public void testConvertToSamplePlateInfoMap() {

		final PlateInformationDto plateInformationDto = this.createPlateInformationDto();
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), PlateInformationDto.class.getName());
		final Map<String, SamplePlateInfo> map = this.sampleListService.convertToSamplePlateInfoMap(plateInformationDto, bindingResult);
		Assert.assertEquals(2, map.size());
		Assert.assertEquals("TestValue-1", map.get("SampleId-1").getPlateId());
		Assert.assertEquals("TestValue-2", map.get("SampleId-1").getWell());
		Assert.assertEquals("TestValue-3", map.get("SampleId-2").getPlateId());
		Assert.assertEquals("TestValue-4", map.get("SampleId-2").getWell());

	}

	private PlateInformationDto createPlateInformationDto() {

		final PlateInformationDto plateInformationDto = new PlateInformationDto();

		final String sampleIdColumnName = "SAMPLE_ID";
		final String plateIdColumnName = "PLATE_ID";
		final String wellColumnName = "WELL";

		final int listId = 1;
		final List<List<String>> importData = new ArrayList<>();
		importData.add(Arrays.asList(sampleIdColumnName, plateIdColumnName, wellColumnName));
		importData.add(Arrays.asList("SampleId-1","TestValue-1", "TestValue-2"));
		importData.add(Arrays.asList("SampleId-2","TestValue-3", "TestValue-4"));
		plateInformationDto.setListId(listId);
		plateInformationDto.setPlateIdHeader(plateIdColumnName);
		plateInformationDto.setSampleIdHeader(sampleIdColumnName);
		plateInformationDto.setWellHeader(wellColumnName);
		plateInformationDto.setImportData(importData);

		return plateInformationDto;

	}


}
