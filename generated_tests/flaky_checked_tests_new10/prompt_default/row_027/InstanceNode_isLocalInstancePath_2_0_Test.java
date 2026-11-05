package io.elasticjob.lite.internal.instance;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.internal.instance.InstanceNode;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.storage.JobNodePath;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class InstanceNode_isLocalInstancePath_2_0_Test {

    private InstanceNode instanceNode;

    private JobNodePath jobNodePath;

    private JobRegistry jobRegistry;

    private String jobName;

    private JobInstance jobInstance;

    @Before
    public void setUp() throws Exception {
        jobName = "test_job";
        jobNodePath = new JobNodePath(jobName);
        // Use reflection to set the singleton instance of JobRegistry
        jobRegistry = JobRegistry.getInstance();
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        ((Map<String, JobInstance>) jobInstanceMapField.get(jobRegistry)).put(jobName, new JobInstance("127.0.0.1@-@12345"));
        instanceNode = new InstanceNode(jobName);
    }

    @Test
    public void testIsLocalInstancePathTrue() throws Exception {
        String expectedPath = jobNodePath.getFullPath(String.format(InstanceNode.ROOT + "/%s", jobRegistry.getJobInstance(jobName).getJobInstanceId()));
        Method method = InstanceNode.class.getDeclaredMethod("isLocalInstancePath", String.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(instanceNode, expectedPath);
        assertTrue(result);
    }

    @Test
    public void testIsLocalInstancePathFalse() throws Exception {
        String unexpectedPath = jobNodePath.getFullPath("instances/different_instance");
        Method method = InstanceNode.class.getDeclaredMethod("isLocalInstancePath", String.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(instanceNode, unexpectedPath);
        assertFalse(result);
    }
}
