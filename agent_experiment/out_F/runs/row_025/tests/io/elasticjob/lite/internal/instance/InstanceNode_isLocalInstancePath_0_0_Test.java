package io.elasticjob.lite.internal.instance;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.storage.JobNodePath;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class InstanceNode_isLocalInstancePath_0_0_Test {

    private InstanceNode instanceNode;
    private JobRegistry jobRegistry;
    private JobNodePath jobNodePath;
    private static final String JOB_NAME = "testJob";
    private static final String JOB_INSTANCE_ID = "127.0.0.1@-@12345";

    @Before
    public void setUp() throws Exception {
        jobNodePath = new JobNodePath(JOB_NAME);
        setupJobRegistry();
        instanceNode = new InstanceNode(JOB_NAME);
    }

    private void setupJobRegistry() throws Exception {
        jobRegistry = JobRegistry.getInstance();
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        Map<String, JobInstance> jobInstanceMap = new HashMap<>();
        jobInstanceMap.put(JOB_NAME, new JobInstance(JOB_INSTANCE_ID));
        jobInstanceMapField.set(jobRegistry, jobInstanceMap);
    }

    @Test
    public void testIsLocalInstancePath() throws Exception {
        Method getFullPathMethod = JobNodePath.class.getDeclaredMethod("getFullPath", String.class);
        getFullPathMethod.setAccessible(true);

        Field instancesField = InstanceNode.class.getDeclaredField("INSTANCES");
        instancesField.setAccessible(true);
        String instancesFormat = (String) instancesField.get(null);

        String correctPath = (String) getFullPathMethod.invoke(jobNodePath, String.format(instancesFormat, JOB_INSTANCE_ID));
        assertTrue(instanceNode.isLocalInstancePath(correctPath));

        String incorrectPath = (String) getFullPathMethod.invoke(jobNodePath, String.format(instancesFormat, "wrongInstanceId"));
        assertFalse(instanceNode.isLocalInstancePath(incorrectPath));
    }
}
