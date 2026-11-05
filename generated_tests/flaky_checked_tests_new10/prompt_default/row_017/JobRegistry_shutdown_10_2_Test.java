package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.schedule.JobScheduleController;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.elasticjob.lite.api.strategy.JobInstance;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.Map;
import org.quartz.Scheduler;
import org.quartz.JobDetail;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class JobRegistry_shutdown_10_2_Test {

    private JobRegistry jobRegistry;

    private CoordinatorRegistryCenter mockRegCenter;

    @Mock
    private Scheduler mockScheduler;

    @Mock
    private JobDetail mockJobDetail;

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();
        // Use reflection to access private fields
        java.lang.reflect.Field schedulerMapField = JobRegistry.class.getDeclaredField("schedulerMap");
        schedulerMapField.setAccessible(true);
        ConcurrentHashMap<String, JobScheduleController> schedulerMap = new ConcurrentHashMap<>();
        schedulerMapField.set(jobRegistry, schedulerMap);
        java.lang.reflect.Field regCenterMapField = JobRegistry.class.getDeclaredField("regCenterMap");
        regCenterMapField.setAccessible(true);
        ConcurrentHashMap<String, CoordinatorRegistryCenter> regCenterMap = new ConcurrentHashMap<>();
        regCenterMapField.set(jobRegistry, regCenterMap);
        java.lang.reflect.Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        ConcurrentHashMap<String, Object> jobInstanceMap = new ConcurrentHashMap<>();
        jobInstanceMapField.set(jobRegistry, jobInstanceMap);
        java.lang.reflect.Field jobRunningMapField = JobRegistry.class.getDeclaredField("jobRunningMap");
        jobRunningMapField.setAccessible(true);
        ConcurrentHashMap<String, Boolean> jobRunningMap = new ConcurrentHashMap<>();
        jobRunningMapField.set(jobRegistry, jobRunningMap);
        java.lang.reflect.Field currentShardingTotalCountMapField = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        currentShardingTotalCountMapField.setAccessible(true);
        ConcurrentHashMap<String, Integer> currentShardingTotalCountMap = new ConcurrentHashMap<>();
        currentShardingTotalCountMapField.set(jobRegistry, currentShardingTotalCountMap);
        // Mock dependencies
        mockRegCenter = mock(CoordinatorRegistryCenter.class);
        // Add mock objects to the maps
        JobScheduleController mockJobScheduleController = new JobScheduleController(mockScheduler, mockJobDetail, "testJob");
        schedulerMap.put("testJob", mockJobScheduleController);
        regCenterMap.put("testJob", mockRegCenter);
        jobInstanceMap.put("testJob", new Object());
        jobRunningMap.put("testJob", true);
        currentShardingTotalCountMap.put("testJob", 1);
    }

    @Test
    public void testShutdown() throws Exception {
        jobRegistry.shutdown("testJob");
        // Verify that the schedulerMap no longer contains the job
        assertNull(getMapValue("schedulerMap", "testJob"));
        // Verify that the regCenterMap no longer contains the job
        assertNull(getMapValue("regCenterMap", "testJob"));
        // Verify that the jobInstanceMap no longer contains the job
        assertNull(getMapValue("jobInstanceMap", "testJob"));
        // Verify that the jobRunningMap no longer contains the job
        assertNull(getMapValue("jobRunningMap", "testJob"));
        // Verify that the currentShardingTotalCountMap no longer contains the job
        assertNull(getMapValue("currentShardingTotalCountMap", "testJob"));
        // Verify that the evictCacheData method was called on the mockRegCenter
        verify(mockRegCenter).evictCacheData("/testJob");
    }

    private Object getMapValue(String mapName, String key) throws Exception {
        java.lang.reflect.Field mapField = JobRegistry.class.getDeclaredField(mapName);
        mapField.setAccessible(true);
        ConcurrentHashMap<String, ?> map = (ConcurrentHashMap<String, ?>) mapField.get(jobRegistry);
        return map.get(key);
    }
}
