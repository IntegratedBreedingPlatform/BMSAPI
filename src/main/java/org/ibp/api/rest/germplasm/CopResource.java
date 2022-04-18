package org.ibp.api.rest.germplasm;

import com.google.common.io.Files;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.germplasm.pedigree.cop.BTypeEnum;
import org.generationcp.middleware.api.germplasm.pedigree.cop.CopResponse;
import org.ibp.api.exception.ApiRuntime2Exception;
import org.ibp.api.java.file.FileStorageService;
import org.ibp.api.java.impl.middleware.germplasm.cop.CopService;
import org.generationcp.middleware.api.germplasm.pedigree.cop.CopUtils;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

// TODO move package
@Api("Coefficient Of Parentage services")
@RequestMapping(value = "/crops/{cropName}")
@RestController
public class CopResource {

	@Autowired
	private CopService copService;

	@Autowired
	private FileStorageService fileStorageService;

	@Value("${cop.max.entries}")
	private Integer copMaxEntries;

	@ApiOperation("Calculate coefficient of parentage")
	@RequestMapping(value = "/cop/calculation", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<CopResponse> calculateCopMatrix(
		@PathVariable final String cropName,
		@RequestBody final Set<Integer> gids,
		@RequestParam final BTypeEnum btype,
		@ApiParam("whether to use or ignore pre-existing cop values. If true, re-calculate everything from scratch")
		@RequestParam(required = false, defaultValue = "false") final boolean reset
	) {
		BaseValidator.checkNotNull(gids, "param.null", new String[] {"gids"});
		BaseValidator.checkNotNull(btype, "param.null", new String[] {"btype"});
		BaseValidator.checkArgument(gids.size() <= this.copMaxEntries, "cop.calculation.max.entries", new Integer[] {this.copMaxEntries});
		final CopResponse results = this.copService.calculateCoefficientOfParentage(gids, null, btype, reset);
		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	@ApiIgnore // prototype, not part of release
	@ApiOperation("Calculate coefficient of parentage for a list")
	@RequestMapping(value = "/cop/calculation/list/{listId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<CopResponse> calculateCopMatrixForList(
		@PathVariable final String cropName,
		@PathVariable final Integer listId,
		@RequestParam final BTypeEnum btype
	) {
		if (!this.fileStorageService.isConfigured()) {
			throw new ApiRuntime2Exception("", "cop.file.storage.not.configured");
		}
		final CopResponse results = this.copService.calculateCoefficientOfParentage(listId, btype);
		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	@ApiOperation("Cancel coefficient of parentage calculation jobs")
	@RequestMapping(value = "/cop/calculation", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Void> cancelJobs(
		@PathVariable final String cropName,
		@RequestParam(required = false) final Set<Integer> gids,
		@RequestParam(required = false) final Integer listId
	) {
		// Either listId or gids param
		BaseValidator.checkArgument(isEmpty(gids) != (listId == null), "cop.params.gids.or.listid");
		this.copService.cancelJobs(gids, listId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiIgnore // prototype, not part of release
	@ApiOperation("Get coefficient of parentage")
	@RequestMapping(value = "/cop", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<CopResponse> getCopMatrix(
		@PathVariable final String cropName,
		@RequestParam(required = false) final Set<Integer> gids,
		@RequestParam(required = false) final Integer listId,
		final HttpServletRequest request,
		final HttpServletResponse response
	) throws IOException {
		// Either listId or gids param
		BaseValidator.checkArgument(isEmpty(gids) != (listId == null), "cop.params.gids.or.listid");
		final CopResponse results = this.copService.viewCoefficientOfParentage(gids, listId, request, response);
		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	@ApiIgnore // prototype, not part of release
	@ApiOperation("Get coefficient of parentage as csv")
	@RequestMapping(value = "/cop/csv/list/{listId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<byte[]> downloadCopMatrixAsCsv(
		@PathVariable final String cropName,
		@PathVariable final Integer listId
	) throws IOException {
		final byte[] file = this.copService.downloadFile(listId);

		final HttpHeaders headers = new HttpHeaders();
		final String fileName = CopUtils.getFileName(listId);
		headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", FileUtils.sanitizeFileName(fileName)));
		headers.add(HttpHeaders.CONTENT_TYPE, String.format("%s;charset=utf-8", FileUtils.detectMimeType(fileName)));
		return new ResponseEntity<>(file, headers, HttpStatus.OK);
	}

	@ApiOperation("Get coefficient of parentage as csv")
	@RequestMapping(value = "/cop/csv", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<FileSystemResource> getCopMatrixAsCsv(
		@PathVariable final String cropName,
		@RequestParam final Set<Integer> gids
	) throws IOException {
		final CopResponse results = this.copService.viewCoefficientOfParentage(gids, null, null, null);

		// FIXME avoid writing to disk
		final File temporaryFolder = Files.createTempDir();
		final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator + "COP.csv";
		final File file = CopUtils.generateFile(results, fileNameFullPath);

		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", FileUtils.sanitizeFileName(file.getName())));
		headers.add(HttpHeaders.CONTENT_TYPE, String.format("%s;charset=utf-8", FileUtils.detectMimeType(file.getName())));
		final FileSystemResource fileSystemResource = new FileSystemResource(file);
		return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
	}

}
