package org.ibp.api.rest.sample;

import org.apache.commons.lang3.RandomStringUtils;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SampleListValidatorTest {

	private final SampleListValidator validator = new SampleListValidator();

	@Test
	public void testValidateSampleListSuccess() {

		final SampleListDto sampleListDto = new SampleListDto();
		sampleListDto.setInstanceIds(Arrays.asList(1));
		sampleListDto.setSelectionVariableId(1);
		sampleListDto.setListName(RandomStringUtils.randomAlphabetic(10));
		sampleListDto.setCreatedDate("2019-01-01");

		try {
			validator.validateSampleList(sampleListDto);
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
			validator.validateSampleList(sampleListDto);
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
			validator.validateSampleList(sampleListDto);
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
	public void testValidateFolderName() {

		try {
			validator.validateFolderName("Name");
		} catch (final ApiRequestValidationException e) {
			Assert.fail("Expcected to NOT throw ApiRequestValidationException");
		}

		try {
			validator.validateFolderName(null);
			Assert.fail("Expcected to throw ApiRequestValidationException");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("sample.list.folder.is.null", e.getErrors().get(0).getCode());
		}

	}

	@Test
	public void testvalidateFolderId() {

		try {
			validator.validateFolderId(1);
		} catch (final ApiRequestValidationException e) {
			Assert.fail("Expcected to NOT throw ApiRequestValidationException");
		}

		try {
			validator.validateFolderId(null);
			Assert.fail("Expcected to throw ApiRequestValidationException");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("sample.list.parent.id.is.null", e.getErrors().get(0).getCode());
		}

	}

	@Test
	public void testvalidateProgramUUID() {

		try {
			validator.validateProgramUUID("UUID");
		} catch (final ApiRequestValidationException e) {
			Assert.fail("Expcected to NOT throw ApiRequestValidationException");
		}

		try {
			validator.validateProgramUUID(null);
			Assert.fail("Expcected to throw ApiRequestValidationException");
		} catch (final ApiRequestValidationException e) {
			Assert.assertEquals("sample.list.program.uuid.is.null", e.getErrors().get(0).getCode());
		}

	}

}
