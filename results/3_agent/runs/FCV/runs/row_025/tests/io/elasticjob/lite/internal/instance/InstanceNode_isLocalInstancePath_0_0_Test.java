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
    private static final String JOB_NAME = "test_job";
    private static final String JOB_INSTANCE_ID = "127.0.0.1@-@12345";
    private JobRegistry originalJobRegistry;
    private Map<String, JobInstance> originalJobInstanceMap;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        originalJobRegistry = JobRegistry.getInstance();
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        originalJobInstanceMap = new ConcurrentHashMap<>((Map<String, JobInstance>) jobInstanceMapField.get(originalJobRegistry));

        JobInstance jobInstance = new JobInstance(JOB_INSTANCE_ID);
        originalJobInstanceMap.put(JOB_NAME, jobInstance);
        jobInstanceMapField.set(originalJobRegistry, originalJobInstanceMap);
    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        Field jobInstanceMapField = JobRegistry.class.getDeclaredField("jobInstanceMap");
        jobInstanceMapField.setAccessible(true);
        jobInstanceMapField.set(originalJobRegistry, new ConcurrentHashMap<>(originalJobInstanceMap));
    }

    @Test
    public void testIsLocalInstancePath() {
        JobNodePath jobNodePath = new JobNodePath(JOB_NAME);
        InstanceNode instanceNode = new InstanceNode(JOB_NAME);

        String validPath = jobNodePath.getFullPath(InstanceNode.ROOT + "/" + JOB_INSTANCE_ID);
        assertTrue(instanceNode.isLocalInstancePath(validPath));

        String invalidPath = jobNodePath.getFullPath(InstanceNode.ROOT + "/invalid_instance_id");
        assertFalse(instanceNode.isLocalInstancePath(invalidPath));
    }
}
