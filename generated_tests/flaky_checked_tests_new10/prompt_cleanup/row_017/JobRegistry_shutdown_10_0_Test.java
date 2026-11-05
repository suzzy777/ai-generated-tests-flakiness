package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.schedule.JobScheduleController;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.mockito.*;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.elasticjob.lite.api.strategy.JobInstance;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

public class JobRegistry_shutdown_10_0_Test {

    private JobRegistry jobRegistry;

    private Map<String, JobScheduleController> originalSchedulerMap;

    private Map<String, CoordinatorRegistryCenter> originalRegCenterMap;

    private Map<String, JobInstance> originalJobInstanceMap;

    private Map<String, Boolean> originalJobRunningMap;

    private Map<String, Integer> originalCurrentShardingTotalCountMap;

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();
        // Save original maps
        originalSchedulerMap = getFieldValue("schedulerMap");
        originalRegCenterMap = getFieldValue("regCenterMap");
        originalJobInstanceMap = getFieldValue("jobInstanceMap");
        originalJobRunningMap = getFieldValue("jobRunningMap");
        originalCurrentShardingTotalCountMap = getFieldValue("currentShardingTotalCountMap");
        // Set up mock maps
        setFieldValue("schedulerMap", new ConcurrentHashMap<>());
        setFieldValue("regCenterMap", new ConcurrentHashMap<>());
        setFieldValue("jobInstanceMap", new ConcurrentHashMap<>());
        setFieldValue("jobRunningMap", new ConcurrentHashMap<>());
        setFieldValue("currentShardingTotalCountMap", new ConcurrentHashMap<>());
    }

    @After
    public void tearDown() throws Exception {
        // Restore original maps
        setFieldValue("schedulerMap", originalSchedulerMap);
        setFieldValue("regCenterMap", originalRegCenterMap);
        setFieldValue("jobInstanceMap", originalJobInstanceMap);
        setFieldValue("jobRunningMap", originalJobRunningMap);
        setFieldValue("currentShardingTotalCountMap", originalCurrentShardingTotalCountMap);
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

    @Test
    public void testShutdownJobDoesNotExist() throws Exception {
        String jobName = "nonExistentJob";
        // Execute shutdown
        jobRegistry.shutdown(jobName);
        // Verify no exceptions and no interactions
        assertNull(((Map<String, JobScheduleController>) getFieldValue("schedulerMap")).get(jobName));
        assertNull(((Map<String, CoordinatorRegistryCenter>) getFieldValue("regCenterMap")).get(jobName));
        assertNull(((Map<String, JobInstance>) getFieldValue("jobInstanceMap")).get(jobName));
        assertNull(((Map<String, Boolean>) getFieldValue("jobRunningMap")).get(jobName));
        assertNull(((Map<String, Integer>) getFieldValue("currentShardingTotalCountMap")).get(jobName));
    }
}
