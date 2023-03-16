package org.ibp.api.rest.sample;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.service.CsvExportSampleListService;
import org.generationcp.commons.service.impl.CsvExportSampleListServiceImpl;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.domain.samplelist.SampleListDTO;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Api(value = "Sample Services")
@Controller
@RequestMapping("/crops")
public class SampleListResource {

	private static final Logger LOG = LoggerFactory.getLogger(SampleListResource.class);

	public static final String NULL = "null";
	// FIXME externalize
	public static final String ERROR = "ERROR";
	public static final String SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN = "Something went wrong, please try again";

	@Autowired
	public SampleListService sampleListService;

	@Autowired
	public CsvExportSampleListService csvExportSampleListService;

	@ApiOperation(value = "Create sample list", notes = "Create sample list. ")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES', 'LISTS', 'SAMPLES_LISTS')")
	@RequestMapping(value = "/{crop}/sample-lists", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity createSampleList(@PathVariable final String crop, @RequestParam final String programUUID,
		@RequestBody final SampleListDto dto) {
		dto.setCropName(crop);
		// The programUUID in request is the program where sample list is made from. It is used to filter permissions for a program-level user
		// It is not necessarily set in the SampleListDto as the list might be a crop-level list
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

	@ApiOperation(value = "Delete sample list entries", notes = "Delete sample list entries. ")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES', 'LISTS', 'SAMPLES_LISTS', 'DELETE_SAMPLES')")
	@RequestMapping(value = "/{crop}/sample-lists/{sampleListId}/entries", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteSampleListEntries(@PathVariable final String crop, @RequestParam final String programUUID,
		@PathVariable final Integer sampleListId,
		@RequestParam(required = true) final Set<Integer> selectedEntries) {
		this.sampleListService.deleteSamples(sampleListId, new ArrayList<>(selectedEntries));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Create sample list folder", notes = "Create sample list folder. ")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES', 'LISTS', 'SAMPLES_LISTS')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/sample-list-folders", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity createSampleListFolder(@PathVariable final String crop, @PathVariable final String programUUID,
		@RequestParam final String folderName,
		@RequestParam final Integer parentId) {
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
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES', 'LISTS', 'SAMPLES_LISTS')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/sample-list-folders/{folderId}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateSampleListFolderName(@PathVariable final String crop, @PathVariable final String programUUID,
		@PathVariable final Integer folderId, @RequestParam final String newFolderName) {
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
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES', 'LISTS', 'SAMPLES_LISTS')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/sample-list-folders/{folderId}/move", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity moveSampleListFolder(@PathVariable final String crop, @PathVariable final String programUUID,
		@PathVariable final Integer folderId,
		@RequestParam final Integer newParentId, @RequestParam final boolean isCropList) {
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
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES', 'LISTS', 'SAMPLES_LISTS')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/sample-list-folders/{folderId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteSampleListFolder(@PathVariable final String crop, @PathVariable final String programUUID,
		@PathVariable final Integer folderId) {
		try {
			this.sampleListService.deleteSampleListFolder(folderId);
		} catch (final MiddlewareException e) {
			LOG.error("Error deleting sample list folder", e);
			final ErrorResponse response = new ErrorResponse();
			response.addError("Something went wrong, please try again");
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Search Sample List", notes = "Search Sample List")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES', 'LISTS', 'SAMPLES_LISTS')")
	@RequestMapping(value = "/{crop}/sample-lists/search", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<org.generationcp.middleware.pojos.SampleList>> search(@PathVariable final String crop,
		@RequestParam final String programUUID,
		@ApiParam("Only return the exact match of the search text") @RequestParam final boolean exactMatch,
		@ApiParam("The name of the list to be searched") @RequestParam final String searchString, final Pageable pageable) {
		final List<org.generationcp.middleware.pojos.SampleList> sampleLists =
			this.sampleListService.search(searchString, exactMatch, programUUID, pageable);
		return new ResponseEntity<>(sampleLists, HttpStatus.OK);
	}

	@ApiOperation(value = "Download Sample List as CSV file", notes = "Download Sample List as CSV file")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES', 'LISTS', 'SAMPLES_LISTS')")
	@RequestMapping(value = "/{crop}/sample-lists/{listId}/download", method = RequestMethod.GET)
	public ResponseEntity<FileSystemResource> download(@PathVariable final String crop, @PathVariable final Integer listId,
		@RequestParam final String programUUID,
		@RequestParam final String listName) throws IOException {

		final List<SampleDetailsDTO> sampleDetailsDTOs = this.sampleListService.getSampleDetailsDTOs(listId);

		final List<String> visibleColumns = Arrays
			.asList(CsvExportSampleListServiceImpl.SAMPLE_ENTRY, CsvExportSampleListServiceImpl.DESIGNATION,
				CsvExportSampleListServiceImpl.GID, CsvExportSampleListServiceImpl.SAMPLE_NAME, CsvExportSampleListServiceImpl.TAKEN_BY,
				CsvExportSampleListServiceImpl.SAMPLING_DATE, CsvExportSampleListServiceImpl.SAMPLE_UID,
				CsvExportSampleListServiceImpl.PLATE_ID, CsvExportSampleListServiceImpl.WELL);

		// TODO: Set the enumerator variable name to blank for now, until we improve Manage Samples CSV Export to include enumerator variable columns
		final FileExportInfo exportInfo = this.csvExportSampleListService.export(sampleDetailsDTOs, listName, visibleColumns, "");

		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", exportInfo.getDownloadFileName()));
		headers.add(HttpHeaders.CONTENT_TYPE,
			String.format("%s;charset=utf-8", FileUtils.detectMimeType(exportInfo.getDownloadFileName())));
		final File file = new File(exportInfo.getFilePath());
		final FileSystemResource fileSystemResource = new FileSystemResource(file);

		return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
	}

	//TODO: Is necessary make a refactor in the future with this service for do it more generic to import samples not only Plate Id and well.
	@ApiOperation(value = "Import Plate Information", notes = "Current implementation only supports patch on plateId and well attributes")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES', 'LISTS', 'SAMPLES_LISTS')")
	@RequestMapping(value = "/{crop}/sample-lists/{listId}/samples", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity saveSamplePlateInformation(
		@PathVariable final String crop, @PathVariable final Integer listId, @RequestParam final String programUUID,
		@RequestBody final List<SampleDTO> sampleDTOs) {
		this.sampleListService.importSamplePlateInformation(sampleDTOs, listId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Get sample lists given a tree parent node folder", notes = "Get sample lists given a tree parent node folder")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'VIEW_STUDIES', 'LISTS', 'SAMPLES_LISTS')")
	@RequestMapping(value = "/{crop}/sample-lists/tree", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<TreeNode>> getSampleListByParentFolderId(
		@ApiParam(value = "The crop type", required = true) @PathVariable final String crop,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
		@ApiParam(value = "The id of the parent folder") @RequestParam(required = false) final String parentFolderId,
		@ApiParam(value = "Only folders") @RequestParam(required = true) final Boolean onlyFolders) {
		final List<TreeNode> children = this.sampleListService.getSampleListChildrenNodes(crop, programUUID, parentFolderId, onlyFolders);
		return new ResponseEntity<>(children, HttpStatus.OK);
	}

	@ApiOperation(value = "Get sample lists associated to study", notes = "Get sample lists associated to study")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'VIEW_STUDIES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/sample-lists/{studyId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<SampleListDTO>> getSampleListsByStudy(
		@PathVariable final String crop,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@RequestParam(required = false) final boolean withGenotypesOnly) {
		return new ResponseEntity<>(this.sampleListService.getSampleListsByStudy(studyId, withGenotypesOnly), HttpStatus.OK);
	}
}
