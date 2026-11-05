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
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.elasticjob.lite.api.strategy.JobInstance;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class JobRegistry_shutdown_10_1_Test {

    private JobRegistry jobRegistry;

    private Map<String, JobScheduleController> originalSchedulerMap;

    private Map<String, CoordinatorRegistryCenter> originalRegCenterMap;

    private Map<String, Object> originalJobInstanceMap;

    private Map<String, Boolean> originalJobRunningMap;

    private Map<String, Integer> originalCurrentShardingTotalCountMap;

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();
        // Save original maps
        originalSchedulerMap = getPrivateField("schedulerMap");
        originalRegCenterMap = getPrivateField("regCenterMap");
        originalJobInstanceMap = getPrivateField("jobInstanceMap");
        originalJobRunningMap = getPrivateField("jobRunningMap");
        originalCurrentShardingTotalCountMap = getPrivateField("currentShardingTotalCountMap");
        // Prepare mock maps for testing
        setPrivateField("schedulerMap", new ConcurrentHashMap<>());
        setPrivateField("regCenterMap", new ConcurrentHashMap<>());
        setPrivateField("jobInstanceMap", new ConcurrentHashMap<>());
        setPrivateField("jobRunningMap", new ConcurrentHashMap<>());
        setPrivateField("currentShardingTotalCountMap", new ConcurrentHashMap<>());
    }

    @After
    public void tearDown() throws Exception {
        // Restore original maps
        setPrivateField("schedulerMap", originalSchedulerMap);
        setPrivateField("regCenterMap", originalRegCenterMap);
        setPrivateField("jobInstanceMap", originalJobInstanceMap);
        setPrivateField("jobRunningMap", originalJobRunningMap);
        setPrivateField("currentShardingTotalCountMap", originalCurrentShardingTotalCountMap);
    }

    @Test
    public void testShutdownWithNonExistingJob() throws Exception {
        String jobName = "nonExistingJob";
        jobRegistry.shutdown(jobName);
        // Verify that no interactions occur with non-existing job
        assertNull(((Map<?, ?>) getPrivateField("schedulerMap")).get(jobName));
        assertNull(((Map<?, ?>) getPrivateField("regCenterMap")).get(jobName));
        assertNull(((Map<?, ?>) getPrivateField("jobInstanceMap")).get(jobName));
        assertNull(((Map<?, ?>) getPrivateField("jobRunningMap")).get(jobName));
        assertNull(((Map<?, ?>) getPrivateField("currentShardingTotalCountMap")).get(jobName));
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(String fieldName) throws Exception {
        Field field = JobRegistry.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(jobRegistry);
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = JobRegistry.class.getDeclaredField(fieldName);
    }
}
