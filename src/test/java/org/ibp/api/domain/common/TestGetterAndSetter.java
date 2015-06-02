
package org.ibp.api.domain.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.apache.commons.beanutils.BeanUtils;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.domain.germplasm.GermplasmListDetails;
import org.ibp.api.domain.germplasm.GermplasmListEntrySummary;
import org.ibp.api.domain.germplasm.GermplasmListSummary;
import org.ibp.api.domain.location.Location;
import org.ibp.api.domain.location.LocationType;
import org.ibp.api.domain.ontology.DataType;
import org.ibp.api.domain.ontology.ExpectedRange;
import org.ibp.api.domain.ontology.MethodDetails;
import org.ibp.api.domain.ontology.MethodSummary;
import org.ibp.api.domain.ontology.PropertyDetails;
import org.ibp.api.domain.ontology.PropertySummary;
import org.ibp.api.domain.ontology.TermSummary;
import org.ibp.api.domain.ontology.VariableCategory;
import org.ibp.api.domain.ontology.VariableType;
import org.ibp.api.domain.ontology.VariableUsages;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.MeasurementIdentifier;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.domain.study.Trait;
import org.junit.Test;

import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

/**
 * A helper test to test getter and setter solely for reducing noise in the test coverage.
 *
 */
public class TestGetterAndSetter {

	final PodamFactory factory = new PodamFactoryImpl();

	@Test
	public void testGetterAndSetter() throws Exception {
		testGivenKlass(StudyGermplasmDto.class);

		testGivenKlass(Measurement.class);
		testGivenKlass(MeasurementIdentifier.class);
		testGivenKlass(Observation.class);
		testGivenKlass(StudyGermplasm.class);
		testGivenKlass(StudySummary.class);
		testGivenKlass(Trait.class);
		
		// Ontology Domain Testing
		testGivenKlass(DataType.class);
		testGivenKlass(ExpectedRange.class);

		testGivenKlass(MethodDetails.class);
		testGivenKlass(MethodSummary.class);
		testGivenKlass(PropertyDetails.class);
		testGivenKlass(PropertySummary.class);

		testGivenKlass(TermSummary.class);
		testGivenKlass(VariableCategory.class);

		testGivenKlass(VariableType.class);
		testGivenKlass(VariableUsages.class);
		
		// Location related tests
		testGivenKlass(Location.class);
		testGivenKlass(LocationType.class);
		
		// Germplasm related domain testing
		testGivenKlass(GermplasmListDetails.class);
		testGivenKlass(GermplasmListEntrySummary.class);
		testGivenKlass(GermplasmListSummary.class);


	}

	private void testGivenKlass(final Class<?> klass) throws Exception {
		final Object source = this.factory.manufacturePojo(klass);
		final Object destination = klass.newInstance();
		BeanUtils.copyProperties(destination, source);
		assertEquals(source, destination);
		assertNotEquals(source, new Object());
		source.toString();
		assertEquals(source.hashCode(), destination.hashCode());
	}
}
