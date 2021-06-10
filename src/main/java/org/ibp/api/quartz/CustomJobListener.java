package org.ibp.api.quartz;

import org.ibp.api.java.job.JobService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomJobListener implements JobListener {

	@Autowired
	private JobService jobService;

	@Override
	public String getName() {
		return this.getClass().getCanonicalName();
	}

	@Override
	public void jobToBeExecuted(final JobExecutionContext context) {
		final String jobId = this.getJobId(context);
		this.jobService.markAsExecuting(jobId);
	}

	@Override
	public void jobExecutionVetoed(final JobExecutionContext context) {

	}

	@Override
	public void jobWasExecuted(final JobExecutionContext context, final JobExecutionException jobException) {
		final String jobId = this.getJobId(context);
		this.jobService.markAsCompleted(jobId);
	}

	private String getJobId(final JobExecutionContext context) {
		return context.getJobDetail().getKey().getName();
	}

}
