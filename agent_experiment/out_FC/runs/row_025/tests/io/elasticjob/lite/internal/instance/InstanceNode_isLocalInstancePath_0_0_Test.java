package io.elasticjob.lite.internal.instance;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.storage.JobNodePath;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InstanceNode_isLocalInstancePath_0_0_Test {
    private static final String JOB_NAME = "testJob";
    private InstanceNode instanceNode;
    private JobNodePath jobNodePath;
    private Map<String, JobInstance> originalJobInstanceMap;
    private String instancesFormat;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Save the original state of the jobInstanceMap
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        JobRegistry jobRegistry = JobRegistry.getInstance();
        originalJobInstanceMap = new ConcurrentHashMap<>((Map<String, JobInstance>) jobInstanceMapField.get(jobRegistry));

        // Set up the test job instance
        JobInstance jobInstance = new JobInstance("127.0.0.1@-@12345");
        ((Map<String, JobInstance>) jobInstanceMapField.get(jobRegistry)).put(JOB_NAME, jobInstance);

        // Initialize the instanceNode and jobNodePath
        jobNodePath = new JobNodePath(JOB_NAME);
        instanceNode = new InstanceNode(JOB_NAME);

        // Access the private INSTANCES field
        Field instancesField = InstanceNode.class.getDeclaredField("INSTANCES");
        instancesField.setAccessible(true);
        instancesFormat = (String) instancesField.get(instanceNode);
    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        // Restore the original state of the jobInstanceMap
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        ((Map<String, JobInstance>) jobInstanceMapField.get(JobRegistry.getInstance())).clear();
        ((Map<String, JobInstance>) jobInstanceMapField.get(JobRegistry.getInstance())).putAll(originalJobInstanceMap);
    }

    @Test
    public void testIsLocalInstancePathTrue() {
        String localInstancePath = jobNodePath.getFullPath(String.format(instancesFormat, "127.0.0.1@-@12345"));
        assertTrue(instanceNode.isLocalInstancePath(localInstancePath));
    }

    @Test
    public void testIsLocalInstancePathFalse() {
        String differentInstancePath = jobNodePath.getFullPath(String.format(instancesFormat, "127.0.0.1@-@54321"));
        assertFalse(instanceNode.isLocalInstancePath(differentInstancePath));
    }
}
