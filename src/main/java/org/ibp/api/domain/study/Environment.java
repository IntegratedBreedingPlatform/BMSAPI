
package org.ibp.api.domain.study;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a single environment (also referred to as a trial instance, or a trial location) within a Study.
 *
 */
public class Environment {

	private final Set<StudyAttribute> environmentDetails = new HashSet<>();

	public Set<StudyAttribute> getEnvironmentDetails() {
		return this.environmentDetails;
	}

	public void addEnvironmentDetail(StudyAttribute attr) {
		if (attr != null) {
			this.environmentDetails.add(attr);
		}
	}
}
