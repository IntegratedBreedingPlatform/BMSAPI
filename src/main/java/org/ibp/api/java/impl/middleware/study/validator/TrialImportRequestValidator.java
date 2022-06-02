package org.ibp.api.java.impl.middleware.study.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v2.trial.TrialImportRequestDTO;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.util.Util;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class TrialImportRequestValidator {

	private static final Integer TRIAL_NAME_MAX_LENGTH = 225;
	private static final Integer TRIAL_DESCRIPTION_MAX_LENGTH = 225;
	private static final int MAX_ADDITIONAL_INFO_LENGTH = 255;
	private static final int MAX_REFERENCE_ID_LENGTH = 2000;
	private static final int MAX_REFERENCE_SOURCE_LENGTH = 255;
	private static final int MAX_CONTACT_INFO_LENGTH = 255;
	public static final String CONTACT_INFO_VALUE_EXCEEDED_LENGTH = "trial.import.contact.info.value.exceeded.length";

	@Autowired
	private FieldbookService fieldbookService;

	@Autowired
	private ProgramService programService;

	protected BindingResult errors;

	public BindingResult pruneTrialsInvalidForImport(final List<TrialImportRequestDTO> trialImportRequestDTOS, final String crop) {
		BaseValidator.checkNotEmpty(trialImportRequestDTOS, "trial.import.request.null");

		this.errors = new MapBindingResult(new HashMap<>(), TrialImportRequestDTO.class.getName());

		final Map<TrialImportRequestDTO, Integer> importRequestByIndexMap = IntStream.range(0, trialImportRequestDTOS.size())
			.boxed().collect(Collectors.toMap(trialImportRequestDTOS::get, i -> i));

		final List<String> trialNames = new ArrayList<>();

		trialImportRequestDTOS.removeIf(t -> {
			final Integer index = importRequestByIndexMap.get(t) + 1;

			if (this.isTrialNameInvalid(trialNames, t, index) ||
				this.isProgramDbIdInvalid(crop, t, index) ||
				this.isBasicTrialInfoInvalid(t, index) ||
				this.isAnyAdditionalInfoInvalid(t, index) ||
				this.isAnyExternalReferenceInvalid(t, index) ||
				this.isAnyContactsInvalid(t, index)) {
				return true;
			}

			// Add current trial name on the trialNames list after it passed all validations
			trialNames.add(t.getTrialName());

			return false;
		});

		return this.errors;
	}

	private boolean isBasicTrialInfoInvalid(final TrialImportRequestDTO t, final Integer index) {
		if (StringUtils.isEmpty(t.getTrialDescription())) {
			this.errors.reject("trial.import.description.null", new String[] {index.toString()}, "");
			return true;
		}
		if (t.getTrialDescription().length() > TRIAL_DESCRIPTION_MAX_LENGTH) {
			this.errors.reject("trial.import.description.exceed.length", new String[] {index.toString()}, "");
			return true;
		}
		if (StringUtils.isEmpty(t.getStartDate())) {
			this.errors.reject("trial.import.start.date.null", new String[] {index.toString()}, "");
			return true;
		}
		final Date startDate = Util.tryParseDate(t.getStartDate(), Util.FRONTEND_DATE_FORMAT);
		if (startDate == null) {
			this.errors.reject("trial.import.start.date.invalid.format", new String[] {index.toString()}, "");
			return true;
		}

		if (StringUtils.isNotEmpty(t.getEndDate())) {
			final Date endDate = Util.tryParseDate(t.getEndDate(), Util.FRONTEND_DATE_FORMAT);
			if (endDate == null) {
				this.errors.reject("trial.import.end.date.invalid.format", new String[] {index.toString()}, "");
				return true;
			}
			if (endDate.compareTo(startDate) < 0) {
				this.errors.reject("trial.import.end.date.invalid.date", new String[] {index.toString()}, "");
				return true;
			}
		}
		return false;
	}

	private boolean isProgramDbIdInvalid(final String crop, final TrialImportRequestDTO t, final Integer index) {
		if (StringUtils.isEmpty(t.getProgramDbId())) {
			this.errors.reject("trial.import.program.dbid.null", new String[] {index.toString()}, "");
			return true;
		}

		final Project project = this.programService.getProjectByUuidAndCrop(t.getProgramDbId(), crop);
		if (project == null) {
			this.errors.reject("trial.import.program.dbid.invalid", new String[] {index.toString()}, "");
			return true;
		}

		final Integer trialDbId = this.fieldbookService.getProjectIdByNameAndProgramUUID(t.getTrialName(), t.getProgramDbId());
		if (trialDbId != null) {
			this.errors.reject("trial.import.name.duplicate.not.unique", new String[] {index.toString()}, "");
			return true;
		}
		return false;
	}

	private boolean isTrialNameInvalid(final List<String> trialNames, final TrialImportRequestDTO t, final Integer index) {
		if (StringUtils.isEmpty(t.getTrialName())) {
			this.errors.reject("trial.import.name.null", new String[] {index.toString()}, "");
			return true;
		}
		if (t.getTrialName().length() > TRIAL_NAME_MAX_LENGTH) {
			this.errors.reject("trial.import.name.exceed.length", new String[] {index.toString()}, "");
			return true;
		}

		if (trialNames.contains(t.getTrialName())) {
			this.errors.reject("trial.import.name.duplicate.import", new String[] {index.toString()}, "");
			return true;
		}
		return false;
	}

	private boolean isAnyExternalReferenceInvalid(final TrialImportRequestDTO t, final Integer index) {
		if (t.getExternalReferences() != null) {
			return t.getExternalReferences().stream().anyMatch(r -> {
				if (r == null || StringUtils.isEmpty(r.getReferenceID()) || StringUtils.isEmpty(r.getReferenceSource())) {
					this.errors.reject("trial.import.reference.null", new String[] {index.toString(), "externalReference"}, "");
					return true;
				}
				if (StringUtils.isNotEmpty(r.getReferenceID()) && r.getReferenceID().length() > MAX_REFERENCE_ID_LENGTH) {
					this.errors.reject("trial.import.reference.id.exceeded.length", new String[] {index.toString(), "referenceID"}, "");
					return true;
				}
				if (StringUtils.isNotEmpty(r.getReferenceSource()) && r.getReferenceSource().length() > MAX_REFERENCE_SOURCE_LENGTH) {
					this.errors.reject("trial.import.reference.source.exceeded.length", new String[] {index.toString(), "referenceSource"},
						"");
					return true;
				}
				return false;
			});
		}
		return false;
	}

	private boolean isAnyContactsInvalid(final TrialImportRequestDTO t, final Integer index) {
		return t.getContacts().stream().anyMatch(c -> {
			if (c == null ) {
				this.errors.reject("trial.import.contact.info.null", new String[] {index.toString()}, "");
				return true;
			}
			if (StringUtils.isEmpty(c.getEmail()) && StringUtils.isEmpty(c.getName()) && StringUtils.isEmpty(c.getInstituteName()) && StringUtils.isEmpty(c.getType())) {
				this.errors.reject("trial.import.contact.info.empty.values", new String[] {index.toString()}, "");
				return true;
			}
			return this.anyContactFieldExceededLength(index, c);
		});
	}

	private boolean anyContactFieldExceededLength(final Integer index, final org.generationcp.middleware.service.api.user.ContactDto c) {
		if (StringUtils.isNotEmpty(c.getEmail()) && c.getEmail().length() > MAX_CONTACT_INFO_LENGTH) {
			this.errors.reject(CONTACT_INFO_VALUE_EXCEEDED_LENGTH, new String[] {index.toString(), "email"},
				"");
			return true;
		}
		if (StringUtils.isNotEmpty(c.getName()) && c.getName().length() > MAX_CONTACT_INFO_LENGTH) {
			this.errors.reject(CONTACT_INFO_VALUE_EXCEEDED_LENGTH, new String[] {index.toString(), "name"},
				"");
			return true;
		}
		if (StringUtils.isNotEmpty(c.getType()) && c.getType().length() > MAX_CONTACT_INFO_LENGTH) {
			this.errors.reject(CONTACT_INFO_VALUE_EXCEEDED_LENGTH, new String[] {index.toString(), "type"},
				"");
			return true;
		}
		if (StringUtils.isNotEmpty(c.getInstituteName()) && c.getInstituteName().length() > MAX_CONTACT_INFO_LENGTH) {
			this.errors.reject(CONTACT_INFO_VALUE_EXCEEDED_LENGTH, new String[] {index.toString(), "instituteName"},
				"");
			return true;
		}
		return false;
	}

	private boolean isAnyAdditionalInfoInvalid(final TrialImportRequestDTO t, final Integer index) {
		if (t.getAdditionalInfo() != null) {
			if (t.getAdditionalInfo().keySet().stream().anyMatch(Objects::isNull)) {
				this.errors.reject("trial.import.additional.info.null", new String[] {index.toString()}, "");
				return true;
			}
			if (t.getAdditionalInfo().keySet().stream().anyMatch(String::isEmpty)) {
				this.errors.reject("trial.import.additional.info.null", new String[] {index.toString()}, "");
				return true;
			}

			return t.getAdditionalInfo().keySet().stream().anyMatch(k -> {
				if (StringUtils.isNotEmpty(t.getAdditionalInfo().get(k)) && t.getAdditionalInfo().get(k).length() > MAX_ADDITIONAL_INFO_LENGTH) {
					this.errors.reject("trial.import.additional.info.value.exceeded.length", new String[] {index.toString(), k}, "");
					return true;
				}
				return false;
			});
		}
		return false;
	}
}
