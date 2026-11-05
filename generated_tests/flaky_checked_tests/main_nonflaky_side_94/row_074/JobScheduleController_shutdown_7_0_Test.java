package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.internal.schedule.JobScheduleController;
import io.elasticjob.lite.exception.JobSystemException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import lombok.RequiredArgsConstructor;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

@RunWith(MockitoJUnitRunner.class)
public class JobScheduleController_shutdown_7_0_Test {

    @Mock
    private Scheduler scheduler;

    @Mock
    private JobDetail jobDetail;

    private String triggerIdentity = "testTrigger";

    @InjectMocks
    private JobScheduleController jobScheduleController;

    @Before
    public void setUp() {
        jobScheduleController = new JobScheduleController(scheduler, jobDetail, triggerIdentity);
    }

    @Test
    public void testShutdownWhenSchedulerIsNotShutdown() throws SchedulerException {
        when(scheduler.isShutdown()).thenReturn(false);
        jobScheduleController.shutdown();
        verify(scheduler, times(1)).shutdown();
    }

    @Test
    public void testShutdownWhenSchedulerIsAlreadyShutdown() throws SchedulerException {
        when(scheduler.isShutdown()).thenReturn(true);
        jobScheduleController.shutdown();
        verify(scheduler, never()).shutdown();
    }

    @Test(expected = JobSystemException.class)
    public void testShutdownThrowsJobSystemException() throws SchedulerException {
        when(scheduler.isShutdown()).thenReturn(false);
        doThrow(new SchedulerException()).when(scheduler).shutdown();
        jobScheduleController.shutdown();
    }
}
