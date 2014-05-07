package org.generationcp.bms.dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.generationcp.bms.domain.GermplasmScoreCard;
import org.generationcp.bms.domain.Trait;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariableSummary;
import org.generationcp.middleware.domain.h2h.Observation;
import org.generationcp.middleware.domain.h2h.TraitInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * This test relies on the data from central crop database for rice.
 */
public class SimpleDaoIntegrationTest {
	
	private static final Integer PLANT_HEIGHT_ID = 18020;
	
	private static final Integer BON2005DS_STUDY_ID = 10010;
	
	private static JdbcTemplate jdbcTemplate;
	
	private static SimpleDao dao; 
	
	@BeforeClass
	public static void once() throws FileNotFoundException, IOException {
		Properties props = new Properties();
		props.load(SimpleDaoIntegrationTest.class.getResourceAsStream("/test.properties"));
		
		DriverManagerDataSource dataSource = new DriverManagerDataSource(
				props.getProperty("db.url"), 
				props.getProperty("db.username"), 
				props.getProperty("db.password"));
		
		jdbcTemplate = new JdbcTemplate(dataSource);
		dao = new SimpleDao(jdbcTemplate);
	}
	
	// @Test
	public void testLoadPlantHeightFromView() throws MiddlewareQueryException {

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
	
	@Test
	public void testGetMeasuredTraits() {
		List<Trait> measuredTraits = dao.getMeasuredTraits(BON2005DS_STUDY_ID);		
		Assert.assertTrue(measuredTraits.size() == 8);
		
		List<Trait> numericTraits = new ArrayList<Trait>();
		for(Trait trait : measuredTraits) {
			if(trait.isNumeric()) {
				numericTraits.add(trait);
			}
		}
		// There are two numeric traits in our test study : FLW and MAT85
		Assert.assertTrue(numericTraits.size() == 2);	
	}
	
	@Test
	public void testGetTraitObservationsForTrials() {
		
		ArrayList<TraitInfo> interestingTraits = new ArrayList<TraitInfo>();
		interestingTraits.add(new TraitInfo(21735)); // amylose
		interestingTraits.add(new TraitInfo(21726)); // plant height
		interestingTraits.add(new TraitInfo(20826)); // grain density
		
		List<GermplasmScoreCard> result = dao.getTraitObservationsForTrial(5765, interestingTraits);
		
		Assert.assertTrue(result.size() > 0);
		Assert.assertTrue(result.size() == 220);
		
	}
}
