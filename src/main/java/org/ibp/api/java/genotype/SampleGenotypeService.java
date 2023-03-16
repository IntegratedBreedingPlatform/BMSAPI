package org.ibp.api.java.genotype;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.genotype.GenotypeDTO;
import org.generationcp.middleware.domain.genotype.SampleGenotypeImportRequestDto;
import org.generationcp.middleware.domain.genotype.SampleGenotypeSearchRequestDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SampleGenotypeService {

    List<Integer> importSampleGenotypes(String programUUID, Integer studyId, List<SampleGenotypeImportRequestDto> genotypeImportRequestDtos);

    List<GenotypeDTO> searchSampleGenotypes(SampleGenotypeSearchRequestDTO searchRequestDTO, Pageable pageable);

    long countSampleGenotypes(SampleGenotypeSearchRequestDTO searchRequestDTO);

    long countFilteredSampleGenotypes(SampleGenotypeSearchRequestDTO searchRequestDTO);

    List<MeasurementVariable> getSampleGenotypeColumns(final Integer studyId);
}
