package org.ibp.api.java.impl.middleware.design.util;

import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.rest.dataset.ObservationUnitData;

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

	public static ObservationUnitData createObservationUnitData(final Integer variableId, final String value) {
		final ObservationUnitData observationUnitData = new ObservationUnitData();
		observationUnitData.setVariableId(variableId);
		observationUnitData.setValue(value);
		return observationUnitData;
	}
}
