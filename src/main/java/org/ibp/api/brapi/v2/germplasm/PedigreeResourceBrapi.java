package org.ibp.api.brapi.v2.germplasm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.brapi.v2.germplasm.PedigreeNodeDTO;
import org.ibp.api.brapi.PedigreeServiceBrapi;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v2.BrapiResponseMessageGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Api(value = "BrAPI v2.1 Pedigree Services")
@Controller
public class PedigreeResourceBrapi {

	@Autowired
	private PedigreeServiceBrapi pedigreeServiceBrapi;

	@Autowired
	private BrapiResponseMessageGenerator<PedigreeNodeDTO> responseMessageGenerator;

	@ApiOperation(value = "Send a list of pedigree nodes to update existing information on a server", notes = "Send a list of pedigree nodes to update existing information on a server")
	@RequestMapping(value = "/{crop}/brapi/v2/pedigree", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<EntityListResponse<PedigreeNodeDTO>> updatePedigreeNodes(@PathVariable final String crop,
		@RequestBody final Map<String, PedigreeNodeDTO> pedigreeNodeDTOMap) {
		final PedigreeNodesUpdateResponse pedigreeNodesUpdateResponse = this.pedigreeServiceBrapi.updatePedigreeNodes(pedigreeNodeDTOMap);
		final Result<PedigreeNodeDTO> results = new Result<PedigreeNodeDTO>().withData(pedigreeNodesUpdateResponse.getEntityList());
		final Pagination pagination = new Pagination().withPageNumber(BrapiPagedResult.DEFAULT_PAGE_NUMBER).withPageSize(1)
			.withTotalCount(Long.valueOf(pedigreeNodesUpdateResponse.getUpdatedSize())).withTotalPages(1);
		final Metadata metadata = new Metadata().withPagination(pagination)
			.withStatus(this.responseMessageGenerator.getMessagesList(pedigreeNodesUpdateResponse));
		final EntityListResponse<PedigreeNodeDTO> entityListResponse = new EntityListResponse<>(metadata, results);
		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}
}
