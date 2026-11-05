package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class JobRegistry_shutdown_0_0_Test {
    private JobRegistry jobRegistry;
    private Map<String, JobScheduleController> originalSchedulerMap;
    private Map<String, CoordinatorRegistryCenter> originalRegCenterMap;
    private Map<String, JobInstance> originalJobInstanceMap;
    private Map<String, Boolean> originalJobRunningMap;
    private Map<String, Integer> originalCurrentShardingTotalCountMap;

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();
        
        // Save original maps
        originalSchedulerMap = new ConcurrentHashMap<>((Map<String, JobScheduleController>) getPrivateField(jobRegistry, "schedulerMap"));
        originalRegCenterMap = new ConcurrentHashMap<>((Map<String, CoordinatorRegistryCenter>) getPrivateField(jobRegistry, "regCenterMap"));
        originalJobInstanceMap = new ConcurrentHashMap<>((Map<String, JobInstance>) getPrivateField(jobRegistry, "jobInstanceMap"));
        originalJobRunningMap = new ConcurrentHashMap<>((Map<String, Boolean>) getPrivateField(jobRegistry, "jobRunningMap"));
        originalCurrentShardingTotalCountMap = new ConcurrentHashMap<>((Map<String, Integer>) getPrivateField(jobRegistry, "currentShardingTotalCountMap"));
    }

    @After
    public void tearDown() throws Exception {
        // Restore original maps
        setPrivateField(jobRegistry, "schedulerMap", originalSchedulerMap);
        setPrivateField(jobRegistry, "regCenterMap", originalRegCenterMap);
        setPrivateField(jobRegistry, "jobInstanceMap", originalJobInstanceMap);
        setPrivateField(jobRegistry, "jobRunningMap", originalJobRunningMap);
        setPrivateField(jobRegistry, "currentShardingTotalCountMap", originalCurrentShardingTotalCountMap);
    }

    @Test
    public void testShutdownWithExistingJob() throws Exception {
        String jobName = "testJob";
        
        JobScheduleController mockScheduleController = mock(JobScheduleController.class);
        CoordinatorRegistryCenter mockRegCenter = mock(CoordinatorRegistryCenter.class);
        
        // Set up mock data
        Map<String, JobScheduleController> schedulerMap = new ConcurrentHashMap<>();
        schedulerMap.put(jobName, mockScheduleController);
        setPrivateField(jobRegistry, "schedulerMap", schedulerMap);

        Map<String, CoordinatorRegistryCenter> regCenterMap = new ConcurrentHashMap<>();
        regCenterMap.put(jobName, mockRegCenter);
        setPrivateField(jobRegistry, "regCenterMap", regCenterMap);

        Map<String, JobInstance> jobInstanceMap = new ConcurrentHashMap<>();
        jobInstanceMap.put(jobName, mock(JobInstance.class));
        setPrivateField(jobRegistry, "jobInstanceMap", jobInstanceMap);

        Map<String, Boolean> jobRunningMap = new ConcurrentHashMap<>();
        jobRunningMap.put(jobName, true);
        setPrivateField(jobRegistry, "jobRunningMap", jobRunningMap);

        Map<String, Integer> currentShardingTotalCountMap = new ConcurrentHashMap<>();
        currentShardingTotalCountMap.put(jobName, 1);
        setPrivateField(jobRegistry, "currentShardingTotalCountMap", currentShardingTotalCountMap);

        jobRegistry.shutdown(jobName);

        verify(mockScheduleController).shutdown();
        verify(mockRegCenter).evictCacheData("/" + jobName);
        assertFalse(schedulerMap.containsKey(jobName));
        assertFalse(regCenterMap.containsKey(jobName));
        assertFalse(jobInstanceMap.containsKey(jobName));
        assertFalse(jobRunningMap.containsKey(jobName));
        assertFalse(currentShardingTotalCountMap.containsKey(jobName));
    }

    @Test
    public void testShutdownWithNonExistingJob() throws Exception {
        String jobName = "nonExistingJob";

        jobRegistry.shutdown(jobName);

        // Ensure no exceptions and nothing is removed
        assertEquals(originalSchedulerMap, getPrivateField(jobRegistry, "schedulerMap"));
        assertEquals(originalRegCenterMap, getPrivateField(jobRegistry, "regCenterMap"));
        assertEquals(originalJobInstanceMap, getPrivateField(jobRegistry, "jobInstanceMap"));
        assertEquals(originalJobRunningMap, getPrivateField(jobRegistry, "jobRunningMap"));
        assertEquals(originalCurrentShardingTotalCountMap, getPrivateField(jobRegistry, "currentShardingTotalCountMap"));
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object object, String fieldName) throws Exception {
        java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(object);
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}
