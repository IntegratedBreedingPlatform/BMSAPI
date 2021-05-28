package org.ibp.api.brapi.v2.inventory;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.generationcp.middleware.service.api.BrapiView;
import org.ibp.api.brapi.v1.common.*;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.inventory.manager.LotService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Api(value = "BrAPI Seed Lot Services")
@Controller
public class LotResourceBrapi {

    private static final String HAS_MANAGE_LOTS = "hasAnyAuthority('ADMIN','CROP_MANAGEMENT','MANAGE_INVENTORY', 'MANAGE_LOTS')";


    @Resource
    private LotService lotService;

    @ApiOperation(value = "Get a filtered list of Seed Lot descriptions", notes = "Get a filtered list of Seed Lot descriptions")
    @PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('VIEW_LOTS')")
    @RequestMapping(value = "/{crop}/brapi/v2/seedlots", method = RequestMethod.GET)
    @ResponseBody
    @JsonView(BrapiView.BrapiV2.class)
    public ResponseEntity<EntityListResponse<LotDetails>> getSeedLots(@PathVariable final String crop, @RequestParam(value = "seedLotDbId", required = false)  final String seedLotDbId,
                                                        @RequestParam(value = "germplasmDbId", required = false)  final String germplasmDbId,
                                                        @ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION)
                                                      @RequestParam(value = "page", required = false) final Integer currentPage,
                                                        @ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION)
                                                          @RequestParam(value = "pageSize", required = false) final Integer pageSize) {


        final int finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
        final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

        final LotsSearchDto lotsSearchDto = new LotsSearchDto();
        if (seedLotDbId != null) {
            lotsSearchDto.setLotUUIDs(Collections.singletonList(seedLotDbId));
        }
        if (germplasmDbId != null) {
            lotsSearchDto.setGermplasmUUIDs(Collections.singletonList(germplasmDbId));
        }
        // Only retrieve Active Lots
        lotsSearchDto.setStatus(LotStatus.ACTIVE.getIntValue());


        final PagedResult<ExtendedLotDto> resultPage =
                new PaginatedSearch().executeBrapiSearch(finalPageNumber, finalPageSize, new SearchSpec<ExtendedLotDto>() {

                    @Override
                    public long getCount() {
                        return LotResourceBrapi.this.lotService.countSearchLots(lotsSearchDto);
                    }

                    @Override
                    public List<ExtendedLotDto> getResults(final PagedResult<ExtendedLotDto> pagedResult) {
                        return LotResourceBrapi.this.lotService.searchLots(lotsSearchDto, new PageRequest(finalPageNumber, finalPageSize));

                    }
                });

        final List<ExtendedLotDto> lotDtos = resultPage.getPageResults();
        final List<LotDetails> lotList = new ArrayList<>();
        final ModelMapper mapper = LotMapper.getInstance();
        for (final ExtendedLotDto lot : lotDtos) {
            lotList.add(mapper.map(lot, LotDetails.class));
        }

        final Result<LotDetails> result = new Result<LotDetails>().withData(lotList);
        final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
                .withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());
        final Metadata metadata = new Metadata().withPagination(pagination);

        final EntityListResponse<LotDetails> entityListResponse = new EntityListResponse<>(metadata, result);

        return new ResponseEntity<>(entityListResponse, HttpStatus.OK);

    }
}
