package org.ibp.api.brapi.v1.program;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.generationcp.middleware.service.api.program.ProgramDetailsDto;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.domain.common.PagedResult;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProgramEntityResponse {
    public ResponseEntity<EntityListResponse<ProgramDetailsDto>> getEntityListResponseResponseEntity(final PagedResult<ProgramDetailsDto> pagedResult) {
        final ModelMapper modelMapper = ProgramMapper.getInstance();
        final List<Program> programs = new ArrayList<>();

        for (final ProgramDetailsDto programDetailsDto : pagedResult.getPageResults()) {
            final Program program = modelMapper.map(programDetailsDto, Program.class);
            programs.add(program);
        }

        final Result<Program> results = new Result<Program>().withData(programs);
        final Pagination pagination = new Pagination().withPageNumber(pagedResult.getPageNumber()).withPageSize(pagedResult.getPageSize())
                .withTotalCount(pagedResult.getTotalResults()).withTotalPages(pagedResult.getTotalPages());

        final Metadata metadata = new Metadata().withPagination(pagination);
        return new ResponseEntity<>(new EntityListResponse(metadata, results), HttpStatus.OK);
    }

    public ResponseEntity<EntityListResponse<ProgramDetailsDto>> getEntityListResponseResponseEntityNotFound(final String message) {
        final List<Map<String, String>> status = Collections.singletonList(ImmutableMap.of("message",  message));
        final Metadata metadata = new Metadata(null, status);

        return new ResponseEntity<>(new EntityListResponse().withMetadata(metadata), HttpStatus.NOT_FOUND);
    }
}
