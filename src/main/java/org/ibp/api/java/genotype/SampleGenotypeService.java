package org.ibp.api.java.genotype;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.genotype.SampleGenotypeDTO;
import org.generationcp.middleware.domain.genotype.SampleGenotypeImportRequestDto;
import org.generationcp.middleware.domain.genotype.SampleGenotypeSearchRequestDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SampleGenotypeService {

	List<Integer> importSampleGenotypes(String programUUID, Integer studyId, Integer sampleListId,
		List<SampleGenotypeImportRequestDto> genotypeImportRequestDtos);

	List<SampleGenotypeDTO> searchSampleGenotypes(SampleGenotypeSearchRequestDTO searchRequestDTO, Pageable pageable);

	long countFilteredSampleGenotypes(SampleGenotypeSearchRequestDTO searchRequestDTO);

	List<MeasurementVariable> getSampleGenotypeColumns(Integer studyId, List<Integer> sampleListIds);
}
