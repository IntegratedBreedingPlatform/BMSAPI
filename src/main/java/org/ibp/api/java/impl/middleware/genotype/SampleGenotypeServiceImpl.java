package org.ibp.api.java.impl.middleware.genotype;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.genotype.SampleGenotypeDTO;
import org.generationcp.middleware.domain.genotype.SampleGenotypeImportRequestDto;
import org.generationcp.middleware.domain.genotype.SampleGenotypeSearchRequestDTO;
import org.ibp.api.java.genotype.SampleGenotypeService;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.rest.sample.SampleListValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SampleGenotypeServiceImpl implements SampleGenotypeService {

	@Autowired
	private SampleGenotypeValidator sampleGenotypeValidator;

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private SampleListValidator sampleListValidator;

	@Autowired
	private org.generationcp.middleware.api.genotype.SampleGenotypeService sampleGenotypeServiceMW;

	@Override
	public List<Integer> importSampleGenotypes(final String programUUID, final Integer studyId, final Integer sampleListId,
		final List<SampleGenotypeImportRequestDto> sampleGenotypeImportRequestDtos) {
		this.studyValidator.validate(studyId, false);
		this.sampleListValidator.validateSampleList(sampleListId);
		this.sampleGenotypeValidator.validateImport(programUUID, studyId, sampleListId, sampleGenotypeImportRequestDtos);
		return this.sampleGenotypeServiceMW.importSampleGenotypes(sampleGenotypeImportRequestDtos);
	}

	@Override
	public List<SampleGenotypeDTO> searchSampleGenotypes(final SampleGenotypeSearchRequestDTO searchRequestDTO, final Pageable pageable) {
		return this.sampleGenotypeServiceMW.searchSampleGenotypes(searchRequestDTO, pageable);
	}

	@Override
	public long countFilteredSampleGenotypes(final SampleGenotypeSearchRequestDTO searchRequestDTO) {
		return this.sampleGenotypeServiceMW.countFilteredSampleGenotypes(searchRequestDTO);
	}

	@Override
	public List<MeasurementVariable> getSampleGenotypeColumns(final Integer studyId, final List<Integer> sampleListIds) {
		this.studyValidator.validate(studyId, false);
		return this.sampleGenotypeServiceMW.getSampleGenotypeColumns(studyId, sampleListIds);
	}
}
