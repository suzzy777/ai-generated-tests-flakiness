package io.elasticjob.lite.internal.instance;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.storage.JobNodePath;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InstanceNode_isLocalInstancePath_0_0_Test {
    private InstanceNode instanceNode;
    private JobRegistry jobRegistry;
    private JobNodePath jobNodePath;
    private String jobName = "testJob";
    private String jobInstanceId = "127.0.0.1@-@1234";

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();
        jobNodePath = new JobNodePath(jobName);

        // Use reflection to set the jobInstanceMap
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        ((Map<String, JobInstance>) jobInstanceMapField.get(jobRegistry)).put(jobName, new JobInstance(jobInstanceId));

        instanceNode = new InstanceNode(jobName);
    }

    @Test
    public void testIsLocalInstancePathTrue() throws Exception {
        String path = jobNodePath.getFullPath(String.format("instances/" + jobInstanceId, jobInstanceId));
        Method method = InstanceNode.class.getDeclaredMethod("isLocalInstancePath", String.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(instanceNode, path);
        assertTrue(result);
    }

    @Test
    public void testIsLocalInstancePathFalse() throws Exception {
        String path = jobNodePath.getFullPath(String.format("instances/" + jobInstanceId, "anotherInstanceId"));
        Method method = InstanceNode.class.getDeclaredMethod("isLocalInstancePath", String.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(instanceNode, path);
        assertTrue(result);
    }
}
