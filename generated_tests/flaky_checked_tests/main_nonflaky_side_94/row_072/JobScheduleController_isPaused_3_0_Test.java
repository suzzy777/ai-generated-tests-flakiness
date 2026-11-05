package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.internal.schedule.JobScheduleController;
import io.elasticjob.lite.exception.JobSystemException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import lombok.RequiredArgsConstructor;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.TriggerBuilder;

@RunWith(MockitoJUnitRunner.class)
public class JobScheduleController_isPaused_3_0_Test {

    @Mock
    private Scheduler scheduler;

    @Mock
    private TriggerKey triggerKey;

    @InjectMocks
    private JobScheduleController jobScheduleController;

    private final String triggerIdentity = "testTrigger";

    @Before
    public void setUp() {
        jobScheduleController = new JobScheduleController(scheduler, null, triggerIdentity);
    }

    @Test
    public void testIsPausedWhenSchedulerIsNotShutdownAndTriggerIsPaused() throws SchedulerException {
        when(scheduler.isShutdown()).thenReturn(false);
        when(scheduler.getTriggerState(new TriggerKey(triggerIdentity))).thenReturn(Trigger.TriggerState.PAUSED);
        assertTrue(jobScheduleController.isPaused());
    }

    @Test
    public void testIsPausedWhenSchedulerIsShutdown() throws SchedulerException {
        when(scheduler.isShutdown()).thenReturn(true);
        assertFalse(jobScheduleController.isPaused());
    }

    @Test
    public void testIsPausedWhenSchedulerIsNotShutdownAndTriggerIsNotPaused() throws SchedulerException {
        when(scheduler.isShutdown()).thenReturn(false);
        when(scheduler.getTriggerState(new TriggerKey(triggerIdentity))).thenReturn(Trigger.TriggerState.NORMAL);
        assertFalse(jobScheduleController.isPaused());
    }

    @Test(expected = JobSystemException.class)
    public void testIsPausedThrowsJobSystemException() throws SchedulerException {
        when(scheduler.isShutdown()).thenReturn(false);
        when(scheduler.getTriggerState(new TriggerKey(triggerIdentity))).thenThrow(new SchedulerException());
        jobScheduleController.isPaused();
    }
}
