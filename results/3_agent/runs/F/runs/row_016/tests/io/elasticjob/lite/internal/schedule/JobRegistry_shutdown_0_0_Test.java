package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.*;

public class JobRegistry_shutdown_0_0_Test {
    private JobRegistry jobRegistry;
    private JobScheduleController jobScheduleController;
    private CoordinatorRegistryCenter coordinatorRegistryCenter;
    
    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();

        // Use reflection to access the private schedulerMap field
        Field schedulerMapField = JobRegistry.class.getDeclaredField("schedulerMap");
        schedulerMapField.setAccessible(true);
        Map<String, JobScheduleController> schedulerMap = (Map<String, JobScheduleController>) schedulerMapField.get(jobRegistry);
        schedulerMap.clear();

        // Use reflection to access the private regCenterMap field
        Field regCenterMapField = JobRegistry.class.getDeclaredField("regCenterMap");
        regCenterMapField.setAccessible(true);
        Map<String, CoordinatorRegistryCenter> regCenterMap = (Map<String, CoordinatorRegistryCenter>) regCenterMapField.get(jobRegistry);
        regCenterMap.clear();

        // Use reflection to access the private jobInstanceMap field
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        Map<String, JobInstance> jobInstanceMap = (Map<String, JobInstance>) jobInstanceMapField.get(jobRegistry);
        jobInstanceMap.clear();

        // Use reflection to access the private jobRunningMap field
        Field jobRunningMapField = JobRegistry.class.getDeclaredField("jobRunningMap");
        jobRunningMapField.setAccessible(true);
        Map<String, Boolean> jobRunningMap = (Map<String, Boolean>) jobRunningMapField.get(jobRegistry);
        jobRunningMap.clear();

        // Use reflection to access the private currentShardingTotalCountMap field
        Field currentShardingTotalCountMapField = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        currentShardingTotalCountMapField.setAccessible(true);
        Map<String, Integer> currentShardingTotalCountMap = (Map<String, Integer>) currentShardingTotalCountMapField.get(jobRegistry);
        currentShardingTotalCountMap.clear();

        // Mock dependencies
        jobScheduleController = mock(JobScheduleController.class);
        coordinatorRegistryCenter = mock(CoordinatorRegistryCenter.class);

        // Populate maps with mock objects
        schedulerMap.put("testJob", jobScheduleController);
        regCenterMap.put("testJob", coordinatorRegistryCenter);
        jobInstanceMap.put("testJob", new JobInstance("testJobInstance"));
        jobRunningMap.put("testJob", true);
        currentShardingTotalCountMap.put("testJob", 1);
    }

    @Test
    public void testShutdown() {
        jobRegistry.shutdown("testJob");

        // Verify that shutdown was called on the JobScheduleController
        verify(jobScheduleController).shutdown();

        // Verify that evictCacheData was called on the CoordinatorRegistryCenter
        verify(coordinatorRegistryCenter).evictCacheData("/testJob");
    }
}
