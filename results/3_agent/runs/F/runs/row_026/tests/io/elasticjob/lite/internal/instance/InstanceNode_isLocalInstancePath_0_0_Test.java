package io.elasticjob.lite.internal.instance;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.storage.JobNodePath;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class InstanceNode_isLocalInstancePath_0_0_Test {
    private static final String JOB_NAME = "test_job";
    private static final String JOB_INSTANCE_ID = "127.0.0.1@-@1234";
    private InstanceNode instanceNode;
    private JobNodePath jobNodePath;
    private JobInstance jobInstance;
    private String instancesFormat;

    @Before
    public void setUp() throws Exception {
        jobNodePath = new JobNodePath(JOB_NAME);
        jobInstance = new JobInstance(JOB_INSTANCE_ID);

        // Use reflection to set the private jobInstanceMap in JobRegistry
        JobRegistry jobRegistry = JobRegistry.getInstance();
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        ((ConcurrentHashMap<String, JobInstance>) jobInstanceMapField.get(jobRegistry)).put(JOB_NAME, jobInstance);

        instanceNode = new InstanceNode(JOB_NAME);

        // Use reflection to access the private static field INSTANCES
        Field instancesField = InstanceNode.class.getDeclaredField("INSTANCES");
        instancesField.setAccessible(true);
        instancesFormat = (String) instancesField.get(null);
    }

    @Test
    public void testIsLocalInstancePath() {
        String validPath = jobNodePath.getFullPath(String.format(instancesFormat, JOB_INSTANCE_ID));
        assertTrue(instanceNode.isLocalInstancePath(validPath));

        String invalidPath = jobNodePath.getFullPath(String.format(instancesFormat, "invalid_id"));
        assertFalse(instanceNode.isLocalInstancePath(invalidPath));
    }
}
