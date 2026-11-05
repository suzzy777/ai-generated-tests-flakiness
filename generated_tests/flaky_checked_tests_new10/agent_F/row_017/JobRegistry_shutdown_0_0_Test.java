package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class JobRegistry_shutdown_0_0_Test {

    private JobRegistry jobRegistry;

    @Mock
    private JobScheduleController jobScheduleController;

    @Mock
    private CoordinatorRegistryCenter coordinatorRegistryCenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        jobRegistry = JobRegistry.getInstance();
        setPrivateField("schedulerMap", jobRegistry, new ConcurrentHashMap<String, JobScheduleController>());
        setPrivateField("regCenterMap", jobRegistry, new ConcurrentHashMap<String, CoordinatorRegistryCenter>());
        setPrivateField("jobInstanceMap", jobRegistry, new ConcurrentHashMap<String, Object>());
        setPrivateField("jobRunningMap", jobRegistry, new ConcurrentHashMap<String, Boolean>());
        setPrivateField("currentShardingTotalCountMap", jobRegistry, new ConcurrentHashMap<String, Integer>());
    }

    @Test
    public void testShutdown() throws Exception {
        String jobName = "testJob";

        // Setup mock behavior
        Map<String, JobScheduleController> schedulerMap = getPrivateField("schedulerMap", jobRegistry);
        schedulerMap.put(jobName, jobScheduleController);
        Map<String, CoordinatorRegistryCenter> regCenterMap = getPrivateField("regCenterMap", jobRegistry);
        regCenterMap.put(jobName, coordinatorRegistryCenter);
        Map<String, Object> jobInstanceMap = getPrivateField("jobInstanceMap", jobRegistry);
        jobInstanceMap.put(jobName, new Object());
        Map<String, Boolean> jobRunningMap = getPrivateField("jobRunningMap", jobRegistry);
        jobRunningMap.put(jobName, true);
        Map<String, Integer> currentShardingTotalCountMap = getPrivateField("currentShardingTotalCountMap", jobRegistry);
        currentShardingTotalCountMap.put(jobName, 1);

        // Call the method under test
        jobRegistry.shutdown(jobName);

        // Verify interactions
        Mockito.verify(jobScheduleController).shutdown();
        Mockito.verify(coordinatorRegistryCenter).evictCacheData("/" + jobName);

        // Assert that maps are empty
        assertTrue(schedulerMap.isEmpty());
        assertTrue(regCenterMap.isEmpty());
        assertTrue(jobInstanceMap.isEmpty());
        assertTrue(jobRunningMap.isEmpty());
        assertTrue(currentShardingTotalCountMap.isEmpty());
    }

    private void setPrivateField(String fieldName, Object target, Object value) throws Exception {
        Field field = JobRegistry.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(String fieldName, Object target) throws Exception {
        Field field = JobRegistry.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }
}
