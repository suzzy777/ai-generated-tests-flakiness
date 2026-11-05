package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.exception.JobSystemException;
import io.elasticjob.lite.internal.schedule.JobScheduleController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.*;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import lombok.RequiredArgsConstructor;

@RunWith(MockitoJUnitRunner.class)
public class JobScheduleController_rescheduleJob_1_0_Test {

    @Mock
    private Scheduler scheduler;

    @Mock
    private JobDetail jobDetail;

    @InjectMocks
    private JobScheduleController jobScheduleController;

    private static final String TRIGGER_IDENTITY = "testTrigger";

    @Before
    public void setUp() {
        jobScheduleController = new JobScheduleController(scheduler, jobDetail, TRIGGER_IDENTITY);
    }

    @Test
    public void testRescheduleJobWhenSchedulerIsShutdown() throws Exception {
        when(scheduler.isShutdown()).thenReturn(true);
        jobScheduleController.rescheduleJob("0/5 * * * * ?");
        verify(scheduler, never()).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
    }

    @Test
    public void testRescheduleJobWhenTriggerIsNull() throws Exception {
        when(scheduler.isShutdown()).thenReturn(false);
        when(scheduler.getTrigger(any(TriggerKey.class))).thenReturn(null);
        jobScheduleController.rescheduleJob("0/5 * * * * ?");
        verify(scheduler, never()).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
    }

    @Test
    public void testRescheduleJobWhenCronExpressionIsDifferent() throws Exception {
        CronTrigger mockTrigger = mock(CronTrigger.class);
        when(scheduler.isShutdown()).thenReturn(false);
        when(scheduler.getTrigger(any(TriggerKey.class))).thenReturn(mockTrigger);
        when(mockTrigger.getCronExpression()).thenReturn("0 0 12 * * ?");
        jobScheduleController.rescheduleJob("0/5 * * * * ?");
        verify(scheduler, times(1)).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
    }

    @Test
    public void testRescheduleJobWhenCronExpressionIsSame() throws Exception {
        CronTrigger mockTrigger = mock(CronTrigger.class);
        when(scheduler.isShutdown()).thenReturn(false);
        when(scheduler.getTrigger(any(TriggerKey.class))).thenReturn(mockTrigger);
        when(mockTrigger.getCronExpression()).thenReturn("0/5 * * * * ?");
        jobScheduleController.rescheduleJob("0/5 * * * * ?");
        verify(scheduler, never()).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
    }

    @Test(expected = JobSystemException.class)
    public void testRescheduleJobThrowsSchedulerException() throws Exception {
        when(scheduler.getTrigger(any(TriggerKey.class))).thenThrow(SchedulerException.class);
        jobScheduleController.rescheduleJob("0/5 * * * * ?");
    }

    @Test
    public void testCreateTriggerUsingReflection() throws Exception {
        Method createTriggerMethod = JobScheduleController.class.getDeclaredMethod("createTrigger", String.class);
        createTriggerMethod.setAccessible(true);
        CronTrigger trigger = (CronTrigger) createTriggerMethod.invoke(jobScheduleController, "0/5 * * * * ?");
        assertEquals(TRIGGER_IDENTITY, trigger.getKey().getName());
        assertEquals("0/5 * * * * ?", trigger.getCronExpression());
    }
}
