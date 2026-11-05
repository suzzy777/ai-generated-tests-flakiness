package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

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

        originalSchedulerMap = getField(jobRegistry, "schedulerMap");
        originalRegCenterMap = getField(jobRegistry, "regCenterMap");
        originalJobInstanceMap = getField(jobRegistry, "jobInstanceMap");
        originalJobRunningMap = getField(jobRegistry, "jobRunningMap");
        originalCurrentShardingTotalCountMap = getField(jobRegistry, "currentShardingTotalCountMap");

        setField(jobRegistry, "schedulerMap", new ConcurrentHashMap<>());
        setField(jobRegistry, "regCenterMap", new ConcurrentHashMap<>());
        setField(jobRegistry, "jobInstanceMap", new ConcurrentHashMap<>());
        setField(jobRegistry, "jobRunningMap", new ConcurrentHashMap<>());
        setField(jobRegistry, "currentShardingTotalCountMap", new ConcurrentHashMap<>());
    }

    @After
    public void tearDown() throws Exception {
        setField(jobRegistry, "schedulerMap", originalSchedulerMap);
        setField(jobRegistry, "regCenterMap", originalRegCenterMap);
        setField(jobRegistry, "jobInstanceMap", originalJobInstanceMap);
        setField(jobRegistry, "jobRunningMap", originalJobRunningMap);
        setField(jobRegistry, "currentShardingTotalCountMap", originalCurrentShardingTotalCountMap);
    }

    @Test
    public void testShutdown() throws Exception {
        String jobName = "testJob";

        JobScheduleController mockController = Mockito.mock(JobScheduleController.class);
        CoordinatorRegistryCenter mockRegCenter = Mockito.mock(CoordinatorRegistryCenter.class);

        Map<String, JobScheduleController> schedulerMap = getField(jobRegistry, "schedulerMap");
        Map<String, CoordinatorRegistryCenter> regCenterMap = getField(jobRegistry, "regCenterMap");
        Map<String, JobInstance> jobInstanceMap = getField(jobRegistry, "jobInstanceMap");
        Map<String, Boolean> jobRunningMap = getField(jobRegistry, "jobRunningMap");
        Map<String, Integer> currentShardingTotalCountMap = getField(jobRegistry, "currentShardingTotalCountMap");

        schedulerMap.put(jobName, mockController);
        regCenterMap.put(jobName, mockRegCenter);
        jobInstanceMap.put(jobName, new JobInstance(jobName));
        jobRunningMap.put(jobName, true);
        currentShardingTotalCountMap.put(jobName, 1);

        jobRegistry.shutdown(jobName);

        Mockito.verify(mockController).shutdown();
        Mockito.verify(mockRegCenter).evictCacheData("/" + jobName);

        assertFalse(schedulerMap.containsKey(jobName));
        assertFalse(regCenterMap.containsKey(jobName));
        assertFalse(jobInstanceMap.containsKey(jobName));
        assertFalse(jobRunningMap.containsKey(jobName));
        assertFalse(currentShardingTotalCountMap.containsKey(jobName));
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(object);
    }

    private void setField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}
