package org.ibp.api.brapi.v1.germplasm;

import java.util.ArrayList;
import java.util.List;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.generationcp.middleware.dao.germplasm.GermplasmSearchRequestDTO;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.generationcp.middleware.domain.germplasm.ProgenyDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmDTO;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Api(value = "BrAPI Germplasm Services")
@Controller
public class GermplasmResourceBrapi {

	@Autowired
	private GermplasmService germplasmService;

	@ApiOperation(value = "Search germplasms", notes = "Search germplasms")
	@RequestMapping(value = "/{crop}/brapi/v1/germplasm-search", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EntityListResponse<Germplasm>> searchGermplasms(
			@PathVariable
			final String crop,
			@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false)
			@RequestParam(value = "page",
					required = false)
			final Integer currentPage,
			@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
			@RequestParam(value = "pageSize",
					required = false)
			final Integer pageSize,
			@ApiParam(value = "Permanent unique identifier", required = false)
			@RequestParam(value = "germplasmPUI",
					required = false)
			final String germplasmPUI,
			@ApiParam(value = "Internal database identifier", required = false)
			@RequestParam(value = "germplasmDbId",
					required = false)
			final String germplasmDbId,
			@ApiParam(value = "Name of the germplasm", required = false)
			@RequestParam(value = "germplasmName",
					required = false)
			final String germplasmName,
			@ApiParam(value = "The common crop name. This value is discarded, crop needs to be included as part of the URL", required = false)
			@RequestParam(value = "commonCropName",
					required = false)
			final String commonCropName) {

		Integer gid;

		try {
			gid = Integer.parseInt(germplasmDbId);
		} catch (final NumberFormatException e) {
			gid = null;
		}

		final GermplasmSearchRequestDTO germplasmSearchRequestDTO =
				new GermplasmSearchRequestDTO(gid, germplasmName, germplasmPUI, currentPage, pageSize);

		final PagedResult<GermplasmDTO> resultPage = new PaginatedSearch()
				.executeBrapiSearch(germplasmSearchRequestDTO.getPage(), germplasmSearchRequestDTO.getPageSize(),
						new SearchSpec<GermplasmDTO>() {

							@Override
							public long getCount() {
								return GermplasmResourceBrapi.this.germplasmService.countGermplasmDTOs(germplasmSearchRequestDTO);
							}

							@Override
							public List<GermplasmDTO> getResults(final PagedResult<GermplasmDTO> pagedResult) {
								return GermplasmResourceBrapi.this.germplasmService.searchGermplasmDTO(germplasmSearchRequestDTO);
							}
						});

		List<Germplasm> germplasmList = new ArrayList<>();

		if (resultPage.getPageResults() != null) {
			final ModelMapper mapper = new ModelMapper();
			for (final GermplasmDTO germplasmDTO : resultPage.getPageResults()) {
				germplasmList.add(mapper.map(germplasmDTO, Germplasm.class));
			}
		}

		final Result<Germplasm> results = new Result<Germplasm>().withData(germplasmList);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
				.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		final EntityListResponse<Germplasm> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);

	}

	@ApiOperation(value = "Germplasm search by germplasmDbId", notes = "Germplasm search by germplasmDbId")
	@RequestMapping(value = "/{crop}/brapi/v1/germplasm/{germplasmDbId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<SingleEntityResponse<Germplasm>> searchGermplasm(
			@PathVariable
			final String crop,
			@PathVariable
			final String germplasmDbId) {
		try {
			final Integer gid = Integer.parseInt(germplasmDbId);
			final GermplasmDTO germplasmDTO = germplasmService.getGermplasmDTObyGID(gid);

			if (germplasmDTO != null) {
				final ModelMapper mapper = new ModelMapper();
				final Germplasm germplasm = mapper.map(germplasmDTO, Germplasm.class);

				final SingleEntityResponse<Germplasm> singleGermplasmResponse = new SingleEntityResponse<>(germplasm);

				return new ResponseEntity<>(singleGermplasmResponse, HttpStatus.OK);
			} else {
				return buildNotFoundSimpleGermplasmResponse();
			}
		} catch (final NumberFormatException e) {
			return buildNotFoundSimpleGermplasmResponse();
		}

	}

	@ApiOperation(value = "Germplasm pedigree by id", notes = "")
	@RequestMapping(value = "/{crop}/brapi/v1/germplasm/{germplasmDbId}/pedigree", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<SingleEntityResponse<PedigreeDTO>> getPedigree(
		@PathVariable
		final String crop,
		@ApiParam(value = "the internal id of the germplasm")
		@PathVariable(value = "germplasmDbId")
		final String germplasmDbId
		// TODO
		// @ApiParam(value = "text representation of the pedigree", required = false)
		// @RequestParam(value = "notation", required = false)
		// final String notation,
		// @ApiParam(value = "include array of siblings in response", required = false)
		// @RequestParam(required = false, required = false)
		// final Boolean includeSiblings
		) {

		Integer gid = null;
		try {
			gid = Integer.valueOf(germplasmDbId);
		} catch (final NumberFormatException e) {
			final SingleEntityResponse<PedigreeDTO> response = new SingleEntityResponse<PedigreeDTO>()
				.withMessage("no germplasm found");
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}

		final SingleEntityResponse<PedigreeDTO> response = new SingleEntityResponse<>(this.germplasmService.getPedigree(gid, null));

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "Germplasm progeny by id", notes = "")
	@RequestMapping(value = "/{crop}/brapi/v1/germplasm/{germplasmDbId}/progeny", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<SingleEntityResponse<ProgenyDTO>> getProgeny(
		@PathVariable
		final String crop,
		@ApiParam(value = "the internal id of the germplasm")
		@PathVariable(value = "germplasmDbId")
		final String germplasmDbId
	) {

		Integer gid = null;
		try {
			gid = Integer.valueOf(germplasmDbId);
		} catch (final NumberFormatException e) {
			final SingleEntityResponse<ProgenyDTO> response = new SingleEntityResponse<ProgenyDTO>()
				.withMessage("no germplasm found");
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}

		final SingleEntityResponse<ProgenyDTO> response = new SingleEntityResponse<>(this.germplasmService.getProgeny(gid));

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	private ResponseEntity<SingleEntityResponse<Germplasm>> buildNotFoundSimpleGermplasmResponse() {
		final SingleEntityResponse<Germplasm> germplasmResponse = new SingleEntityResponse<Germplasm>().withMessage("no germplasm found");
		return new ResponseEntity<>(germplasmResponse, HttpStatus.NOT_FOUND);
	}

}
