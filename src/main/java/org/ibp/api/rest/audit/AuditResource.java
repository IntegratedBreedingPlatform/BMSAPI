package org.ibp.api.rest.audit;

import org.ibp.api.quartz.ScheduleJobBuilder;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AuditResource {

	@Autowired
	private ScheduleJobBuilder scheduleJobBuilder;

	@RequestMapping(
		value = "/crops/{cropName}/audit/report",
		method = RequestMethod.GET)
	public ResponseEntity<Void> getReport(final HttpServletRequest request,
		@PathVariable final String cropName,
		@RequestParam final String programUUID,
		@RequestParam final Integer gid) throws SchedulerException {

		final JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put("cropName", cropName);
		jobDataMap.put("programUUID", programUUID);
		jobDataMap.put("token", request.getHeader("X-Auth-Token"));
		jobDataMap.put("gid", gid);

		this.scheduleJobBuilder.buildAuditReportJob(jobDataMap);
		return null;
	}

}
