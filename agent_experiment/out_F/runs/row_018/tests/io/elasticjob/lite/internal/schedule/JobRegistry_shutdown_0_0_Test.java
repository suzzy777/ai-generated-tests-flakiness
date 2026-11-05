package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;
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

        setField("schedulerMap", "testJob", jobScheduleController);
        setField("regCenterMap", "testJob", coordinatorRegistryCenter);
        setField("jobInstanceMap", "testJob", mock(JobInstance.class));
        setField("jobRunningMap", "testJob", true);
        setField("currentShardingTotalCountMap", "testJob", 1);
    }

    @Test
    public void testShutdown() throws Exception {
        jobRegistry.shutdown("testJob");

        verify(jobScheduleController).shutdown();
        verify(coordinatorRegistryCenter).evictCacheData("/testJob");

        assertNull(getField("schedulerMap", "testJob"));
        assertNull(getField("regCenterMap", "testJob"));
        assertNull(getField("jobInstanceMap", "testJob"));
        assertNull(getField("jobRunningMap", "testJob"));
        assertNull(getField("currentShardingTotalCountMap", "testJob"));
    }

    private void setField(String fieldName, String key, Object value) throws Exception {
        Field field = JobRegistry.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        Map<String, Object> map = (Map<String, Object>) field.get(jobRegistry);
        map.put(key, value);
    }

    private Object getField(String fieldName, String key) throws Exception {
        Field field = JobRegistry.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        Map<String, Object> map = (Map<String, Object>) field.get(jobRegistry);
        return map.get(key);
    }
}
