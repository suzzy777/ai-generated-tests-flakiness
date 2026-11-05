package io.elasticjob.lite.internal.schedule;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;

public class JobRegistry_getCurrentShardingTotalCount_0_0_Test {

    private JobRegistry jobRegistry;

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();
        setPrivateField(jobRegistry, "currentShardingTotalCountMap", new ConcurrentHashMap<String, Integer>());
    }

    @SuppressWarnings("unchecked")
    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobNameExists() throws Exception {
        String jobName = "testJob";
        int shardingCount = 5;
        jobRegistry.setCurrentShardingTotalCount(jobName, shardingCount);
        assertEquals(shardingCount, jobRegistry.getCurrentShardingTotalCount(jobName));
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobNameDoesNotExist() {
        String jobName = "nonExistentJob";
        assertEquals(0, jobRegistry.getCurrentShardingTotalCount(jobName));
    }
}
