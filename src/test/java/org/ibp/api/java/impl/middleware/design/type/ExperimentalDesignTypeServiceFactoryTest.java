package org.ibp.api.java.impl.middleware.design.type;

import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExperimentalDesignTypeServiceFactoryTest {

	private final ExperimentalDesignTypeServiceFactory factory = new ExperimentalDesignTypeServiceFactory();

	@Before
	public void init() {
		this.factory.addService(new EntryListOrderDesignTypeServiceImpl());
	}

	@Test
	public void testLookup() {
		Assert.assertNotNull(this.factory.lookup(ExperimentDesignType.ENTRY_LIST_ORDER.getId()));
		Assert.assertNull(this.factory.lookup(0));
	}

}
