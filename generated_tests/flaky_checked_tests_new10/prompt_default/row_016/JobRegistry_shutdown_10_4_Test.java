package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.schedule.JobScheduleController;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import io.elasticjob.lite.api.strategy.JobInstance;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@RunWith(MockitoJUnitRunner.class)
public class JobRegistry_shutdown_10_4_Test {

    private JobRegistry jobRegistry;

    @Mock
    private JobScheduleController jobScheduleController;

    @Mock
    private CoordinatorRegistryCenter coordinatorRegistryCenter;

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();
        // Use reflection to set private fields
        setPrivateField("schedulerMap", jobRegistry, new ConcurrentHashMap<>());
        setPrivateField("regCenterMap", jobRegistry, new ConcurrentHashMap<>());
        setPrivateField("jobInstanceMap", jobRegistry, new ConcurrentHashMap<>());
        setPrivateField("jobRunningMap", jobRegistry, new ConcurrentHashMap<>());
        setPrivateField("currentShardingTotalCountMap", jobRegistry, new ConcurrentHashMap<>());
        // Initialize the maps with mock objects
        getSchedulerMap().put("testJob", jobScheduleController);
        getRegCenterMap().put("testJob", coordinatorRegistryCenter);
    }

    @Test
    public void testShutdown() throws Exception {
        jobRegistry.shutdown("testJob");
        // Verify shutdown was called on JobScheduleController
        verify(jobScheduleController).shutdown();
        // Verify evictCacheData was called on CoordinatorRegistryCenter
        verify(coordinatorRegistryCenter).evictCacheData("/testJob");
        // Ensure the maps no longer contain the job
        assertNull(getSchedulerMap().get("testJob"));
        assertNull(getRegCenterMap().get("testJob"));
        assertNull(getJobInstanceMap().get("testJob"));
        assertNull(getJobRunningMap().get("testJob"));
        assertNull(getCurrentShardingTotalCountMap().get("testJob"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, JobScheduleController> getSchedulerMap() throws Exception {
        return (Map<String, JobScheduleController>) getPrivateField("schedulerMap", jobRegistry);
    }

    @SuppressWarnings("unchecked")
    private Map<String, CoordinatorRegistryCenter> getRegCenterMap() throws Exception {
        return (Map<String, CoordinatorRegistryCenter>) getPrivateField("regCenterMap", jobRegistry);
    }

    @SuppressWarnings("unchecked")
    private Map<String, ?> getJobInstanceMap() throws Exception {
        return (Map<String, ?>) getPrivateField("jobInstanceMap", jobRegistry);
    }

    @SuppressWarnings("unchecked")
    private Map<String, ?> getJobRunningMap() throws Exception {
        return (Map<String, ?>) getPrivateField("jobRunningMap", jobRegistry);
    }

    @SuppressWarnings("unchecked")
    private Map<String, ?> getCurrentShardingTotalCountMap() throws Exception {
        return (Map<String, ?>) getPrivateField("currentShardingTotalCountMap", jobRegistry);
    }

    private void setPrivateField(String fieldName, Object object, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    private Object getPrivateField(String fieldName, Object object) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }
}
