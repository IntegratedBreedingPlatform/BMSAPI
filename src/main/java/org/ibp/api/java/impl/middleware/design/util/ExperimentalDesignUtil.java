package org.ibp.api.java.impl.middleware.design.util;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.ibp.api.domain.design.ListItem;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentalDesignInput;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExperimentalDesignUtil {

	private ExperimentalDesignUtil() {
		// hide implicit public constructor
	}

	public static String getXmlStringForSetting(final MainDesign mainDesign) throws JAXBException {
		final JAXBContext context = JAXBContext.newInstance(MainDesign.class);
		final Marshaller marshaller = context.createMarshaller();
		final StringWriter writer = new StringWriter();
		marshaller.marshal(mainDesign, writer);
		return writer.toString();
	}

	public static String cleanBVDesignKey(final String key) {
		if (key != null) {
			return "_" + key.replace("-", "_");
		}
		return key;
	}

	public static void setReplatinGroups(final ExperimentalDesignInput experimentalDesignInput) {
		if (experimentalDesignInput.getUseLatenized() != null && experimentalDesignInput.getUseLatenized() && experimentalDesignInput.getReplicationsArrangement() != null) {
			if (experimentalDesignInput.getReplicationsArrangement() == 1) {
				// column
				experimentalDesignInput.setReplatinGroups(String.valueOf(experimentalDesignInput.getReplicationsCount()));
			} else if (experimentalDesignInput.getReplicationsArrangement() == 2) {
				// rows
				final StringBuilder rowReplatingGroupStringBuilder = new StringBuilder();
				for (int i = 0; i < experimentalDesignInput.getReplicationsCount(); i++) {
					if (!StringUtils.isEmpty(rowReplatingGroupStringBuilder.toString())) {
						rowReplatingGroupStringBuilder.append(",");
					}
					rowReplatingGroupStringBuilder.append("1");
				}
				experimentalDesignInput.setReplatinGroups(rowReplatingGroupStringBuilder.toString());
			}
		}
	}

	public static List<ListItem> convertToListItemList(final List<String> listString) {

		final List<ListItem> listItemList = new ArrayList<>();
		for (final String value : listString) {
			listItemList.add(new ListItem(value));
		}
		return listItemList;

	}


	public static ObservationUnitData getObservationUnitData(final ObservationUnitRow row, final Integer termId, final StudyEntryDto studyEntryDto) {
		if (termId == TermId.ENTRY_NO.getId()) {
			final Integer entryNumber = studyEntryDto.getEntryNumber();
			row.setEntryNumber(entryNumber);
			return new ObservationUnitData(termId, String.valueOf(entryNumber));
		} else if (termId == TermId.SEED_SOURCE.getId() || termId == TermId.GERMPLASM_SOURCE.getId()) {
			final Optional<String> source = studyEntryDto.getStudyEntryPropertyValue(TermId.SEED_SOURCE.getId());
			return new ObservationUnitData(termId, source.orElse(StringUtils.EMPTY));
		} else if (termId == TermId.GROUPGID.getId()) {
			final Optional<String> groupGID = studyEntryDto.getStudyEntryPropertyValue(TermId.GROUPGID.getId());
			return new ObservationUnitData(termId, groupGID.orElse(StringUtils.EMPTY));
		} else if (termId == TermId.CROSS.getId()) {
			final Optional<String> cross = studyEntryDto.getStudyEntryPropertyValue(TermId.CROSS.getId());
			return new ObservationUnitData(termId, cross.orElse(StringUtils.EMPTY));
		} else if (termId == TermId.DESIG.getId()) {
			return new ObservationUnitData(termId, studyEntryDto.getDesignation());
		} else if (termId == TermId.GID.getId()) {
			return new ObservationUnitData(termId, String.valueOf(studyEntryDto.getGid()));
			// TODO: should get entry code from properties?
//		} else if (termId == TermId.ENTRY_CODE.getId()) {
//			return new ObservationUnitData(termId, studyEntryDto.getEntryCode());
		} else if (termId == TermId.ENTRY_TYPE.getId()) {
			final Optional<String> entryType = studyEntryDto.getStudyEntryPropertyValue(TermId.ENTRY_TYPE.getId());
			return new ObservationUnitData(termId, entryType.orElse(StringUtils.EMPTY));
		} else {
			// meaning non factor
			return new ObservationUnitData(termId, StringUtils.EMPTY);
		}
	}
}
