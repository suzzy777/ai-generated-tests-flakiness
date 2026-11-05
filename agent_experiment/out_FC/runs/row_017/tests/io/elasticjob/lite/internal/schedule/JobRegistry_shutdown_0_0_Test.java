package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

public class JobRegistry_shutdown_0_0_Test {

    private JobRegistry jobRegistry;
    private String jobName = "testJob";
    private JobScheduleController mockScheduleController;
    private CoordinatorRegistryCenter mockRegCenter;

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();

        // Set up the mock objects
        mockScheduleController = mock(JobScheduleController.class);
        mockRegCenter = mock(CoordinatorRegistryCenter.class);

        // Use reflection to set the private fields
        setPrivateField(jobRegistry, "schedulerMap", new ConcurrentHashMap<String, JobScheduleController>());
        setPrivateField(jobRegistry, "regCenterMap", new ConcurrentHashMap<String, CoordinatorRegistryCenter>());
        setPrivateField(jobRegistry, "jobInstanceMap", new ConcurrentHashMap<String, JobInstance>());
        setPrivateField(jobRegistry, "jobRunningMap", new ConcurrentHashMap<String, Boolean>());
        setPrivateField(jobRegistry, "currentShardingTotalCountMap", new ConcurrentHashMap<String, Integer>());

        // Add mock objects to the maps
        getSchedulerMap().put(jobName, mockScheduleController);
        getRegCenterMap().put(jobName, mockRegCenter);
        getJobInstanceMap().put(jobName, new JobInstance(jobName));
        getJobRunningMap().put(jobName, true);
        getCurrentShardingTotalCountMap().put(jobName, 1);
    }

    @After
    public void tearDown() throws Exception {
        // Clear the maps after each test
        getSchedulerMap().clear();
        getRegCenterMap().clear();
        getJobInstanceMap().clear();
        getJobRunningMap().clear();
        getCurrentShardingTotalCountMap().clear();
    }

    @Test
    public void testShutdown() throws Exception {
        jobRegistry.shutdown(jobName);

        // Verify that the shutdown method was called on the JobScheduleController
        verify(mockScheduleController).shutdown();

        // Verify that the evictCacheData method was called on the CoordinatorRegistryCenter
        verify(mockRegCenter).evictCacheData("/" + jobName);

        // Verify that the maps no longer contain the jobName
        assertFalse(getSchedulerMap().containsKey(jobName));
        assertFalse(getRegCenterMap().containsKey(jobName));
        assertFalse(getJobInstanceMap().containsKey(jobName));
        assertFalse(getJobRunningMap().containsKey(jobName));
        assertFalse(getCurrentShardingTotalCountMap().containsKey(jobName));
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    private Map<String, JobScheduleController> getSchedulerMap() throws Exception {
        return (Map<String, JobScheduleController>) getPrivateField(jobRegistry, "schedulerMap");
    }

    private Map<String, CoordinatorRegistryCenter> getRegCenterMap() throws Exception {
        return (Map<String, CoordinatorRegistryCenter>) getPrivateField(jobRegistry, "regCenterMap");
    }

    private Map<String, JobInstance> getJobInstanceMap() throws Exception {
        return (Map<String, JobInstance>) getPrivateField(jobRegistry, "jobInstanceMap");
    }

    private Map<String, Boolean> getJobRunningMap() throws Exception {
        return (Map<String, Boolean>) getPrivateField(jobRegistry, "jobRunningMap");
    }

    private Map<String, Integer> getCurrentShardingTotalCountMap() throws Exception {
        return (Map<String, Integer>) getPrivateField(jobRegistry, "currentShardingTotalCountMap");
    }

    private Object getPrivateField(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }
}
