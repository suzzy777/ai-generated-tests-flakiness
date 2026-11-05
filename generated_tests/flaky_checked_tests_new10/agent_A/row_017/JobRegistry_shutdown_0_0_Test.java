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

        // Using reflection to set private fields
        setPrivateField("schedulerMap", "testJob", jobScheduleController);
        setPrivateField("regCenterMap", "testJob", coordinatorRegistryCenter);
        setPrivateField("jobInstanceMap", "testJob", mock(JobInstance.class));
        setPrivateField("jobRunningMap", "testJob", true);
        setPrivateField("currentShardingTotalCountMap", "testJob", 3);
    }

    @Test
    public void testShutdown() {
        jobRegistry.shutdown("testJob");

        verify(jobScheduleController).shutdown();
        verify(coordinatorRegistryCenter).evictCacheData("/testJob");

        assertNull(getPrivateField("schedulerMap", "testJob"));
        assertNull(getPrivateField("regCenterMap", "testJob"));
        assertNull(getPrivateField("jobInstanceMap", "testJob"));
        assertNull(getPrivateField("jobRunningMap", "testJob"));
        assertNull(getPrivateField("currentShardingTotalCountMap", "testJob"));
    }

    private void setPrivateField(String fieldName, String key, Object value) throws Exception {
        Field field = JobRegistry.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        Map<String, Object> map = (Map<String, Object>) field.get(jobRegistry);
        map.put(key, value);
    }

    private Object getPrivateField(String fieldName, String key) {
        try {
            Field field = JobRegistry.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Map<String, Object> map = (Map<String, Object>) field.get(jobRegistry);
            return map.get(key);
        } catch (Exception e) {
            return null;
        }
    }
}
