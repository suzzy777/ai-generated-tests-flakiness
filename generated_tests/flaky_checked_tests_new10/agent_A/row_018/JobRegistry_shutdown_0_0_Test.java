package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
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
        jobScheduleController = mock(JobScheduleController.class);
        coordinatorRegistryCenter = mock(CoordinatorRegistryCenter.class);

        Field schedulerMapField = JobRegistry.class.getDeclaredField("schedulerMap");
        schedulerMapField.setAccessible(true);
        ((ConcurrentHashMap<String, JobScheduleController>) schedulerMapField.get(jobRegistry)).put("testJob", jobScheduleController);

        Field regCenterMapField = JobRegistry.class.getDeclaredField("regCenterMap");
        regCenterMapField.setAccessible(true);
        ((ConcurrentHashMap<String, CoordinatorRegistryCenter>) regCenterMapField.get(jobRegistry)).put("testJob", coordinatorRegistryCenter);

        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        ((ConcurrentHashMap<String, JobInstance>) jobInstanceMapField.get(jobRegistry)).put("testJob", mock(JobInstance.class));

        Field jobRunningMapField = JobRegistry.class.getDeclaredField("jobRunningMap");
        jobRunningMapField.setAccessible(true);
        ((ConcurrentHashMap<String, Boolean>) jobRunningMapField.get(jobRegistry)).put("testJob", true);

        Field currentShardingTotalCountMapField = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        currentShardingTotalCountMapField.setAccessible(true);
        ((ConcurrentHashMap<String, Integer>) currentShardingTotalCountMapField.get(jobRegistry)).put("testJob", 1);
    }

    @Test
    public void testShutdown() {
        jobRegistry.shutdown("testJob");

        verify(jobScheduleController).shutdown();
        verify(coordinatorRegistryCenter).evictCacheData("/testJob");

        assertFalse(getSchedulerMap().containsKey("testJob"));
        assertFalse(getRegCenterMap().containsKey("testJob"));
        assertFalse(getJobInstanceMap().containsKey("testJob"));
        assertFalse(getJobRunningMap().containsKey("testJob"));
        assertFalse(getCurrentShardingTotalCountMap().containsKey("testJob"));
    }

    private ConcurrentHashMap<String, JobScheduleController> getSchedulerMap() {
        return getMapField("schedulerMap");
    }

    private ConcurrentHashMap<String, CoordinatorRegistryCenter> getRegCenterMap() {
        return getMapField("regCenterMap");
    }

    private ConcurrentHashMap<String, JobInstance> getJobInstanceMap() {
        return getMapField("jobInstanceMap");
    }

    private ConcurrentHashMap<String, Boolean> getJobRunningMap() {
        return getMapField("jobRunningMap");
    }

    private ConcurrentHashMap<String, Integer> getCurrentShardingTotalCountMap() {
        return getMapField("currentShardingTotalCountMap");
    }

    @SuppressWarnings("unchecked")
    private <T> ConcurrentHashMap<String, T> getMapField(String fieldName) {
        try {
            Field field = JobRegistry.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (ConcurrentHashMap<String, T>) field.get(jobRegistry);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
