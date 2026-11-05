package io.elasticjob.lite.internal.instance;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.storage.JobNodePath;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InstanceNode_isLocalInstancePath_0_0_Test {
    private InstanceNode instanceNode;
    private JobNodePath jobNodePath;
    private JobRegistry jobRegistry;
    private String jobName = "testJob";
    private String jobInstanceId = "127.0.0.1@-@1234";
    
    @Before
    public void setUp() throws Exception {
        jobNodePath = new JobNodePath(jobName);
        instanceNode = new InstanceNode(jobName);
        jobRegistry = JobRegistry.getInstance();
        
        // Set jobInstanceMap with reflection
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(jobInstanceMapField, jobInstanceMapField.getModifiers() & ~Modifier.FINAL);
        ((Map<String, JobInstance>) jobInstanceMapField.get(jobRegistry)).put(jobName, new JobInstance(jobInstanceId));
    }

    @Test
    public void testIsLocalInstancePathTrue() throws Exception {
        Field instancesField = InstanceNode.class.getDeclaredField("INSTANCES");
        instancesField.setAccessible(true);
        String instances = (String) instancesField.get(null);
        String path = jobNodePath.getFullPath(String.format(instances, jobInstanceId));
        assertTrue(instanceNode.isLocalInstancePath(path));
    }

    @Test
    public void testIsLocalInstancePathFalse() throws Exception {
        Field instancesField = InstanceNode.class.getDeclaredField("INSTANCES");
        instancesField.setAccessible(true);
        String instances = (String) instancesField.get(null);
        String path = jobNodePath.getFullPath(String.format(instances, "otherInstanceId"));
        assertFalse(instanceNode.isLocalInstancePath(path));
    }
}
