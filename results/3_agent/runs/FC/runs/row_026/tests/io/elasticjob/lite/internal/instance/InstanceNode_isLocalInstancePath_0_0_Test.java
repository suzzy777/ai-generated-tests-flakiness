package io.elasticjob.lite.internal.instance;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.storage.JobNodePath;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InstanceNode_isLocalInstancePath_0_0_Test {

    private JobRegistry originalJobRegistry;
    private Map<String, JobInstance> originalJobInstanceMap;

    @Before
    public void setUp() throws Exception {
        // Save the original JobRegistry instance and jobInstanceMap
        Field instanceField = JobRegistry.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        originalJobRegistry = (JobRegistry) instanceField.get(null);

        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        originalJobInstanceMap = new ConcurrentHashMap<>((Map<String, JobInstance>) jobInstanceMapField.get(JobRegistry.getInstance()));

        // Set up a new JobRegistry instance with a test JobInstance
        JobRegistry testJobRegistry = JobRegistry.getInstance();
        Map<String, JobInstance> testJobInstanceMap = new ConcurrentHashMap<>();
        JobInstance testJobInstance = new JobInstance("127.0.0.1@-@12345");
        testJobInstanceMap.put("testJob", testJobInstance);

        jobInstanceMapField.set(testJobRegistry, testJobInstanceMap);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original JobRegistry instance and jobInstanceMap
        Field instanceField = JobRegistry.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, originalJobRegistry);

        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        jobInstanceMapField.set(JobRegistry.getInstance(), originalJobInstanceMap);
    }

    @Test
    public void testIsLocalInstancePath() throws Exception {
        String jobName = "testJob";
        JobNodePath jobNodePath = new JobNodePath(jobName);
        InstanceNode instanceNode = new InstanceNode(jobName);

        Field instancesField = InstanceNode.class.getDeclaredField("INSTANCES");
        instancesField.setAccessible(true);
        String instancesFormat = (String) instancesField.get(null);

        String validPath = jobNodePath.getFullPath(String.format(instancesFormat, "127.0.0.1@-@12345"));
        String invalidPath = jobNodePath.getFullPath(String.format(instancesFormat, "127.0.0.1@-@54321"));

        assertTrue(instanceNode.isLocalInstancePath(validPath));
        assertFalse(instanceNode.isLocalInstancePath(invalidPath));
    }
}
