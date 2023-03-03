package org.ibp.api.java.genotype;

import org.generationcp.middleware.domain.genotype.GenotypeImportRequestDto;

import java.util.List;

public interface GenotypeService {

    List<Integer> importGenotypes(String programUUID, Integer listId, List<GenotypeImportRequestDto> genotypeImportRequestDtos);
}
