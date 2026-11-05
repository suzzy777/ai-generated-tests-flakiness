package io.elasticjob.lite.internal.instance;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.storage.JobNodePath;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InstanceNode_isLocalInstancePath_0_0_Test {
    private InstanceNode instanceNode;
    private JobRegistry jobRegistry;
    private JobNodePath jobNodePath;

    @Before
    public void setUp() throws Exception {
        String jobName = "testJob";
        instanceNode = new InstanceNode(jobName);

        // Set up JobRegistry instance
        jobRegistry = JobRegistry.getInstance();
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        ConcurrentHashMap<String, JobInstance> jobInstanceMap = new ConcurrentHashMap<>();
        jobInstanceMap.put(jobName, new JobInstance("127.0.0.1@-@1"));
        jobInstanceMapField.set(jobRegistry, jobInstanceMap);

        // Set up JobNodePath
        jobNodePath = new JobNodePath(jobName);
    }

    @Test
    public void testIsLocalInstancePathTrue() {
        String path = jobNodePath.getFullPath("instances/127.0.0.1@-@1");
        assertTrue(instanceNode.isLocalInstancePath(path));
    }

    @Test
    public void testIsLocalInstancePathFalse() {
        String path = jobNodePath.getFullPath("instances/127.0.0.1@-@2");
        assertFalse(instanceNode.isLocalInstancePath(path));
    }
}
