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
import static org.mockito.Mockito.*;

public class JobRegistry_shutdown_0_0_Test {

    private JobRegistry jobRegistry;
    private JobScheduleController jobScheduleController;
    private CoordinatorRegistryCenter coordinatorRegistryCenter;
    private Map<String, JobScheduleController> originalSchedulerMap;
    private Map<String, CoordinatorRegistryCenter> originalRegCenterMap;
    private Map<String, JobInstance> originalJobInstanceMap;
    private Map<String, Boolean> originalJobRunningMap;
    private Map<String, Integer> originalCurrentShardingTotalCountMap;

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();

        // Save original state
        originalSchedulerMap = getFieldValue("schedulerMap");
        originalRegCenterMap = getFieldValue("regCenterMap");
        originalJobInstanceMap = getFieldValue("jobInstanceMap");
        originalJobRunningMap = getFieldValue("jobRunningMap");
        originalCurrentShardingTotalCountMap = getFieldValue("currentShardingTotalCountMap");

        // Set up mock objects
        jobScheduleController = mock(JobScheduleController.class);
        coordinatorRegistryCenter = mock(CoordinatorRegistryCenter.class);

        // Set up test state
        setFieldValue("schedulerMap", new ConcurrentHashMap<String, JobScheduleController>() {{
            put("testJob", jobScheduleController);
        }});
        setFieldValue("regCenterMap", new ConcurrentHashMap<String, CoordinatorRegistryCenter>() {{
            put("testJob", coordinatorRegistryCenter);
        }});
        setFieldValue("jobInstanceMap", new ConcurrentHashMap<String, JobInstance>() {{
            put("testJob", mock(JobInstance.class));
        }});
        setFieldValue("jobRunningMap", new ConcurrentHashMap<String, Boolean>() {{
            put("testJob", true);
        }});
        setFieldValue("currentShardingTotalCountMap", new ConcurrentHashMap<String, Integer>() {{
            put("testJob", 1);
        }});
    }

    @After
    public void tearDown() throws Exception {
        // Restore original state
        setFieldValue("schedulerMap", originalSchedulerMap);
        setFieldValue("regCenterMap", originalRegCenterMap);
        setFieldValue("jobInstanceMap", originalJobInstanceMap);
        setFieldValue("jobRunningMap", originalJobRunningMap);
        setFieldValue("currentShardingTotalCountMap", originalCurrentShardingTotalCountMap);
    }

    @Test
    public void testShutdown() throws Exception {
        jobRegistry.shutdown("testJob");

        // Verify interactions
        verify(jobScheduleController).shutdown();
        verify(coordinatorRegistryCenter).evictCacheData("/testJob");

        // Verify map states
        assertFalse(((Map<String, JobScheduleController>) getFieldValue("schedulerMap")).containsKey("testJob"));
        assertFalse(((Map<String, CoordinatorRegistryCenter>) getFieldValue("regCenterMap")).containsKey("testJob"));
        assertFalse(((Map<String, JobInstance>) getFieldValue("jobInstanceMap")).containsKey("testJob"));
        assertFalse(((Map<String, Boolean>) getFieldValue("jobRunningMap")).containsKey("testJob"));
        assertFalse(((Map<String, Integer>) getFieldValue("currentShardingTotalCountMap")).containsKey("testJob"));
    }

    @SuppressWarnings("unchecked")
    private <T> T getFieldValue(String fieldName) throws Exception {
        Field field = JobRegistry.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(jobRegistry);
    }

    private void setFieldValue(String fieldName, Object value) throws Exception {
        Field field = JobRegistry.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(jobRegistry, value);
    }
}
