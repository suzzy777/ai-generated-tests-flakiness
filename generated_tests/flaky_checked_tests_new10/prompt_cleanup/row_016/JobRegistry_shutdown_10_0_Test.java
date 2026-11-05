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
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.elasticjob.lite.api.strategy.JobInstance;
import org.mockito.*;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@RunWith(MockitoJUnitRunner.class)
public class JobRegistry_shutdown_10_0_Test {

    private JobRegistry jobRegistry;

    private CoordinatorRegistryCenter mockRegistryCenter;

    private Map<String, JobScheduleController> originalSchedulerMap;

    private Map<String, CoordinatorRegistryCenter> originalRegCenterMap;

    private Map<String, JobScheduleController> schedulerMap;

    private Map<String, CoordinatorRegistryCenter> regCenterMap;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        jobRegistry = JobRegistry.getInstance();
        // Save original maps to restore them after tests
        originalSchedulerMap = getPrivateField("schedulerMap");
        originalRegCenterMap = getPrivateField("regCenterMap");
        // Set up mock maps for testing
        schedulerMap = new ConcurrentHashMap<>();
        regCenterMap = new ConcurrentHashMap<>();
        setPrivateField("schedulerMap", schedulerMap);
        setPrivateField("regCenterMap", regCenterMap);
        // Create mock objects
        mockRegistryCenter = mock(CoordinatorRegistryCenter.class);
    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        // Restore original maps
        setPrivateField("schedulerMap", originalSchedulerMap);
        setPrivateField("regCenterMap", originalRegCenterMap);
    }

    @Test
    public void testShutdownWithNonExistingJob() {
        // Prepare test data
        String jobName = "nonExistingJob";
        // Execute the method under test
        jobRegistry.shutdown(jobName);
        // Verify no interactions
        assertNull(schedulerMap.get(jobName));
        assertNull(regCenterMap.get(jobName));
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = JobRegistry.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(jobRegistry);
    }

    private void setPrivateField(String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = JobRegistry.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(jobRegistry, value);
    }
}
