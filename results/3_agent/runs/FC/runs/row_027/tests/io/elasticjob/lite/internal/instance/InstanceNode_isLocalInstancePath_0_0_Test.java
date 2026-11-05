package io.elasticjob.lite.internal.instance;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.storage.JobNodePath;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InstanceNode_isLocalInstancePath_0_0_Test {

    private JobRegistry originalJobRegistry;

    @Before
    public void setUp() throws Exception {
        // Backup the original JobRegistry instance
        Field instanceField = JobRegistry.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        originalJobRegistry = (JobRegistry) instanceField.get(null);

        // Set up a new JobRegistry instance for testing using reflection
        Constructor<JobRegistry> constructor = JobRegistry.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        JobRegistry testRegistry = constructor.newInstance();

        instanceField.set(null, testRegistry);

        // Set up the job instance map
        Map<String, JobInstance> jobInstanceMap = new ConcurrentHashMap<>();
        JobInstance jobInstance = new JobInstance("testJobInstanceId");
        jobInstanceMap.put("testJob", jobInstance);

        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        jobInstanceMapField.set(testRegistry, jobInstanceMap);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original JobRegistry instance
        Field instanceField = JobRegistry.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, originalJobRegistry);
    }

    @Test
    public void testIsLocalInstancePath() throws Exception {
        String jobName = "testJob";
        JobNodePath jobNodePath = new JobNodePath(jobName);
        InstanceNode instanceNode = new InstanceNode(jobName);

        // Access the private INSTANCES field using reflection
        Field instancesField = InstanceNode.class.getDeclaredField("INSTANCES");
        instancesField.setAccessible(true);
        String instancesPattern = (String) instancesField.get(null);

        // Create the expected path
        String expectedPath = jobNodePath.getFullPath(String.format(instancesPattern, "testJobInstanceId"));

        // Test the method
        assertTrue(instanceNode.isLocalInstancePath(expectedPath));
        assertFalse(instanceNode.isLocalInstancePath("/invalid/path"));
    }
}
