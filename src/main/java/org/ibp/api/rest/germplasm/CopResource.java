package org.ibp.api.rest.germplasm;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.collect.Table;
import com.google.common.io.Files;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.germplasm.pedigree.cop.CopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Api("Coefficient Of Parentage services")
@RequestMapping(value = "/crops/{cropName}")
@RestController
public class CopResource {

	@Autowired
	private CopService copService;

	@ApiOperation("Get coefficient of parentage")
	@RequestMapping(value = "/cop", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Map<Integer, Map<Integer, Double>>> getCopMatrix(
		@PathVariable final String cropName,
		@RequestParam final Set<Integer> gids
	) {
		final Map<Integer, Map<Integer, Double>> results = this.copService.coefficientOfParentage(gids).rowMap();
		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	@ApiOperation("Get coefficient of parentage as 2d array")
	@RequestMapping(value = "/cop/array", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<String[]>> getCopMatrixAs2DArray(
		@PathVariable final String cropName,
		@RequestParam final Set<Integer> gids
	) {
		final Table<Integer, Integer, Double> results = this.copService.coefficientOfParentage(gids);
		final List<String[]> array = this.convertTableToCsv(results);
		return new ResponseEntity<>(array, HttpStatus.OK);
	}

	@ApiOperation("Get coefficient of parentage as csv")
	@RequestMapping(value = "/cop/csv", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<FileSystemResource> getCopMatrixAsCsv(
		@PathVariable final String cropName,
		@RequestParam final Set<Integer> gids
	) throws IOException {
		final Table<Integer, Integer, Double> results = this.copService.coefficientOfParentage(gids);

		final File file = this.generateFile(results);

		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", FileUtils.sanitizeFileName(file.getName())));
		headers.add(HttpHeaders.CONTENT_TYPE, String.format("%s;charset=utf-8", FileUtils.detectMimeType(file.getName())));
		final FileSystemResource fileSystemResource = new FileSystemResource(file);
		return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
	}

	private File generateFile(final Table<Integer, Integer, Double> results) throws IOException {
		final File temporaryFolder = Files.createTempDir();
		final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator + "COP.csv";

		try (final CSVWriter csvWriter = new CSVWriter(
			new OutputStreamWriter(new FileOutputStream(fileNameFullPath), StandardCharsets.UTF_8), ',')
		) {
			final File newFile = new File(fileNameFullPath);
			final List<String[]> rowValues = this.convertTableToCsv(results);
			csvWriter.writeAll(rowValues);
			return newFile;
		}

	}

	private List<String[]> convertTableToCsv(final Table<Integer, Integer, Double> results) {
		final List<String[]> rowValues = new ArrayList<>();

		final List<String> header = new ArrayList<>();
		header.add("");
		header.addAll(results.columnKeySet().stream().map(Object::toString).collect(toList()));
		rowValues.add(header.toArray(new String[] {}));

		int offset = 0;
		for (final Map.Entry<Integer, Map<Integer, Double>> rowEntrySet : results.rowMap().entrySet()) {
			final List<String> row = new ArrayList<>();
			row.add(rowEntrySet.getKey().toString());

			/*
			 * x x x x x x
			 *   x x x x x
			 *     x x x x
			 *       x x x
			 *         x x
			 *           x
			 */
			IntStream.range(0, offset).forEach(i -> row.add(""));
			offset++;

			row.addAll(rowEntrySet.getValue().values().stream().map(Object::toString).collect(toList()));
			rowValues.add(row.toArray(new String[] {}));
		}
		return rowValues;
	}

}
