package org.ibp.api.java.impl.middleware.design.util;

import org.apache.commons.lang3.StringUtils;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.design.ExperimentDesignInput;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

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

	public static void setReplatinGroups(final ExperimentDesignInput experimentDesignInput) {
		if (experimentDesignInput.getUseLatenized() != null && experimentDesignInput.getUseLatenized() && experimentDesignInput.getReplicationsArrangement() != null) {
			if (experimentDesignInput.getReplicationsArrangement() == 1) {
				// column
				experimentDesignInput.setReplatinGroups(String.valueOf(experimentDesignInput.getReplicationsCount()));
			} else if (experimentDesignInput.getReplicationsArrangement() == 2) {
				// rows
				final StringBuilder rowReplatingGroupStringBuilder = new StringBuilder();
				for (int i = 0; i < experimentDesignInput.getReplicationsCount(); i++) {
					if (!StringUtils.isEmpty(rowReplatingGroupStringBuilder.toString())) {
						rowReplatingGroupStringBuilder.append(",");
					}
					rowReplatingGroupStringBuilder.append("1");
				}
				experimentDesignInput.setReplatinGroups(rowReplatingGroupStringBuilder.toString());
			}
		}
	}
}
