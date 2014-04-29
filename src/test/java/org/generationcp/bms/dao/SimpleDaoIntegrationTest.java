package org.generationcp.bms.dao;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariableSummary;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class SimpleDaoIntegrationTest {
	
	private static final Integer PLANT_HEIGHT_ID = 18020;
	
	@Test
	@Ignore
	//Ignoring until config externalization is implemented.
	public void testLoadPlantHeightFromView() throws MiddlewareQueryException {
		
		DriverManagerDataSource dataSource = new DriverManagerDataSource("jdbc:mysql://localhost:3306/ibdbv2_rice_central", "root", "");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		SimpleDao dao = new SimpleDao(jdbcTemplate);
		StandardVariableSummary summary = dao.getStandardVariableSummary(PLANT_HEIGHT_ID);
		
		Assert.assertNotNull(summary);
		Assert.assertEquals(PLANT_HEIGHT_ID, summary.getId());
		Assert.assertEquals("Plant_height", summary.getName());
		Assert.assertEquals("Plant height", summary.getProperty().getName());
		Assert.assertEquals("Soil to tip at maturity", summary.getMethod().getName());
		Assert.assertEquals("cm", summary.getScale().getName());
		Assert.assertEquals("Agronomic", summary.getIsA().getName());
		Assert.assertEquals("Observation variate", summary.getStoredIn().getName());
		Assert.assertEquals("Numeric variable", summary.getDataType().getName());
		Assert.assertEquals(PhenotypicType.VARIATE, summary.getPhenotypicType());
	}
}
