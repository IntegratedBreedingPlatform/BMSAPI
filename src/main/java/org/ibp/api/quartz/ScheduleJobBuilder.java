package org.ibp.api.quartz;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ScheduleJobBuilder {

	@Autowired
	private Scheduler scheduler;

	public void buildAuditReportJob(final JobDataMap jobDataMap) throws SchedulerException {
		final JobDetail jobDetail = this.buildJobDetail(AuditReportJob.class, AuditReportJob.GROUP_NAME, AuditReportJob.DESCRIPTION);
		final Trigger trigger = this.buildTrigger(jobDetail, jobDataMap, AuditReportJob.GROUP_NAME, AuditReportJob.DESCRIPTION);
		this.scheduler.scheduleJob(jobDetail, trigger);
	}

	private Trigger buildTrigger(final JobDetail jobDetail, final JobDataMap jobDataMap, final String groupName, final String description) {
		return TriggerBuilder.newTrigger().forJob(jobDetail)
			.withIdentity(jobDetail.getKey().getName(), groupName)
			.withDescription(description)
			.startNow()
//			.startAt(Timestamp.valueOf(LocalDateTime.now().plusSeconds(10)))
			.usingJobData(jobDataMap)
			.build();
	}

	private JobDetail buildJobDetail(Class<? extends Job> jobClass, final String groupName, final String description) {
		return org.quartz.JobBuilder.newJob(jobClass)
			.withIdentity(UUID.randomUUID().toString(), groupName)
			.withDescription(description)
			.storeDurably()
			.build();
	}

}