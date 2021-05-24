package org.ibp.api.java.impl.middleware.study.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.brapi.v2.trial.TrialImportRequestDTO;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.util.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class TrialImportRequestValidator {

	private static final Logger LOG = LoggerFactory.getLogger(TrialImportRequestValidator.class);

	private static final Integer TRIAL_NAME_MAX_LENGTH = 225;
	private static final Integer TRIAL_DESCRIPTION_MAX_LENGTH = 225;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private FieldbookService fieldbookService;

	protected BindingResult errors;

	public BindingResult pruneTrialsInvalidForImport(final List<TrialImportRequestDTO> trialImportRequestDTOS, final String crop) {
		BaseValidator.checkNotEmpty(trialImportRequestDTOS, "trial.import.request.null");

		this.errors = new MapBindingResult(new HashMap<String, String>(), GermplasmImportRequest.class.getName());

		if (!trialImportRequestDTOS.stream().filter(i -> Collections.frequency(trialImportRequestDTOS, i) > 1)
			.collect(Collectors.toSet()).isEmpty()) {
			errors.reject("trial.import.duplicated.objects", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final Map<TrialImportRequestDTO, Integer> importRequestByIndexMap = IntStream.range(0, trialImportRequestDTOS.size())
			.boxed().collect(Collectors.toMap(trialImportRequestDTOS::get, i -> i));

		final List<String> trialNames = new ArrayList<>();

		trialImportRequestDTOS.removeIf(t -> {
			final Integer index = importRequestByIndexMap.get(t) + 1;

			if (StringUtils.isEmpty(t.getTrialName())) {
				errors.reject("trial.import.name.null", new String[] {index.toString()}, "");
				return true;
			}
			if (t.getTrialName().length() > TRIAL_NAME_MAX_LENGTH) {
				errors.reject("trial.import.name.exceed.length", new String[] {index.toString()}, "");
				return true;
			}

			if (trialNames.contains(t.getTrialName())) {
				errors.reject("trial.import.name.duplicate.import", new String[] {index.toString()}, "");
				return true;
			}

			if (StringUtils.isEmpty(t.getProgramDbId())) {
				errors.reject("trial.import.program.dbid.null", new String[] {index.toString()}, "");
				return true;
			}

			Project project = this.workbenchDataManager.getProjectByUuidAndCrop(t.getProgramDbId(), crop);
			if (project == null) {
				errors.reject("trial.import.program.dbid.invalid", new String[] {index.toString()}, "");
				return true;
			}

			final Integer trialDbId = this.fieldbookService.getProjectIdByNameAndProgramUUID(t.getTrialName(), t.getProgramDbId());
			if (trialDbId != null) {
				errors.reject("trial.import.name.duplicate.not.unique", new String[] {index.toString()}, "");
				return true;

			}

			if (StringUtils.isEmpty(t.getTrialDescription())) {
				errors.reject("trial.import.description.null", new String[] {index.toString()}, "");
				return true;
			}
			if (t.getTrialName().length() > TRIAL_DESCRIPTION_MAX_LENGTH) {
				errors.reject("trial.import.description.exceed.length", new String[] {index.toString()}, "");
				return true;
			}
			if (StringUtils.isEmpty(t.getStartDate())) {
				errors.reject("trial.import.start.date.null", new String[] {index.toString()}, "");
				return true;
			}
			final Date startDate = Util.tryParseDate(t.getStartDate(), Util.FRONTEND_DATE_FORMAT);
			if (startDate == null) {
				errors.reject("trial.import.start.date.invalid.format", new String[] {index.toString()}, "");
				return true;
			}

			if (StringUtils.isNotEmpty(t.getEndDate())) {
				final Date endDate = Util.tryParseDate(t.getEndDate(), Util.FRONTEND_DATE_FORMAT);
				if (endDate == null) {
					errors.reject("trial.import.end.date.invalid.format", new String[] {index.toString()}, "");
					return true;
				}
				if (endDate.compareTo(startDate) < 0) {
					errors.reject("trial.import.end.date.invalid.date", new String[] {index.toString()}, "");
					return true;
				}
				if (endDate.before(new Date()) && t.isActive()) {
					errors.reject("trial.import.active.should.be.false", new String[] {index.toString()}, "");
				}
				if (new Date().before(endDate) && !t.isActive()) {
					errors.reject("trial.import.active.should.be.true", new String[] {index.toString()}, "");
				}
			} else {
				if (!t.isActive()) {
					errors.reject("trial.import.active.should.be.true", new String[] {index.toString()}, "");
					return true;
				}
			}

			if (StringUtils.isNotEmpty(t.getCommonCropName()) && !crop.equalsIgnoreCase(t.getCommonCropName())) {
				errors.reject("trial.import.crop.invalid", new String[] {index.toString()}, "");
				return true;
			}

			if (isAnyAdditionalInfoInvalid(t, index)) {
				return true;
			}

			if (isAnyExternalReferenceInvalid(t, index)) {
				return true;
			}

			// Add current trial name on the trialNames list after it passed all validations
			trialNames.add(t.getTrialName());

			return false;
		});

		LOG.error("SIZE: " + trialImportRequestDTOS.size());

		return this.errors;
	}

	private boolean isAnyExternalReferenceInvalid(final TrialImportRequestDTO t, final Integer index) {
		if (t.getExternalReferences() != null) {
			return t.getExternalReferences().stream().anyMatch(r -> {
				if (r == null || StringUtils.isEmpty(r.getReferenceID()) || StringUtils.isEmpty(r.getReferenceSource())) {
					errors.reject("trial.import.reference.null", new String[] {index.toString(), "externalReference"}, "");
					return true;
				}
				if (StringUtils.isNotEmpty(r.getReferenceID()) && r.getReferenceID().length() > 2000) {
					errors.reject("trial.import.reference.id.exceeded.length", new String[] {index.toString(), "referenceID"}, "");
					return true;
				}
				if (StringUtils.isNotEmpty(r.getReferenceSource()) && r.getReferenceSource().length() > 255) {
					errors.reject("trial.import.reference.source.exceeded.length", new String[] {index.toString(), "referenceSource"},
						"");
					return true;
				}
				return false;
			});
		}
		return false;
	}

	private boolean isAnyAdditionalInfoInvalid(final TrialImportRequestDTO t, final Integer index) {
		if (t.getAdditionalInfo() != null) {
			if (t.getAdditionalInfo().keySet().stream().anyMatch(Objects::isNull)) {
				errors.reject("trial.import.additional.info.null", new String[] {index.toString()}, "");
				return true;
			}
			if (t.getAdditionalInfo().keySet().stream().anyMatch(String::isEmpty)) {
				errors.reject("trial.import.additional.info.null", new String[] {index.toString()}, "");
				return true;
			}

			return t.getAdditionalInfo().keySet().stream().anyMatch(k -> {
				if (StringUtils.isNotEmpty(t.getAdditionalInfo().get(k)) && t.getAdditionalInfo().get(k).length() > 255) {
					errors.reject("trial.import.additional.info.value.exceeded.length", new String[] {index.toString(), k}, "");
					return true;
				}
				return false;
			});
		}
		return false;
	}
}
