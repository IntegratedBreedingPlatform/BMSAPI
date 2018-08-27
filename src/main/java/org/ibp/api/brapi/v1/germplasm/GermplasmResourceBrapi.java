package org.ibp.api.brapi.v1.germplasm;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.gms.GermplasmDTO;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.java.germplasm.GermplasmService;
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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Api(value = "BrAPI Germplasm Services")
@Controller
public class GermplasmResourceBrapi {

	@Autowired
	private GermplasmService germplasmService;

	@ApiOperation(value = "Search germplasms", notes = "Search germplasms")
	@RequestMapping(value = "/{crop}/brapi/v1/germplasm-search", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<GermplasmListResponse> searchGermplasms(
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
		return null;
	}

	@ApiOperation(value = "Germplasm search by germplasmDbId", notes = "Germplasm search by germplasmDbId")
	@RequestMapping(value = "/{crop}/brapi/v1/germplasm/{germplasmDbId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<SingleGermplasmResponse> searchGermplasm(
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

				final SingleGermplasmResponse singleGermplasmResponse =
						new SingleGermplasmResponse(new Metadata(new Pagination(0, 0, 0L, 0), new HashMap<String, String>(), new URL[] {}),
								germplasm);

				return new ResponseEntity<>(singleGermplasmResponse, HttpStatus.OK);
			} else {
				return buildNotFoundSimpleGermplasmResponse();
			}
		} catch (final NumberFormatException e) {
			return buildNotFoundSimpleGermplasmResponse();
		}

	}

	private ResponseEntity<SingleGermplasmResponse> buildNotFoundSimpleGermplasmResponse() {
		final Map<String, String> status = new HashMap<>();
		status.put("message", "no germplasm found");
		final Metadata metadata = new Metadata(null, status);
		final SingleGermplasmResponse germplasmResponse = new SingleGermplasmResponse().withMetadata(metadata);
		return new ResponseEntity<>(germplasmResponse, HttpStatus.NOT_FOUND);
	}

}
