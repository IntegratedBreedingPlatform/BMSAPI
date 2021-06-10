package org.ibp.api.java.job;

import org.generationcp.middleware.domain.job.JobDTO;

public interface JobService {

	String create(String quartzJobId);

	JobDTO getByQuartzJobId(String quartzJobId);

	void markAsExecuting(String quartzJobId);

	void markAsCompleted(String quartzJobId);

}
