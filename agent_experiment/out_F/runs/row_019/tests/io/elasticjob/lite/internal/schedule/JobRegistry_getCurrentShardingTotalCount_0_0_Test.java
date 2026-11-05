package io.elasticjob.lite.internal.schedule;

import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;

public class JobRegistry_getCurrentShardingTotalCount_0_0_Test {

    private JobRegistry jobRegistry;

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();
        Field instanceField = JobRegistry.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, jobRegistry);
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobNameExists() throws Exception {
        String jobName = "testJob";
        int shardingTotalCount = 5;
        setCurrentShardingTotalCount(jobName, shardingTotalCount);

        int result = jobRegistry.getCurrentShardingTotalCount(jobName);
        assertEquals(shardingTotalCount, result);
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobNameDoesNotExist() {
        String jobName = "nonExistentJob";

        int result = jobRegistry.getCurrentShardingTotalCount(jobName);
        assertEquals(0, result);
    }

    private void setCurrentShardingTotalCount(String jobName, int count) throws Exception {
        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        Map<String, Integer> map = (Map<String, Integer>) field.get(jobRegistry);
        map.put(jobName, count);
    }
}
