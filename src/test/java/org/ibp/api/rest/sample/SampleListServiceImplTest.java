package org.ibp.api.rest.sample;

import junit.framework.Assert;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.service.impl.study.SamplePlateInfo;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
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
		final int listId = 1;
		final List<SampleDTO> sampleDTOs = this.createSampleDto();
		Mockito.when(this.sampleListServiceMW.countSamplesByUIDs(Mockito.anySetOf(String.class), Mockito.anyInt())).thenReturn(2l);
		try {
			this.sampleListService.importSamplePlateInformation(sampleDTOs, listId);
			Mockito.verify(this.sampleListServiceMW).updateSamplePlateInfo(Mockito.eq(listId), Mockito.anyMapOf(
					String.class, SamplePlateInfo.class));
		} catch (ApiRequestValidationException e) {
			Assert.fail("InvalidValuesException should not be thrown.");
		}

	}

	@Test
	public void testImportSamplePlateInformationError() {
		final int listId = 1;
		final List<SampleDTO> sampleDTOs = this.createSampleDto();

		Mockito.when(this.sampleListServiceMW.countSamplesByUIDs(Mockito.anySetOf(String.class), Mockito.anyInt())).thenReturn(1l);

		try {
			this.sampleListService.importSamplePlateInformation(sampleDTOs, listId);
			Assert.fail("InvalidValuesException should be thrown.");
		} catch (ApiRequestValidationException e) {
			Assert.assertEquals("sample.sample.ids.not.present.in.file",  e.getErrors().get(0).getCodes()[1]);
		}

	}

	@Test
	public void testConvertToSamplePlateInfoMap() {
		final List<SampleDTO> sampleDTOs = this.createSampleDto();
		final Map<String, SamplePlateInfo> map = this.sampleListService.convertToSamplePlateInfoMap(sampleDTOs);
		Assert.assertEquals(2, map.size());
		Assert.assertEquals("TestValue-1", map.get("SampleId-1").getPlateId());
		Assert.assertEquals("TestValue-4", map.get("SampleId-1").getWell());
		Assert.assertEquals("TestValue-2", map.get("SampleId-2").getPlateId());
		Assert.assertEquals("TestValue-5", map.get("SampleId-2").getWell());

	}

	private List<SampleDTO> createSampleDto() {
		final List<SampleDTO> sampleDTOs = new ArrayList<>();
		SampleDTO sampleDTO = new SampleDTO();
		sampleDTO.setSampleBusinessKey("SampleId-1");
		sampleDTO.setPlateId("TestValue-1");
		sampleDTO.setWell("TestValue-4");
		sampleDTOs.add(sampleDTO);
		sampleDTO = new SampleDTO();
		sampleDTO.setSampleBusinessKey("SampleId-2");
		sampleDTO.setPlateId("TestValue-2");
		sampleDTO.setWell("TestValue-5");
		sampleDTOs.add(sampleDTO);
		return sampleDTOs;

	}


}
