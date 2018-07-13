package org.ibp.api.rest.sample;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.service.CsvExportSampleListService;
import org.generationcp.commons.service.impl.CsvExportSampleListServiceImpl;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.ibp.api.domain.common.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Api(value = "Sample Services")
@Controller
@RequestMapping("/sampleLists")
public class SampleListResource {

	private static final Logger LOG = LoggerFactory.getLogger(SampleListResource.class);

	public static final String NULL = "null";
	// FIXME externalize
	public static final String ERROR = "ERROR";
	public static final String SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN = "Something went wrong, please try again";

	@Autowired
	public SampleListService sampleListService;

	@Autowired
	public ContextUtil contextUtil;

	@Autowired
	public CsvExportSampleListService csvExportSampleListService;

	@ApiOperation(value = "Create sample list", notes = "Create sample list. ")
	@RequestMapping(value = "/{crop}/sampleList", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity createSampleList(@PathVariable final String crop, @RequestBody final SampleListDto dto) {
		dto.setCropName(crop);
		final Map<String, Object> map;
		try {
			map = this.sampleListService.createSampleList(dto);

		} catch (final MiddlewareException e) {
			LOG.error("Error creating sample list", e);
			final ErrorResponse response = new ErrorResponse();
			if ("List name should be unique within the same directory".equalsIgnoreCase(e.getMessage())) {
				response.addError(e.getMessage(), "ListName");
			} else {
				response.addError(SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN);
			}
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@ApiOperation(value = "Create sample list folder", notes = "Create sample list folder. ")
	@RequestMapping(value = "/{crop}/sampleListFolder", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity createSampleListFolder(@PathVariable final String crop, @RequestParam final String folderName,
			@RequestParam final Integer parentId, @RequestParam final String programUUID) {
		final Map<String, Object> map;
		try {
			map = this.sampleListService.createSampleListFolder(folderName, parentId, programUUID);
		} catch (final MiddlewareException e) {
			LOG.error("Error creating sample list folder", e);
			final ErrorResponse response = new ErrorResponse();
			response.addError("Something went wrong, please try again");
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		}

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@ApiOperation(value = "Update sample list folder", notes = "Update sample list folder. ")
	@RequestMapping(value = "/{crop}/sampleListFolder/{folderId}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateSampleListFolderName(@PathVariable final String crop, @RequestParam final String newFolderName,
			@PathVariable final Integer folderId) {
		final Map<String, Object> map;
		try {
			map = this.sampleListService.updateSampleListFolderName(folderId, newFolderName);
		} catch (final MiddlewareException e) {
			LOG.error("Error updating sample list folder", e);
			final ErrorResponse response = new ErrorResponse();
			response.addError("Something went wrong, please try again");
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@ApiOperation(value = "Move sample list folder", notes = "Move sample list folder. ")
	@RequestMapping(value = "/{crop}/sampleListFolder/{folderId}/move", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity moveSampleListFolder(@PathVariable final String crop, @PathVariable final Integer folderId,
			@RequestParam final Integer newParentId, @RequestParam final boolean isCropList, @RequestParam final String programUUID) {
		final Map<String, Object> map;
		try {
			map = this.sampleListService.moveSampleListFolder(folderId, newParentId, isCropList, programUUID);
		} catch (final MiddlewareException e) {
			LOG.error("Error moving sample list folder", e);
			final ErrorResponse response = new ErrorResponse();
			response.addError("Something went wrong, please try again");
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@ApiOperation(value = "Delete sample list folder", notes = "Delete sample list folder. ")
	@RequestMapping(value = "/{crop}/sampleListFolder/{folderId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteSampleListFolder(@PathVariable final String crop, @PathVariable final String folderId) {
		try {
			this.sampleListService.deleteSampleListFolder(Integer.valueOf(folderId));
		} catch (final MiddlewareException e) {
			LOG.error("Error deleting sample list folder", e);
			final ErrorResponse response = new ErrorResponse();
			response.addError("Something went wrong, please try again");
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Search Sample List", notes = "Search Sample List")
	@RequestMapping(value = "/{crop}/search", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<org.generationcp.middleware.pojos.SampleList>> search(
			@ApiParam("Only return the exact match of the search text") @RequestParam final boolean exactMatch,
			@ApiParam("The name of the list to be searched") @RequestParam final String searchString, final Pageable pageable) {
		final List<org.generationcp.middleware.pojos.SampleList> sampleLists =
				sampleListService.search(searchString, exactMatch, contextUtil.getCurrentProgramUUID(), pageable);
		return new ResponseEntity<>(sampleLists, HttpStatus.OK);
	}

	@ApiOperation(value = "Download Sample List as CSV file", notes = "Download Sample List as CSV file")
	@RequestMapping(value = "/{crop}/download", method = RequestMethod.GET)
	public ResponseEntity<FileSystemResource> download(@PathVariable final String crop, @RequestParam final Integer listId,
			@RequestParam final String listName) throws IOException {

		final List<SampleDetailsDTO> sampleDetailsDTOs = this.sampleListService.getSampleDetailsDTOs(listId);

		final List<String> visibleColumns =
				Arrays.asList(CsvExportSampleListServiceImpl.SAMPLE_ENTRY, CsvExportSampleListServiceImpl.DESIGNATION,
						CsvExportSampleListServiceImpl.GID, CsvExportSampleListServiceImpl.SAMPLE_NAME,
						CsvExportSampleListServiceImpl.TAKEN_BY, CsvExportSampleListServiceImpl.SAMPLING_DATE,
						CsvExportSampleListServiceImpl.SAMPLE_UID);

		final FileExportInfo exportInfo = this.csvExportSampleListService.export(sampleDetailsDTOs, listName, visibleColumns);

		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", exportInfo.getDownloadFileName()));

		final File file = new File(exportInfo.getFilePath());
		final FileSystemResource fileSystemResource = new FileSystemResource(file);

		return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
	}
}
