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

    private static final String JOB_NAME = "test_job";
    private static final String JOB_INSTANCE_ID = "127.0.0.1@-@12345";
    private InstanceNode instanceNode;
    private JobRegistry originalJobRegistry;
    private Map<String, JobInstance> originalJobInstanceMap;

    @Before
    public void setUp() throws Exception {
        // Save the original JobRegistry instance and its jobInstanceMap
        originalJobRegistry = JobRegistry.getInstance();
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        originalJobInstanceMap = new ConcurrentHashMap<>((Map<String, JobInstance>) jobInstanceMapField.get(originalJobRegistry));

        // Set up the test JobRegistry instance
        JobRegistry testJobRegistry = JobRegistry.getInstance();
        Map<String, JobInstance> testJobInstanceMap = new ConcurrentHashMap<>();
        testJobInstanceMap.put(JOB_NAME, new JobInstance(JOB_INSTANCE_ID));
        jobInstanceMapField.set(testJobRegistry, testJobInstanceMap);

        // Set the singleton instance to the test instance
        Field instanceField = JobRegistry.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, testJobRegistry);

        // Set up the InstanceNode
        JobNodePath jobNodePath = new JobNodePath(JOB_NAME);
        instanceNode = new InstanceNode(JOB_NAME);
        Field jobNodePathField = InstanceNode.class.getDeclaredField("jobNodePath");
        jobNodePathField.setAccessible(true);
        jobNodePathField.set(instanceNode, jobNodePath);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original JobRegistry instance and its jobInstanceMap
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        jobInstanceMapField.set(originalJobRegistry, originalJobInstanceMap);

        Field instanceField = JobRegistry.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, originalJobRegistry);
    }

    @Test
    public void testIsLocalInstancePath() throws Exception {
        String correctPath = "/test_job/instances/" + JOB_INSTANCE_ID;
        assertTrue(instanceNode.isLocalInstancePath(correctPath));

        String incorrectPath = "/test_job/instances/incorrect";
        assertFalse(instanceNode.isLocalInstancePath(incorrectPath));
    }
}
