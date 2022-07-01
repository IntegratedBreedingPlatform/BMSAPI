package org.ibp.api.java.impl.middleware.design;

import org.generationcp.middleware.api.crop.CropService;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ForbiddenException;
import org.ibp.api.java.design.DesignLicenseService;
import org.ibp.api.java.design.ExperimentalDesignService;
import org.ibp.api.java.design.type.ExperimentalDesignTypeService;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.design.type.ExperimentalDesignTypeServiceFactory;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentalDesignTypeValidator;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentalDesignValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ExperimentalDesignServiceImpl implements ExperimentalDesignService {
	static final String EXPERIMENT_DESIGN_LICENSE_EXPIRED = "experiment.design.license.expired";

	@Resource
	private StudyValidator studyValidator;

	@Resource
	private ExperimentalDesignValidator experimentalDesignValidator;

	@Resource
	private InstanceValidator instanceValidator;

	@Autowired
	private StudyService studyService;

	@Autowired
	private StudyEntryService middlewareStudyEntryService;

	@Resource
	private org.generationcp.middleware.service.api.study.generation.ExperimentDesignService experimentDesignMiddlewareService;

	@Resource
	private ExperimentalDesignTypeServiceFactory experimentalDesignTypeServiceFactory;

	@Resource
	private ExperimentalDesignTypeValidator experimentalDesignTypeValidator;

	@Resource
	private CropService cropServiceMW;

	@Resource
	private DesignLicenseService designLicenseService;

	@Override
	public void generateAndSaveDesign(final String cropName, final int studyId, final ExperimentalDesignInput experimentalDesignInput) {
		this.studyValidator.validate(studyId, true);
		final Integer designType = experimentalDesignInput.getDesignType();
		this.experimentalDesignValidator.validateStudyExperimentalDesign(studyId, designType);
		this.instanceValidator.validateInstanceNumbers(studyId, experimentalDesignInput.getTrialInstancesForDesignGeneration());

		// Check license validity first and foremost( if applicable for design type)
		// Raise an error right away if license is not valid
		final ExperimentalDesignTypeService experimentalDesignTypeService =
			this.experimentalDesignTypeServiceFactory.lookup(designType);
		if (experimentalDesignTypeService.requiresLicenseCheck()) {
			this.checkLicense();
		}

		// Validate design type parameters based on study germplasm list
		final List<StudyEntryDto> studyEntryDtoList = this.middlewareStudyEntryService.getStudyEntries(studyId);
		this.experimentalDesignTypeValidator.validate(experimentalDesignInput, studyEntryDtoList);

		// Generate observation unit rows
		final String programUUID = this.studyService.getProgramUUID(studyId);
		final List<ObservationUnitRow> observationUnitRows =
			experimentalDesignTypeService.generateDesign(studyId, experimentalDesignInput, programUUID, studyEntryDtoList);

		// Save experimental design and observation unit rows
		final List<MeasurementVariable> measurementVariables =
			experimentalDesignTypeService.getMeasurementVariables(studyId, experimentalDesignInput, programUUID);

		final CropType cropType = this.cropServiceMW.getCropTypeByName(cropName);
		this.experimentDesignMiddlewareService
			.saveExperimentDesign(cropType, studyId, measurementVariables,
				this.createInstanceObservationUnitRowsMap(observationUnitRows));
	}

	@Override
	public void deleteDesign(final int studyId) {
		//FIXME usage of many flags makes the code hard to read
		this.studyValidator.validate(studyId, true, true);
		this.experimentalDesignValidator.validateExperimentalDesignExistence(studyId, true);
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		try {
			this.experimentDesignMiddlewareService.deleteStudyExperimentDesign(studyId);
		} catch (final Exception e) {
			errors.reject("experimental.design.general.error");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	@Override
	public List<ExperimentDesignType> getExperimentalDesignTypes() {
		final List<ExperimentDesignType> designTypes = new ArrayList<>();

		designTypes.add(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK);
		designTypes.add(ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK);
		designTypes.add(ExperimentDesignType.ROW_COL);
		designTypes.add(ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK);
		designTypes.add(ExperimentDesignType.CUSTOM_IMPORT);
		designTypes.add(ExperimentDesignType.ENTRY_LIST_ORDER);
		designTypes.add(ExperimentDesignType.P_REP);

		return designTypes;
	}

	@Override
	public Optional<Integer> getStudyExperimentalDesignTypeTermId(final int studyId) {
		final Optional<Integer> termIdOptional =
			this.experimentDesignMiddlewareService.getStudyExperimentDesignTypeTermId(studyId);
		if (termIdOptional.isPresent()) {
			return Optional.of(termIdOptional.get());
		}
		return Optional.empty();
	}

	Map<Integer, List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow>> createInstanceObservationUnitRowsMap(
		final List<ObservationUnitRow> observationUnitRows) {
		final ModelMapper observationUnitRowMapper = new ModelMapper();
		observationUnitRowMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());

		final Map<Integer, List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow>> instanceRowsMap = new HashMap<>();
		for (final ObservationUnitRow row : observationUnitRows) {
			final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitData> variables = new HashMap<>();
			final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitData> environmentVariables = new HashMap<>();
			for (final String data : row.getVariables().keySet()) {
				variables.put(data, observationUnitRowMapper
					.map(row.getVariables().get(data), org.generationcp.middleware.service.api.dataset.ObservationUnitData.class));
			}
			for (final String data : row.getEnvironmentVariables().keySet()) {
				environmentVariables.put(data, observationUnitRowMapper.map(row.getEnvironmentVariables().get(data),
					org.generationcp.middleware.service.api.dataset.ObservationUnitData.class));
			}
			final org.generationcp.middleware.service.api.dataset.ObservationUnitRow convertedRow =
				observationUnitRowMapper.map(row, org.generationcp.middleware.service.api.dataset.ObservationUnitRow.class);
			convertedRow.setVariables(variables);
			convertedRow.setEnvironmentVariables(environmentVariables);

			final Integer trialInstance = row.getTrialInstance();
			instanceRowsMap.putIfAbsent(trialInstance, new ArrayList<>());
			instanceRowsMap.get(trialInstance).add(convertedRow);
		}
		return instanceRowsMap;
	}

	void checkLicense() {
		if (this.designLicenseService.isExpired()) {
			final String []errorKey = {EXPERIMENT_DESIGN_LICENSE_EXPIRED};
			final FieldError expiredError =
				new FieldError("", "", null, false, errorKey, null,
					"License is expired");
			throw new ForbiddenException(expiredError);
		}
	}
}
