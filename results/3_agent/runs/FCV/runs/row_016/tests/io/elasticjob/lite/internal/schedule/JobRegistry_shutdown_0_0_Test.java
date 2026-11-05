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
    private JobScheduleController mockJobScheduleController;
    private CoordinatorRegistryCenter mockCoordinatorRegistryCenter;
    private Map<String, JobScheduleController> originalSchedulerMap;
    private Map<String, CoordinatorRegistryCenter> originalRegCenterMap;
    private Map<String, JobInstance> originalJobInstanceMap;
    private Map<String, Boolean> originalJobRunningMap;
    private Map<String, Integer> originalCurrentShardingTotalCountMap;

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();

        // Use reflection to access and backup private fields
        Field schedulerMapField = JobRegistry.class.getDeclaredField("schedulerMap");
        schedulerMapField.setAccessible(true);
        originalSchedulerMap = new ConcurrentHashMap<>((Map<String, JobScheduleController>) schedulerMapField.get(jobRegistry));

        Field regCenterMapField = JobRegistry.class.getDeclaredField("regCenterMap");
        regCenterMapField.setAccessible(true);
        originalRegCenterMap = new ConcurrentHashMap<>((Map<String, CoordinatorRegistryCenter>) regCenterMapField.get(jobRegistry));

        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        originalJobInstanceMap = new ConcurrentHashMap<>((Map<String, JobInstance>) jobInstanceMapField.get(jobRegistry));

        Field jobRunningMapField = JobRegistry.class.getDeclaredField("jobRunningMap");
        jobRunningMapField.setAccessible(true);
        originalJobRunningMap = new ConcurrentHashMap<>((Map<String, Boolean>) jobRunningMapField.get(jobRegistry));

        Field currentShardingTotalCountMapField = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        currentShardingTotalCountMapField.setAccessible(true);
        originalCurrentShardingTotalCountMap = new ConcurrentHashMap<>((Map<String, Integer>) currentShardingTotalCountMapField.get(jobRegistry));

        // Create mock objects
        mockJobScheduleController = Mockito.mock(JobScheduleController.class);
        mockCoordinatorRegistryCenter = Mockito.mock(CoordinatorRegistryCenter.class);

        // Set up initial state
        ((Map<String, JobScheduleController>) schedulerMapField.get(jobRegistry)).put("testJob", mockJobScheduleController);
        ((Map<String, CoordinatorRegistryCenter>) regCenterMapField.get(jobRegistry)).put("testJob", mockCoordinatorRegistryCenter);
        ((Map<String, JobInstance>) jobInstanceMapField.get(jobRegistry)).put("testJob", new JobInstance("testJobInstance"));
        ((Map<String, Boolean>) jobRunningMapField.get(jobRegistry)).put("testJob", true);
        ((Map<String, Integer>) currentShardingTotalCountMapField.get(jobRegistry)).put("testJob", 1);
    }

    @After
    public void tearDown() throws Exception {
        // Restore original state
        Field schedulerMapField = JobRegistry.class.getDeclaredField("schedulerMap");
        schedulerMapField.setAccessible(true);
        schedulerMapField.set(jobRegistry, originalSchedulerMap);

        Field regCenterMapField = JobRegistry.class.getDeclaredField("regCenterMap");
        regCenterMapField.setAccessible(true);
        regCenterMapField.set(jobRegistry, originalRegCenterMap);

        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        jobInstanceMapField.set(jobRegistry, originalJobInstanceMap);

        Field jobRunningMapField = JobRegistry.class.getDeclaredField("jobRunningMap");
        jobRunningMapField.setAccessible(true);
        jobRunningMapField.set(jobRegistry, originalJobRunningMap);

        Field currentShardingTotalCountMapField = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        currentShardingTotalCountMapField.setAccessible(true);
        currentShardingTotalCountMapField.set(jobRegistry, originalCurrentShardingTotalCountMap);
    }

    @Test
    public void testShutdown() {
        jobRegistry.shutdown("testJob");

        Mockito.verify(mockJobScheduleController).shutdown();
        Mockito.verify(mockCoordinatorRegistryCenter).evictCacheData("/testJob");

        assertFalse(originalSchedulerMap.containsKey("testJob"));
        assertFalse(originalRegCenterMap.containsKey("testJob"));
        assertFalse(originalJobInstanceMap.containsKey("testJob"));
        assertFalse(originalJobRunningMap.containsKey("testJob"));
        assertFalse(originalCurrentShardingTotalCountMap.containsKey("testJob"));
    }
}
