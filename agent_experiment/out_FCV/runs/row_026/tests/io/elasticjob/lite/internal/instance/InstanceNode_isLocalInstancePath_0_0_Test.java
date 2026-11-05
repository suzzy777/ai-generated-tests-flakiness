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

    private Map<String, JobInstance> originalJobInstanceMap;
    private String instancesFormat;

    @Before
    public void setUp() throws Exception {
        // Save the original state of the jobInstanceMap
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        originalJobInstanceMap = new ConcurrentHashMap<>((Map<String, JobInstance>) jobInstanceMapField.get(JobRegistry.getInstance()));

        // Set up the test state
        JobInstance jobInstance = new JobInstance(JOB_INSTANCE_ID);
        Map<String, JobInstance> jobInstanceMap = (Map<String, JobInstance>) jobInstanceMapField.get(JobRegistry.getInstance());
        jobInstanceMap.put(JOB_NAME, jobInstance);

        // Access the private INSTANCES field using reflection
        Field instancesField = InstanceNode.class.getDeclaredField("INSTANCES");
        instancesField.setAccessible(true);
        instancesFormat = (String) instancesField.get(null);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original state of the jobInstanceMap
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        Map<String, JobInstance> jobInstanceMap = (Map<String, JobInstance>) jobInstanceMapField.get(JobRegistry.getInstance());
        jobInstanceMap.clear();
        jobInstanceMap.putAll(originalJobInstanceMap);
    }

    @Test
    public void testIsLocalInstancePath() {
        JobNodePath jobNodePath = new JobNodePath(JOB_NAME);
        InstanceNode instanceNode = new InstanceNode(JOB_NAME);

        String validPath = jobNodePath.getFullPath(String.format(instancesFormat, JOB_INSTANCE_ID));
        assertTrue(instanceNode.isLocalInstancePath(validPath));

        String invalidPath = jobNodePath.getFullPath(String.format(instancesFormat, "invalid_instance_id"));
        assertFalse(instanceNode.isLocalInstancePath(invalidPath));
    }
}
