package io.elasticjob.lite.internal.instance;

import io.elasticjob.lite.internal.instance.InstanceNode;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.storage.JobNodePath;
import io.elasticjob.lite.api.strategy.JobInstance;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class InstanceNode_isLocalInstancePath_2_0_Test {

    private static final String JOB_NAME = "testJob";

    private static final String JOB_INSTANCE_ID = "127.0.0.1@-@12345";

    private InstanceNode instanceNode;

    private JobRegistry originalJobRegistry;

    private ConcurrentHashMap<String, JobInstance> originalJobInstanceMap;

    private String originalInstancesFormat;

    @Before
    public void setUp() throws Exception {
        // Save the original JobRegistry instance and jobInstanceMap
        Field instanceField = JobRegistry.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        originalJobRegistry = (JobRegistry) instanceField.get(null);
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        originalJobInstanceMap = (ConcurrentHashMap<String, JobInstance>) jobInstanceMapField.get(JobRegistry.getInstance());
        // Save the original INSTANCES format
        Field instancesField = InstanceNode.class.getDeclaredField("INSTANCES");
        instancesField.setAccessible(true);
        originalInstancesFormat = (String) instancesField.get(null);
        // Set up the JobRegistry with a mock JobInstance
        JobRegistry jobRegistry = JobRegistry.getInstance();
        ConcurrentHashMap<String, JobInstance> jobInstanceMap = new ConcurrentHashMap<>();
        jobInstanceMap.put(JOB_NAME, new JobInstance(JOB_INSTANCE_ID));
        jobInstanceMapField.set(jobRegistry, jobInstanceMap);
        // Initialize InstanceNode
        instanceNode = new InstanceNode(JOB_NAME);
    }

    @Test
    public void testIsLocalInstancePathTrue() throws Exception {
        JobNodePath jobNodePath = new JobNodePath(JOB_NAME);
        String localInstancePath = jobNodePath.getFullPath(String.format(originalInstancesFormat, JOB_INSTANCE_ID));
        boolean result = invokeIsLocalInstancePath(localInstancePath);
        assertTrue(result);
    }

    @Test
    public void testIsLocalInstancePathFalse() throws Exception {
        String incorrectPath = "/wrong/path";
        boolean result = invokeIsLocalInstancePath(incorrectPath);
        assertFalse(result);
    }

    private boolean invokeIsLocalInstancePath(String path) throws Exception {
        java.lang.reflect.Method method = InstanceNode.class.getDeclaredMethod("isLocalInstancePath", String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(instanceNode, path);
    }
}
