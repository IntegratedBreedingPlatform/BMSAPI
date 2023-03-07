package org.ibp.api.java.impl.middleware.genotype;

import org.generationcp.middleware.domain.genotype.GenotypeDTO;
import org.generationcp.middleware.domain.genotype.GenotypeImportRequestDto;
import org.generationcp.middleware.domain.genotype.GenotypeSearchRequestDTO;
import org.ibp.api.java.genotype.GenotypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class GenotypeServiceImpl implements GenotypeService {

    @Autowired
    private GenotypeValidator genotypeValidator;

    @Autowired
    private org.generationcp.middleware.api.genotype.GenotypeService genotypeServiceMW;

    @Override
    public List<Integer> importGenotypes(final String programUUID, final Integer listId, final List<GenotypeImportRequestDto> genotypeImportRequestDtos) {
        this.genotypeValidator.validateImport(programUUID, listId, genotypeImportRequestDtos);
        this.genotypeServiceMW.importGenotypes(genotypeImportRequestDtos);
        return new ArrayList<>();
    }

    @Override
    public List<GenotypeDTO> searchGenotypes(GenotypeSearchRequestDTO searchRequestDTO, Pageable pageable) {
        return this.genotypeServiceMW.searchGenotypes(searchRequestDTO, pageable);
    }

    @Override
    public long countGenotypes(final GenotypeSearchRequestDTO searchRequestDTO) {
        return this.genotypeServiceMW.countGenotypes(searchRequestDTO);
    }

    @Override
    public long countFilteredGenotypes(final GenotypeSearchRequestDTO searchRequestDTO) {
        return this.genotypeServiceMW.countFilteredGenotypes(searchRequestDTO);
    }
}
