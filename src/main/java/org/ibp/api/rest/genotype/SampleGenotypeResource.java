package org.ibp.api.rest.genotype;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.genotype.SampleGenotypeDTO;
import org.generationcp.middleware.domain.genotype.SampleGenotypeImportRequestDto;
import org.generationcp.middleware.domain.genotype.SampleGenotypeSearchRequestDTO;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.genotype.SampleGenotypeService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Api(value = "Sample Genotype Services")
@RequestMapping("/crops")
@Controller
public class SampleGenotypeResource {

	@Autowired
	private SampleGenotypeService sampleGenotypeService;

	@ApiOperation(value = "Import sample genotypes into study", notes = "Import sample genotypes into study")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'MS_SAMPLE_LISTS', 'MS_IMPORT_GENOTYPES_OPTIONS', 'MS_IMPORT_GENOTYPES_FROM_GIGWA', 'MS_IMPORT_GENOTYPES_FROM_FILE')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/samples/genotypes/{sampleListId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<List<Integer>> importSampleGenotypes(final @PathVariable String crop,
		@PathVariable final String programUUID, @PathVariable final Integer studyId, @PathVariable final Integer sampleListId,
		@RequestBody final List<SampleGenotypeImportRequestDto> sampleGenotypeImportRequestDtos) {
		return new ResponseEntity<>(
			this.sampleGenotypeService.importSampleGenotypes(programUUID, studyId, sampleListId, sampleGenotypeImportRequestDtos),
			HttpStatus.OK);
	}

	@ApiOperation(value = "It will retrieve all genotypes of the study",
		notes = "It will retrieve all genotypes of the study")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'VIEW_STUDIES', 'MS_SAMPLE_GENOTYPES', 'MS_VIEW_SAMPLE_GENOTYPES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/samples/genotypes/table", method = RequestMethod.POST)
	@ResponseBody
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "Results page you want to retrieve (0..N)"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page."),
		@ApiImplicitParam(name = "sort", allowMultiple = false, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property,asc|desc. ")
	})
	public ResponseEntity<List<SampleGenotypeDTO>> getGenotypesTable(final @PathVariable String crop,
		@PathVariable final String programUUID, @PathVariable final Integer studyId,
		@RequestBody final SampleGenotypeSearchRequestDTO sampleGenotypeSearchRequestDTO,
		final @ApiIgnore @PageableDefault(page = 0, size = PagedResult.DEFAULT_PAGE_SIZE) Pageable pageable) {
		return new PaginatedSearch()
			.getPagedResult(() -> this.sampleGenotypeService.countFilteredSampleGenotypes(new SampleGenotypeSearchRequestDTO(studyId)),
				() -> this.sampleGenotypeService.countFilteredSampleGenotypes(sampleGenotypeSearchRequestDTO),
				() -> this.sampleGenotypeService.searchSampleGenotypes(sampleGenotypeSearchRequestDTO, pageable), pageable
			);
	}

	@ApiOperation(value = "Get Sample Genotype Columns", notes =
		"Retrieves ALL MeasurementVariables (columns) associated to the Sample Genotypes, "
			+ "that will be shown in the Sample Genotypes Table")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'VIEW_STUDIES' , 'MS_SAMPLE_GENOTYPES', 'MS_VIEW_SAMPLE_GENOTYPES')")
	@RequestMapping(value = "/{crop}/programs/{programUUID}/studies/{studyId}/samples/genotypes/table/columns", method = RequestMethod.GET)
	public ResponseEntity<List<MeasurementVariable>> getSampleGenotypeColumns(@PathVariable final String crop,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@RequestParam final List<Integer> sampleListIds) {

		final List<MeasurementVariable> sampleGenotypeColumns =
			this.sampleGenotypeService.getSampleGenotypeColumns(studyId, sampleListIds);

		return new ResponseEntity<>(sampleGenotypeColumns, HttpStatus.OK);
	}
}
