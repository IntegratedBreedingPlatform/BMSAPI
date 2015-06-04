
package org.ibp.api.domain.common;

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
import org.junit.Assert;
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
		this.testGivenKlass(StudyGermplasmDto.class);

		this.testGivenKlass(Measurement.class);
		this.testGivenKlass(MeasurementIdentifier.class);
		this.testGivenKlass(Observation.class);
		this.testGivenKlass(StudyGermplasm.class);
		this.testGivenKlass(StudySummary.class);
		this.testGivenKlass(Trait.class);

		// Ontology Domain Testing
		this.testGivenKlass(DataType.class);
		this.testGivenKlass(ExpectedRange.class);

		this.testGivenKlass(MethodDetails.class);
		this.testGivenKlass(MethodSummary.class);
		this.testGivenKlass(PropertyDetails.class);
		this.testGivenKlass(PropertySummary.class);

		this.testGivenKlass(TermSummary.class);
		this.testGivenKlass(VariableCategory.class);

		this.testGivenKlass(VariableType.class);
		this.testGivenKlass(VariableUsages.class);

		// Location related tests
		this.testGivenKlass(Location.class);
		this.testGivenKlass(LocationType.class);

		// Germplasm related domain testing
		this.testGivenKlass(GermplasmListDetails.class);
		this.testGivenKlass(GermplasmListEntrySummary.class);
		this.testGivenKlass(GermplasmListSummary.class);

	}

	private void testGivenKlass(final Class<?> klass) throws Exception {
		final Object source = this.factory.manufacturePojo(klass);
		final Object destination = klass.newInstance();
		BeanUtils.copyProperties(destination, source);
		Assert.assertEquals(source, destination);
		Assert.assertNotEquals(source, new Object());
		source.toString();
		Assert.assertEquals(source.hashCode(), destination.hashCode());
	}
}
