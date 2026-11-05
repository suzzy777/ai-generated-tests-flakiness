package io.elasticjob.lite.internal.instance;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.storage.JobNodePath;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InstanceNode_isLocalInstancePath_0_0_Test {
    private InstanceNode instanceNode;
    private JobNodePath jobNodePath;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Set up JobRegistry with a JobInstance
        JobRegistry jobRegistry = JobRegistry.getInstance();
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        Map<String, JobInstance> jobInstanceMap = (Map<String, JobInstance>) jobInstanceMapField.get(jobRegistry);
        jobInstanceMap.put("test_job", new JobInstance("127.0.0.1@-@0"));
        
        // Set up JobNodePath
        jobNodePath = new JobNodePath("test_job");

        // Create InstanceNode using reflection to set the private jobNodePath field
        instanceNode = new InstanceNode("test_job");
        Field jobNodePathField = InstanceNode.class.getDeclaredField("jobNodePath");
        jobNodePathField.setAccessible(true);
        jobNodePathField.set(instanceNode, jobNodePath);
    }

    @Test
    public void testIsLocalInstancePath() {
        String localPath = jobNodePath.getFullPath("instances/127.0.0.1@-@0");
        assertTrue(instanceNode.isLocalInstancePath(localPath));
    }

    @Test
    public void testIsNotLocalInstancePath() {
        String nonLocalPath = jobNodePath.getFullPath("instances/127.0.0.2@-@0");
        assertFalse(instanceNode.isLocalInstancePath(nonLocalPath));
    }
}
