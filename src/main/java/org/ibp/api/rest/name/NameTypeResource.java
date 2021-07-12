package org.ibp.api.rest.name;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeRequestDTO;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.impl.middleware.name.GermplasmNameTypeService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Api(value = "Name Type Services")
@Controller
public class NameTypeResource {

	@Autowired
	private GermplasmNameTypeService germplasmNameTypeService;

	@ApiOperation(value = "Create a new name type", notes = "Create a new name type")
	@RequestMapping(value = "/crops/{cropName}/name-types", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	@ResponseBody
	public ResponseEntity<Integer> createNameType(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID, @RequestBody final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO) {
		return new ResponseEntity<>(this.germplasmNameTypeService.createNameType(germplasmNameTypeRequestDTO), HttpStatus.OK);
	}

	@ApiOperation(value = "It will retrieve all name types", notes = "It will retrieve all name types")
	@RequestMapping(value = "/crops/{cropName}/name-types", method = RequestMethod.GET)
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "page number. Start at " + PagedResult.DEFAULT_PAGE_NUMBER),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page."),
		@ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property,asc|desc. ")
	})
	@ResponseBody
	public ResponseEntity<List<GermplasmNameTypeDTO>> getNameTypes(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@ApiIgnore
		@PageableDefault(page = PagedResult.DEFAULT_PAGE_NUMBER, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable){
		final PagedResult<GermplasmNameTypeDTO> resultPage =
			new PaginatedSearch().execute(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<GermplasmNameTypeDTO>(){

				@Override
				public long getCount() {
					return NameTypeResource.this.germplasmNameTypeService.countNameTypes();
				}

				@Override
				public List<GermplasmNameTypeDTO> getResults(final PagedResult<GermplasmNameTypeDTO> pagedResult) {
					return NameTypeResource.this.germplasmNameTypeService.getNameTypes(pageable);
				}
			});

		final List<GermplasmNameTypeDTO> nameTypeDTOList = resultPage.getPageResults();
		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Filtered-Count", Long.toString(resultPage.getFilteredResults()));
		return new ResponseEntity<>(nameTypeDTOList, headers, HttpStatus.OK);
	}
}
