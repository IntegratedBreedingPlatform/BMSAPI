package org.ibp.api.quartz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.ibp.api.rest.germplasm.GermplasmRestClient;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditReportJob implements Job {

    public static final String GROUP_NAME = "audit-report-job";
    public static final String DESCRIPTION = "Process audit report";

    @Autowired
    private GermplasmRestClient germplasmRestClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final ResponseEntity<GermplasmDto> germplasmDtoById = this.germplasmRestClient.getGermplasmDtoById(dataMap.getString("cropName"),
            dataMap.getInt("gid"),
            dataMap.getString("programUUID"),
            dataMap.getString("token"));

        try {
            System.out.println("JOB executed response: " + this.objectMapper.writeValueAsString(germplasmDtoById.getBody()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
