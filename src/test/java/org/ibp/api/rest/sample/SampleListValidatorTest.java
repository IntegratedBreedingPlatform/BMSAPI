package org.ibp.api.rest.sample;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.enumeration.SampleListType;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.service.api.SampleListService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SampleListValidatorTest {

	@Mock
	private SampleListService sampleListServiceMW;

	@InjectMocks
	private final SampleListValidator validator = new SampleListValidator();

	@Before
	public void setUp() {
		ContextHolder.setCurrentProgram("23487325-dwfkjfsfdsaf-32874829374");
		ContextHolder.setCurrentCrop("maize");
	}

	@Test
	public void testValidateSampleListSuccess() {

		final SampleListDto sampleListDto = new SampleListDto();
		sampleListDto.setInstanceIds(Arrays.asList(1));
		sampleListDto.setSelectionVariableId(1);
		sampleListDto.setListName(RandomStringUtils.randomAlphabetic(10));
		sampleListDto.setCreatedDate("2019-01-01");

		try {
			this.validator.validateSampleList(sampleListDto);
		} catch (final ApiRequestValidationException e) {
			Assert.fail("Expcected to NOT hrow ApiRequestValidationException");
		}

	}

	@Test
	public void testValidateSampleListErrorRequiredFields() {

		final SampleListDto sampleListDto = new SampleListDto();
		sampleListDto.setInstanceIds(null);
		sampleListDto.setSelectionVariableId(null);
		sampleListDto.setListName("");
		sampleListDto.setCreatedDate("");

		try {
			this.validator.validateSampleList(sampleListDto);
			Assert.fail("Expcected to throw ApiRequestValidationException");
		} catch (final ApiRequestValidationException e) {
			final List<String> codes = new ArrayList<>();
			for (final ObjectError error : e.getErrors()) {
				codes.add(error.getCode());
			}
			Assert.assertTrue(codes.contains("sample.list.instance.list.must.not.be.null"));
			Assert.assertTrue(codes.contains("sample.list.selection.variable.id.must.not.empty"));
			Assert.assertTrue(codes.contains("sample.list.listname.must.not.empty"));
			Assert.assertTrue(codes.contains("sample.list.created.date.empty"));
		}

	}

	@Test
	public void testValidateSampleListMaxLengthReached() {

		final String longString = RandomStringUtils.randomAlphabetic(70000);

		final SampleListDto sampleListDto = new SampleListDto();
		sampleListDto.setInstanceIds(Arrays.asList(1));
		sampleListDto.setSelectionVariableId(1);
		sampleListDto.setListName(longString);
		sampleListDto.setDescription(longString);
		sampleListDto.setNotes(longString);
		sampleListDto.setCreatedDate("2019-01-01");

		try {
			this.validator.validateSampleList(sampleListDto);
			Assert.fail("Expcected to throw ApiRequestValidationException");
		} catch (final ApiRequestValidationException e) {
			final List<String> codes = new ArrayList<>();
			for (final ObjectError error : e.getErrors()) {
				codes.add(error.getCode());
			}
			Assert.assertTrue(codes.contains("sample.list.listname.exceed.length"));
			Assert.assertTrue(codes.contains("sample.list.description.exceed.length"));
			Assert.assertTrue(codes.contains("sample.list.notes.exceed.length"));
		}

	}

	@Test
	public void testValidateSampleList_InvalidListId() {
		try {
			this.validator.validateSampleList(1);
		} catch (final ApiRequestValidationException e) {
			final List<String> codes = new ArrayList<>();
			for (final ObjectError error : e.getErrors()) {
				codes.add(error.getCode());
			}
			Assert.assertTrue(codes.contains("sample.list.id.is.invalid"));
		}
	}

	@Test
	public void testValidateSampleList_InvalidListType() {
		final Integer listId = 1;
		final SampleList sampleList = new SampleList();
		sampleList.setType(SampleListType.FOLDER);
		Mockito.when(this.sampleListServiceMW.getSampleList(listId)).thenReturn(sampleList);
		try {
			this.validator.validateSampleList(1);
		} catch (final ApiRequestValidationException e) {
			final List<String> codes = new ArrayList<>();
			for (final ObjectError error : e.getErrors()) {
				codes.add(error.getCode());
			}
			Assert.assertTrue(codes.contains("sample.list.type.is.invalid"));
		}
	}

	@Test
	public void testVerifyListEntryIdsExist_EmptyEntryIds() {
		try {
			this.validator.verifyListEntryIdsExist(1, Collections.emptyList());
		} catch (final ApiRequestValidationException e) {
			final List<String> codes = new ArrayList<>();
			for (final ObjectError error : e.getErrors()) {
				codes.add(error.getCode());
			}
			Assert.assertTrue(codes.contains("sample.ids.selected.entries"));
		}
	}
	
	@Test
	public void testVerifyListEntryIdsExist_NonExistentEntryIds() {
		try {
			this.validator.verifyListEntryIdsExist(1, Collections.singletonList(1));
		} catch (final ApiRequestValidationException e) {
			final List<String> codes = new ArrayList<>();
			for (final ObjectError error : e.getErrors()) {
				codes.add(error.getCode());
			}
			Assert.assertTrue(codes.contains("sample.ids.not.exist"));
		}
	}

	@Test
	public void testValidateFolderName() {

		try {
			this.validator.validateFolderName("Name");
		} catch (final ApiRequestValidationException e) {
			Assert.fail("Expcected to NOT throw ApiRequestValidationException");
		}

		try {
			this.validator.validateFolderName(null);
			Assert.fail("Expcected to throw ApiRequestValidationException");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("sample.list.folder.is.null", e.getErrors().get(0).getCode());
		}

	}

	@Test
	public void testvalidateFolderId() {

		try {
			this.validator.validateFolderIdAndProgram(1);
		} catch (final ApiRequestValidationException e) {
			Assert.fail("Expcected to NOT throw ApiRequestValidationException");
		}

		try {
			this.validator.validateFolderIdAndProgram(null);
			Assert.fail("Expcected to throw ApiRequestValidationException");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("sample.list.parent.id.is.null", e.getErrors().get(0).getCode());
		}

	}

}
