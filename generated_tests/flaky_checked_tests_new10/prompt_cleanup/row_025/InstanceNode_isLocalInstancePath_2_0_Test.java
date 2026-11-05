package io.elasticjob.lite.internal.instance;

import io.elasticjob.lite.internal.instance.InstanceNode;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.storage.JobNodePath;
import io.elasticjob.lite.api.strategy.JobInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import org.mockito.*;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

public class InstanceNode_isLocalInstancePath_2_0_Test {

    private static final String JOB_NAME = "testJob";

    private static final String JOB_INSTANCE_ID = "127.0.0.1@-@12345";

    private static final String LOCAL_INSTANCE_PATH = "/testJob/instances/127.0.0.1@-@12345";

    private JobRegistry originalJobRegistryInstance;

    private ConcurrentHashMap<String, JobInstance> originalJobInstanceMap;

    @Before
    public void setUp() throws Exception {
        // Save the original JobRegistry instance and jobInstanceMap
        Field instanceField = JobRegistry.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        originalJobRegistryInstance = (JobRegistry) instanceField.get(null);
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        originalJobInstanceMap = (ConcurrentHashMap<String, JobInstance>) jobInstanceMapField.get(JobRegistry.getInstance());
        // Set up a mock JobRegistry instance with a test job instance
        JobRegistry mockJobRegistry = JobRegistry.getInstance();
        ConcurrentHashMap<String, JobInstance> mockJobInstanceMap = new ConcurrentHashMap<>();
        mockJobInstanceMap.put(JOB_NAME, new JobInstance(JOB_INSTANCE_ID));
        jobInstanceMapField.set(mockJobRegistry, mockJobInstanceMap);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original JobRegistry instance and jobInstanceMap
        Field instanceField = JobRegistry.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, originalJobRegistryInstance);
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        jobInstanceMapField.set(JobRegistry.getInstance(), originalJobInstanceMap);
    }

    @Test
    public void testIsLocalInstancePathTrue() throws Exception {
        JobNodePath jobNodePath = new JobNodePath(JOB_NAME);
        InstanceNode instanceNode = new InstanceNode(JOB_NAME);
        Method isLocalInstancePathMethod = InstanceNode.class.getDeclaredMethod("isLocalInstancePath", String.class);
        isLocalInstancePathMethod.setAccessible(true);
        boolean result = (boolean) isLocalInstancePathMethod.invoke(instanceNode, LOCAL_INSTANCE_PATH);
        assertTrue(result);
    }

    @Test
    public void testIsLocalInstancePathFalse() throws Exception {
        JobNodePath jobNodePath = new JobNodePath(JOB_NAME);
        InstanceNode instanceNode = new InstanceNode(JOB_NAME);
        Method isLocalInstancePathMethod = InstanceNode.class.getDeclaredMethod("isLocalInstancePath", String.class);
        isLocalInstancePathMethod.setAccessible(true);
        boolean result = (boolean) isLocalInstancePathMethod.invoke(instanceNode, "/wrong/path");
        assertFalse(result);
    }
}
