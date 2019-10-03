package org.ibp.api.java.impl.middleware.design.util;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.ibp.api.domain.design.MainDesign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

public class ExpDesignUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ExpDesignUtil.class);

	private ExpDesignUtil() {
		// hide implicit public constructor
	}

	public static String getXmlStringForSetting(final MainDesign mainDesign) throws JAXBException {
		final JAXBContext context = JAXBContext.newInstance(MainDesign.class);
		final Marshaller marshaller = context.createMarshaller();
		final StringWriter writer = new StringWriter();
		marshaller.marshal(mainDesign, writer);
		return writer.toString();
	}

	public static String cleanBVDesingKey(final String key) {
		if (key != null) {
			return "_" + key.replace("-", "_");
		}
		return key;
	}
}
