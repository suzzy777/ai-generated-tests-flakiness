package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.elasticjob.lite.api.strategy.JobInstance;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@RunWith(MockitoJUnitRunner.class)
public class JobRegistry_shutdown_10_0_Test {

    private JobRegistry jobRegistry;

    @Mock
    private JobScheduleController mockScheduleController;

    @Mock
    private CoordinatorRegistryCenter mockRegCenter;

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();
        // Use reflection to access private fields and set them
        Field schedulerMapField = JobRegistry.class.getDeclaredField("schedulerMap");
        schedulerMapField.setAccessible(true);
        Map<String, JobScheduleController> schedulerMap = new ConcurrentHashMap<>();
        schedulerMapField.set(jobRegistry, schedulerMap);
        Field regCenterMapField = JobRegistry.class.getDeclaredField("regCenterMap");
        regCenterMapField.setAccessible(true);
        Map<String, CoordinatorRegistryCenter> regCenterMap = new ConcurrentHashMap<>();
        regCenterMapField.set(jobRegistry, regCenterMap);
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        Map<String, JobInstance> jobInstanceMap = new ConcurrentHashMap<>();
        jobInstanceMapField.set(jobRegistry, jobInstanceMap);
        Field jobRunningMapField = JobRegistry.class.getDeclaredField("jobRunningMap");
        jobRunningMapField.setAccessible(true);
        Map<String, Boolean> jobRunningMap = new ConcurrentHashMap<>();
        jobRunningMapField.set(jobRegistry, jobRunningMap);
        Field currentShardingTotalCountMapField = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        currentShardingTotalCountMapField.setAccessible(true);
        Map<String, Integer> currentShardingTotalCountMap = new ConcurrentHashMap<>();
        currentShardingTotalCountMapField.set(jobRegistry, currentShardingTotalCountMap);
    }

    @Test
    public void testShutdownWithExistingJob() throws Exception {
        String jobName = "testJob";
        // Add entries to maps
        Field schedulerMapField = JobRegistry.class.getDeclaredField("schedulerMap");
        schedulerMapField.setAccessible(true);
        Map<String, JobScheduleController> schedulerMap = (Map<String, JobScheduleController>) schedulerMapField.get(jobRegistry);
        schedulerMap.put(jobName, mockScheduleController);
        Field regCenterMapField = JobRegistry.class.getDeclaredField("regCenterMap");
        regCenterMapField.setAccessible(true);
        Map<String, CoordinatorRegistryCenter> regCenterMap = (Map<String, CoordinatorRegistryCenter>) regCenterMapField.get(jobRegistry);
        regCenterMap.put(jobName, mockRegCenter);
        jobRegistry.shutdown(jobName);
        assertFalse(schedulerMap.containsKey(jobName));
        assertFalse(regCenterMap.containsKey(jobName));
        verify(mockScheduleController).shutdown();
        verify(mockRegCenter).evictCacheData("/" + jobName);
    }

    @Test
    public void testShutdownWithNonExistingJob() throws Exception {
        String jobName = "nonExistingJob";
        jobRegistry.shutdown(jobName);
        // No exception should be thrown and maps should remain unchanged
    }
}
