package io.elasticjob.lite.internal.instance;

import io.elasticjob.lite.internal.instance.InstanceNode;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.storage.JobNodePath;
import io.elasticjob.lite.api.strategy.JobInstance;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

public class InstanceNode_isLocalInstancePath_2_0_Test {

    private InstanceNode instanceNode;

    private JobRegistry jobRegistry;

    private JobNodePath jobNodePath;

    private String jobName = "testJob";

    private JobInstance jobInstance;

    @Before
    public void setUp() throws Exception {
        // Initialize JobRegistry singleton
        jobRegistry = JobRegistry.getInstance();
        // Set up JobInstance and add it to JobRegistry
        jobInstance = new JobInstance("127.0.0.1@-@12345");
        setJobRegistryJobInstance(jobName, jobInstance);
        // Initialize JobNodePath
        jobNodePath = new JobNodePath(jobName);
        // Initialize InstanceNode
        instanceNode = new InstanceNode(jobName);
    }

    @Test
    public void testIsLocalInstancePathTrue() throws Exception {
        String localInstancePath = jobNodePath.getFullPath(String.format(InstanceNode.ROOT + "/%s", jobInstance.getJobInstanceId()));
        assertTrue(invokeIsLocalInstancePath(localInstancePath));
    }

    @Test
    public void testIsLocalInstancePathFalse() throws Exception {
        String nonLocalInstancePath = jobNodePath.getFullPath(String.format(InstanceNode.ROOT + "/%s", "nonExistingInstanceId"));
        assertFalse(invokeIsLocalInstancePath(nonLocalInstancePath));
    }

    private boolean invokeIsLocalInstancePath(String path) throws Exception {
        java.lang.reflect.Method method = InstanceNode.class.getDeclaredMethod("isLocalInstancePath", String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(instanceNode, path);
    }

    private void setJobRegistryJobInstance(String jobName, JobInstance jobInstance) throws Exception {
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        ConcurrentHashMap<String, JobInstance> jobInstanceMap = (ConcurrentHashMap<String, JobInstance>) jobInstanceMapField.get(jobRegistry);
        jobInstanceMap.put(jobName, jobInstance);
    }
}
