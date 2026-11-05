package io.elasticjob.lite.internal.instance;

import io.elasticjob.lite.internal.instance.InstanceNode;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.storage.JobNodePath;
import io.elasticjob.lite.api.strategy.JobInstance;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
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

    private JobRegistry jobRegistry;

    private JobNodePath jobNodePath;

    private String jobName = "testJob";

    @Before
    public void setUp() throws Exception {
        // Initialize JobRegistry and its jobInstanceMap using reflection
        jobRegistry = JobRegistry.getInstance();
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        jobInstanceMapField.set(jobRegistry, new ConcurrentHashMap<>());
        // Add a JobInstance to the jobInstanceMap
        JobInstance jobInstance = new JobInstance("localhost@-@12345");
        ((ConcurrentHashMap<String, JobInstance>) jobInstanceMapField.get(jobRegistry)).put(jobName, jobInstance);
        // Initialize JobNodePath with the jobName
        jobNodePath = new JobNodePath(jobName);
        // Initialize InstanceNode with the jobName
        instanceNode = new InstanceNode(jobName);
    }

    @Test
    public void testIsLocalInstancePathTrue() throws Exception {
        // Construct the expected path
        String expectedPath = jobNodePath.getFullPath(String.format(getInstancesPattern(), "localhost@-@12345"));
        // Use reflection to invoke the private method
        boolean result = (boolean) InstanceNode.class.getDeclaredMethod("isLocalInstancePath", String.class).invoke(instanceNode, expectedPath);
        assertTrue(result);
    }

    @Test
    public void testIsLocalInstancePathFalse() throws Exception {
        // Construct an incorrect path
        String incorrectPath = jobNodePath.getFullPath(String.format(getInstancesPattern(), "otherInstance"));
        // Use reflection to invoke the private method
        boolean result = (boolean) InstanceNode.class.getDeclaredMethod("isLocalInstancePath", String.class).invoke(instanceNode, incorrectPath);
        assertFalse(result);
    }

    private String getInstancesPattern() throws Exception {
        Field instancesField = InstanceNode.class.getDeclaredField("INSTANCES");
        instancesField.setAccessible(true);
        return (String) instancesField.get(null);
    }
}
