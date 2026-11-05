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

    private static final String JOB_NAME = "testJob";

    private static final String JOB_INSTANCE_ID = "127.0.0.1@-@12345";

    private InstanceNode instanceNode;

    private JobNodePath jobNodePath;

    private JobRegistry jobRegistry;

    @Before
    public void setUp() throws Exception {
        jobNodePath = new JobNodePath(JOB_NAME);
        instanceNode = new InstanceNode(JOB_NAME);
        // Set up JobRegistry singleton instance
        jobRegistry = JobRegistry.getInstance();
        setJobInstance(JOB_NAME, new JobInstance(JOB_INSTANCE_ID));
    }

    private void setJobInstance(String jobName, JobInstance jobInstance) throws Exception {
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        ConcurrentHashMap<String, JobInstance> jobInstanceMap = (ConcurrentHashMap<String, JobInstance>) jobInstanceMapField.get(jobRegistry);
        jobInstanceMap.put(jobName, jobInstance);
    }

    @Test
    public void testIsLocalInstancePathTrue() {
        String localPath = jobNodePath.getFullPath(getInstanceNodePath(JOB_INSTANCE_ID));
        assertTrue(invokeIsLocalInstancePath(localPath));
    }

    @Test
    public void testIsLocalInstancePathFalse() {
        String nonLocalPath = jobNodePath.getFullPath(getInstanceNodePath("nonExistentInstanceId"));
        assertFalse(invokeIsLocalInstancePath(nonLocalPath));
    }

    private boolean invokeIsLocalInstancePath(String path) {
        try {
            java.lang.reflect.Method method = InstanceNode.class.getDeclaredMethod("isLocalInstancePath", String.class);
            method.setAccessible(true);
            return (boolean) method.invoke(instanceNode, path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getInstanceNodePath(String instanceId) {
        try {
            Field instancesField = InstanceNode.class.getDeclaredField("INSTANCES");
            instancesField.setAccessible(true);
            String instancesFormat = (String) instancesField.get(null);
            return String.format(instancesFormat, instanceId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
