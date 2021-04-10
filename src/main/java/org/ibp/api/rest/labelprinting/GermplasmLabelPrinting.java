package org.ibp.api.rest.labelprinting;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchService;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.domain.LabelsInfoInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummary;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummaryResponse;
import org.ibp.api.rest.labelprinting.domain.OriginResourceMetadata;
import org.ibp.api.rest.labelprinting.domain.Sortable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Transactional
public class GermplasmLabelPrinting extends LabelPrintingStrategy {

	@Autowired
	private ResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private GermplasmSearchService germplasmSearchService;

	@Autowired
	private GermplasmService germplasmService;

	public static List<FileType> SUPPORTED_FILE_TYPES = Arrays.asList(FileType.CSV, FileType.PDF, FileType.XLS);

	private enum GERMPLASM_DETAILS_FIELD {
		GID(40, "GID"),
		GUID(41, "GUID"),
		GROUP_ID(42, "GroupID"),
		LOCATION(43, "Location"),
		LOCATION_ABBR(44, "Location Abbr"),
		BREEDING_METHOD_NAME(45,"Breeding Method Name"),
		PREFERRED_ID(46, "PreferredID"),
		PREFERRED_NAME(47, "Preferred Name"),
		REFERENCE(48, "Reference"),
		CREATION_DATE(49, "Creation Date"),
		METHOD_CODE(50, "Method Code"),
		METHOD_NUMBER(51, "Method Number"),
		METHOD_GROUP(52, "Method Group"),
		GROUP_SOURCE_GID(53, "Group Source GID"),
		GROUP_SOURCE_PREFERRED_NAME(54, "Group Source Preferred Name"),
		AVAILABLE(55, "Available"),
		UNITS(56, "Units"),
		LOTS(57, "Lots");

		private static Map<Integer, GERMPLASM_DETAILS_FIELD> byId =
			Arrays.stream(GERMPLASM_DETAILS_FIELD.values())
				.collect(Collectors.toMap(GERMPLASM_DETAILS_FIELD::getId, Function.identity()));

		private final int id;
		private final String name;

		GERMPLASM_DETAILS_FIELD(final int id, final String name) {
			this.id = id;
			this.name = name;
		}

		public int getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}

		public static GERMPLASM_DETAILS_FIELD getById(final int id) {
			return byId.get(id);
		}
	}

	// Germplasm Details. fields
	private static LabelType GERMPLASM_DETAILS_FIXED_LABEL_TYPES = new LabelType("Germplasm Details", "Germplasm Details")
		.withFields(Arrays.stream(GERMPLASM_DETAILS_FIELD.values())
			.map(field -> new Field(field.getId(), field.getName()))
			.collect(Collectors.toList()));


	private enum PEDIGREE_FIELD {
		CROSS(58, "Cross"),
		FEMALE_PARENT_GID(59, "Female Parent GID"),
		MALE_PARENT_GID(60, "Male Parent GID"),
		MALE_PARENT_PREFERRED_NAME(61, "Male Parent Preferred Name"),
		INMEDIATE_SOURCE_GID(62, "Immediate Source GID"),
		INMEDIATE_SOURCE_PREFERRED_NAME(63, "Immediate Source Preferred Name");

		private static Map<Integer, PEDIGREE_FIELD> byId =
			Arrays.stream(PEDIGREE_FIELD.values())
				.collect(Collectors.toMap(PEDIGREE_FIELD::getId, Function.identity()));

		private final int id;
		private final String name;

		PEDIGREE_FIELD(final int id, final String name) {
			this.id = id;
			this.name = name;
		}

		public int getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}

		public static PEDIGREE_FIELD getById(final int id) {
			return byId.get(id);
		}
	}

	private static LabelType PEDIGREE_FIXED_LABEL_TYPES = new LabelType("Pedigree", "Pedigree")
		.withFields(Arrays.stream(PEDIGREE_FIELD.values())
			.map(field -> new Field(field.getId(), field.getName()))
			.collect(Collectors.toList()));


	@Override
	void validateLabelsInfoInputData(final LabelsInfoInput labelsInfoInput) {

	}

	@Override
	LabelsNeededSummary getSummaryOfLabelsNeeded(
		final LabelsInfoInput labelsInfoInput) {
		return null;
	}

	@Override
	LabelsNeededSummaryResponse transformLabelsNeededSummary(
		final LabelsNeededSummary labelsNeededSummary) {
		return null;
	}

	@Override
	OriginResourceMetadata getOriginResourceMetadata(
		final LabelsInfoInput labelsInfoInput) {
		final String fileName = FileNameGenerator.generateFileName("germplasm-labels");
		return new OriginResourceMetadata(FileUtils.cleanFileName(fileName), new HashMap<>());	}

	@Override
	List<LabelType> getAvailableLabelTypes(
		final LabelsInfoInput labelsInfoInput) {
		return null;
	}

	@Override
	LabelsData getLabelsData(
		final LabelsGeneratorInput labelsGeneratorInput) {
		return null;
	}

	@Override
	List<FileType> getSupportedFileTypes() {
		return null;
	}

	@Override
	List<Sortable> getSortableFields() {
		return null;
	}
}