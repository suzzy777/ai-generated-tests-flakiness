package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class JobRegistry_shutdown_0_0_Test {

    private JobRegistry jobRegistry;
    private Map<String, JobScheduleController> schedulerMapBackup;
    private Map<String, CoordinatorRegistryCenter> regCenterMapBackup;
    private Map<String, JobInstance> jobInstanceMapBackup;
    private Map<String, Boolean> jobRunningMapBackup;
    private Map<String, Integer> currentShardingTotalCountMapBackup;

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();
        schedulerMapBackup = new ConcurrentHashMap<>((Map<String, JobScheduleController>) getMapField(jobRegistry, "schedulerMap"));
        regCenterMapBackup = new ConcurrentHashMap<>((Map<String, CoordinatorRegistryCenter>) getMapField(jobRegistry, "regCenterMap"));
        jobInstanceMapBackup = new ConcurrentHashMap<>((Map<String, JobInstance>) getMapField(jobRegistry, "jobInstanceMap"));
        jobRunningMapBackup = new ConcurrentHashMap<>((Map<String, Boolean>) getMapField(jobRegistry, "jobRunningMap"));
        currentShardingTotalCountMapBackup = new ConcurrentHashMap<>((Map<String, Integer>) getMapField(jobRegistry, "currentShardingTotalCountMap"));
    }

    @After
    public void tearDown() throws Exception {
        setMapField(jobRegistry, "schedulerMap", schedulerMapBackup);
        setMapField(jobRegistry, "regCenterMap", regCenterMapBackup);
        setMapField(jobRegistry, "jobInstanceMap", jobInstanceMapBackup);
        setMapField(jobRegistry, "jobRunningMap", jobRunningMapBackup);
        setMapField(jobRegistry, "currentShardingTotalCountMap", currentShardingTotalCountMapBackup);
    }

    @Test
    public void testShutdown() throws Exception {
        String jobName = "testJob";

        JobScheduleController mockScheduleController = mock(JobScheduleController.class);
        CoordinatorRegistryCenter mockRegCenter = mock(CoordinatorRegistryCenter.class);

        Map<String, JobScheduleController> schedulerMap = getMapField(jobRegistry, "schedulerMap");
        Map<String, CoordinatorRegistryCenter> regCenterMap = getMapField(jobRegistry, "regCenterMap");
        Map<String, JobInstance> jobInstanceMap = getMapField(jobRegistry, "jobInstanceMap");
        Map<String, Boolean> jobRunningMap = getMapField(jobRegistry, "jobRunningMap");
        Map<String, Integer> currentShardingTotalCountMap = getMapField(jobRegistry, "currentShardingTotalCountMap");

        schedulerMap.put(jobName, mockScheduleController);
        regCenterMap.put(jobName, mockRegCenter);
        jobInstanceMap.put(jobName, mock(JobInstance.class));
        jobRunningMap.put(jobName, true);
        currentShardingTotalCountMap.put(jobName, 1);

        jobRegistry.shutdown(jobName);

        verify(mockScheduleController).shutdown();
        verify(mockRegCenter).evictCacheData("/" + jobName);

        assertFalse(schedulerMap.containsKey(jobName));
        assertFalse(regCenterMap.containsKey(jobName));
        assertFalse(jobInstanceMap.containsKey(jobName));
        assertFalse(jobRunningMap.containsKey(jobName));
        assertFalse(currentShardingTotalCountMap.containsKey(jobName));
    }

    @SuppressWarnings("unchecked")
    private <T> T getMapField(Object instance, String fieldName) throws Exception {
        java.lang.reflect.Field field = JobRegistry.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(instance);
    }

    private void setMapField(Object instance, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = JobRegistry.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }
}
