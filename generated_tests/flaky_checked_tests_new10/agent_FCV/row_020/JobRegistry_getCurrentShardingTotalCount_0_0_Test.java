package io.elasticjob.lite.internal.schedule;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;

public class JobRegistry_getCurrentShardingTotalCount_0_0_Test {
    private JobRegistry jobRegistry;
    private Map<String, Integer> originalCurrentShardingTotalCountMap;

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();
        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        originalCurrentShardingTotalCountMap = new ConcurrentHashMap<>((Map<String, Integer>) field.get(jobRegistry));
    }

    @After
    public void tearDown() throws Exception {
        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        field.set(jobRegistry, originalCurrentShardingTotalCountMap);
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobExists() {
        String jobName = "testJob";
        int shardingCount = 5;
        jobRegistry.setCurrentShardingTotalCount(jobName, shardingCount);
        assertEquals(shardingCount, jobRegistry.getCurrentShardingTotalCount(jobName));
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobDoesNotExist() {
        String jobName = "nonExistentJob";
        assertEquals(0, jobRegistry.getCurrentShardingTotalCount(jobName));
    }
}
