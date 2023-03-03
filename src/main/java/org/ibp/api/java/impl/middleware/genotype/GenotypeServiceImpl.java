package org.ibp.api.java.impl.middleware.genotype;

import org.generationcp.middleware.domain.genotype.GenotypeImportRequestDto;
import org.ibp.api.java.genotype.GenotypeService;
import org.springframework.beans.factory.annotation.Autowired;
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
}
