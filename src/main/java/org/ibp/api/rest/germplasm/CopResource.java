package org.ibp.api.rest.germplasm;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.io.Files;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.germplasm.pedigree.cop.CopResponse;
import org.generationcp.middleware.api.germplasm.pedigree.cop.CopService;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;

// TODO move package
@Api("Coefficient Of Parentage services")
@RequestMapping(value = "/crops/{cropName}")
@RestController
public class CopResource {

	@Autowired
	private CopService copService;

	@ApiOperation("Calculate coefficient of parentage")
	@RequestMapping(value = "/cop/calculation", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<CopResponse> calculateCopMatrix(
		@PathVariable final String cropName,
		@RequestBody final Set<Integer> gids
	) {
		final CopResponse results = this.copService.calculateCoefficientOfParentage(gids);
		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	@ApiOperation("Calculate coefficient of parentage for a list")
	@RequestMapping(value = "/cop/calculation/list/{listId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<CopResponse> calculateCopMatrixForList(
		@PathVariable final String cropName,
		@PathVariable final Integer listId
	) {
		final CopResponse results = this.copService.calculateCoefficientOfParentage(listId);
		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	@ApiOperation("Cancel coefficient of parentage calculation jobs")
	@RequestMapping(value = "/cop/calculation", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Void> cancelJobs(
		@PathVariable final String cropName,
		@RequestParam final Set<Integer> gids
	) {
		this.copService.cancelJobs(gids);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation("Get coefficient of parentage")
	@RequestMapping(value = "/cop", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<CopResponse> getCopMatrix(
		@PathVariable final String cropName,
		@RequestParam final Set<Integer> gids
	) {
		final CopResponse results = this.copService.coefficientOfParentage(gids);
		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	@ApiOperation("Get coefficient of parentage as csv")
	@RequestMapping(value = "/cop/csv", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<FileSystemResource> getCopMatrixAsCsv(
		@PathVariable final String cropName,
		@RequestParam final Set<Integer> gids
	) throws IOException {
		final CopResponse results = this.copService.coefficientOfParentage(gids);

		final File file = this.generateFile(results);

		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", FileUtils.sanitizeFileName(file.getName())));
		headers.add(HttpHeaders.CONTENT_TYPE, String.format("%s;charset=utf-8", FileUtils.detectMimeType(file.getName())));
		final FileSystemResource fileSystemResource = new FileSystemResource(file);
		return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
	}

	private File generateFile(final CopResponse results) throws IOException {
		final File temporaryFolder = Files.createTempDir();
		final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator + "COP.csv";

		try (final CSVWriter csvWriter = new CSVWriter(
			new OutputStreamWriter(new FileOutputStream(fileNameFullPath), StandardCharsets.UTF_8), ',')
		) {
			final File newFile = new File(fileNameFullPath);
			csvWriter.writeAll(results.getArray());
			return newFile;
		}

	}

}
