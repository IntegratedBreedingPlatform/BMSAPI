package org.ibp.api.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.springframework.stereotype.Component;

@Component
public class CustomTriggerListener implements TriggerListener {

	@Override
	public String getName() {
		return this.getClass().getCanonicalName();
	}

	@Override
	public void triggerFired(final Trigger trigger, final JobExecutionContext context) {
		System.out.println("triggerFired");
	}

	@Override
	public boolean vetoJobExecution(final Trigger trigger, final JobExecutionContext context) {
		return false;
	}

	@Override
	public void triggerMisfired(final Trigger trigger) {

	}

	@Override
	public void triggerComplete(final Trigger trigger, final JobExecutionContext context,
		final Trigger.CompletedExecutionInstruction triggerInstructionCode) {
		System.out.println("triggerComplete");
	}
}
