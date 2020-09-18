package org.ibp.api.java.impl.middleware.study;

import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.java.impl.middleware.study.validator.StudyGermplasmValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.study.StudyGermplasmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional
public class StudyGermplasmServiceImpl implements StudyGermplasmService {

    @Resource
    private StudyValidator studyValidator;

    @Autowired
    private PedigreeService pedigreeService;

    @Autowired
    private CrossExpansionProperties crossExpansionProperties;

    @Resource
    private StudyGermplasmValidator studyGermplasmValidator;

    @Resource
    private org.generationcp.middleware.service.api.study.StudyGermplasmService middlewareStudyGermplasmService;

    @Resource
    private DatasetService datasetService;

    @Override
    public StudyGermplasmDto replaceStudyGermplasm(final Integer studyId, final Integer entryId, final StudyGermplasmDto studyGermplasmDto) {
        final Integer gid = studyGermplasmDto.getGermplasmId();
        this.studyValidator.validate(studyId, true);
        this.studyGermplasmValidator.validate(studyId, entryId, gid);

        return this.middlewareStudyGermplasmService.replaceStudyGermplasm(studyId, entryId, gid, this.pedigreeService.getCrossExpansion(gid, this.crossExpansionProperties));
    }

    @Override
    public List<StudyEntryDto> getStudyEntries(final Integer studyId, final StudyEntrySearchDto.Filter filter, final Pageable pageable) {
        this.studyValidator.validate(studyId, false);
        return this.middlewareStudyGermplasmService.getStudyEntries(studyId, filter, pageable);
    }

    @Override
    public long countAllStudyEntries(final Integer studyId) {
        this.studyValidator.validate(studyId, false);
        return this.middlewareStudyGermplasmService.countStudyEntries(studyId);
    }

    @Override
    public List<MeasurementVariable> getEntryDescriptorColumns(final Integer studyId) {
        this.studyValidator.validate(studyId, false);
        final Integer plotDatasetId =
            datasetService.getDatasets(studyId, new HashSet<>(Arrays.asList(DatasetTypeEnum.PLOT_DATA.getId()))).get(0).getDatasetId();

        final List<Integer> termsToRemove = Lists
            .newArrayList(TermId.OBS_UNIT_ID.getId(), TermId.STOCKID.getId());

        final List<MeasurementVariable> entryDescriptors =
            this.datasetService.getObservationSetVariables(plotDatasetId, Lists
                .newArrayList(VariableType.GERMPLASM_DESCRIPTOR.getId()));

        //Remove OBS_UNIT_ID column and STOCKID if present
        entryDescriptors.removeIf(entry -> termsToRemove.contains(entry.getTermId()));

        //Add Inventory related columns
        entryDescriptors.add(this.buildVirtualColumn("LOTS", TermId.GID_ACTIVE_LOTS_COUNT));
        entryDescriptors.add(this.buildVirtualColumn("AVAILABLE", TermId.GID_AVAILABLE_BALANCE));
        entryDescriptors.add(this.buildVirtualColumn("UNIT", TermId.GID_UNIT));

        return entryDescriptors;
    }

    private MeasurementVariable buildVirtualColumn(final String name, final TermId termId) {
        final MeasurementVariable sampleColumn = new MeasurementVariable();
        sampleColumn.setName(name);
        sampleColumn.setAlias(name);
        sampleColumn.setTermId(termId.getId());
        sampleColumn.setFactor(true);
        return sampleColumn;
    }

    public void setDatasetService(final DatasetService datasetService) {
        this.datasetService = datasetService;
    }

}
