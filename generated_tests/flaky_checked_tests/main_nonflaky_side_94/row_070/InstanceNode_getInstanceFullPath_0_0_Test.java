package io.elasticjob.lite.internal.instance;

import io.elasticjob.lite.internal.storage.JobNodePath;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import io.elasticjob.lite.internal.schedule.JobRegistry;

@RunWith(MockitoJUnitRunner.class)
public class InstanceNode_getInstanceFullPath_0_0_Test {

    private InstanceNode instanceNode;

    private final String jobName = "testJob";

    @Before
    public void setUp() {
        instanceNode = new InstanceNode(jobName);
    }

    @Test
    public void testGetInstanceFullPath() throws Exception {
        // Use reflection to create an instance of JobNodePath
        java.lang.reflect.Constructor<JobNodePath> constructor = JobNodePath.class.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        JobNodePath jobNodePath = constructor.newInstance(jobName);
        // Use reflection to set the private field jobNodePath
        java.lang.reflect.Field jobNodePathField = InstanceNode.class.getDeclaredField("jobNodePath");
        jobNodePathField.setAccessible(true);
        jobNodePathField.set(instanceNode, jobNodePath);
        // Verify the method output
        String expectedFullPath = jobNodePath.getFullPath(InstanceNode.ROOT);
        assertEquals(expectedFullPath, instanceNode.getInstanceFullPath());
    }
}
