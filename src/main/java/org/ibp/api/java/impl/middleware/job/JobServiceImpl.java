package org.ibp.api.java.impl.middleware.job;

import org.generationcp.middleware.domain.job.JobDTO;
import org.generationcp.middleware.pojos.workbench.job.Job;
import org.ibp.api.java.job.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JobServiceImpl implements JobService {

	@Autowired
	private org.generationcp.middleware.service.api.job.JobService jobService;

	@Override
	public String create(final String quartzJobId) {
		final Job job = this.jobService.create(quartzJobId);
		return job.getQuartzJobId();
	}

	@Override
	public JobDTO getByQuartzJobId(final String quartzJobId) {
		final Job job = this.jobService.getByQuartzJobId(quartzJobId);
		return new JobDTO(job.getId(), job.getQuartzJobId(), job.getStatus(), job.getStartedDate(), job.getCompletedDate());
	}

	@Override
	public void markAsExecuting(final String quartzJobId) {
		this.jobService.markAsExecuting(quartzJobId);
	}

	@Override
	public void markAsCompleted(final String quartzJobId) {
		this.jobService.markAsCompleted(quartzJobId);
	}

}
