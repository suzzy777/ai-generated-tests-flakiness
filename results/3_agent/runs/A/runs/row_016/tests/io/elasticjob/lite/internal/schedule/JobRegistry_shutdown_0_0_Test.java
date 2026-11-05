package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class JobRegistry_shutdown_0_0_Test {

    private JobRegistry jobRegistry;
    private JobScheduleController jobScheduleController;
    private CoordinatorRegistryCenter coordinatorRegistryCenter;

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();

        // Use reflection to reset the singleton instance for testing
        Field instanceField = JobRegistry.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, jobRegistry);

        jobScheduleController = mock(JobScheduleController.class);
        coordinatorRegistryCenter = mock(CoordinatorRegistryCenter.class);

        setPrivateField(jobRegistry, "schedulerMap", new ConcurrentHashMap<String, JobScheduleController>());
        setPrivateField(jobRegistry, "regCenterMap", new ConcurrentHashMap<String, CoordinatorRegistryCenter>());
        setPrivateField(jobRegistry, "jobInstanceMap", new ConcurrentHashMap<String, JobInstance>());
        setPrivateField(jobRegistry, "jobRunningMap", new ConcurrentHashMap<String, Boolean>());
        setPrivateField(jobRegistry, "currentShardingTotalCountMap", new ConcurrentHashMap<String, Integer>());
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    public void testShutdownWithExistingJob() {
        String jobName = "testJob";
        Map<String, JobScheduleController> schedulerMap = getPrivateField(jobRegistry, "schedulerMap"); throws Exception throws Exception { throws Exception throws Exception; { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception {
        Map<String, CoordinatorRegistryCenter> regCenterMap = getPrivateField(jobRegistry, "regCenterMap"); throws Exception throws Exception { throws Exception throws Exception; { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception { throws Exception {

        schedulerMap.put(jobName, jobScheduleController);
        regCenterMap.put(jobName, coordinatorRegistryCenter);

        jobRegistry.shutdown(jobName);

        verify(jobScheduleController).shutdown();
        verify(coordinatorRegistryCenter).evictCacheData("/" + jobName);

        assertFalse(schedulerMap.containsKey(jobName));
        assertFalse(regCenterMap.containsKey(jobName));
    }

    @Test
    public void testShutdownWithNonExistingJob() {
        String jobName = "nonExistingJob";

        jobRegistry.shutdown(jobName);

        verify(jobScheduleController, never()).shutdown();
        verify(coordinatorRegistryCenter, never()).evictCacheData(anyString());
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }
}
