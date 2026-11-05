package io.elasticjob.lite.internal.schedule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;

public class JobRegistry_getCurrentShardingTotalCount_0_0_Test {

    private JobRegistry jobRegistry;
    private Map<String, Integer> originalShardingTotalCountMap;

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();
        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        originalShardingTotalCountMap = new ConcurrentHashMap<>((Map<String, Integer>) field.get(jobRegistry));
    }

    @After
    public void tearDown() throws Exception {
        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        field.set(jobRegistry, originalShardingTotalCountMap);
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobNameExists() throws Exception {
        String jobName = "testJob";
        int shardingTotalCount = 5;
        jobRegistry.setCurrentShardingTotalCount(jobName, shardingTotalCount);
        assertEquals(shardingTotalCount, jobRegistry.getCurrentShardingTotalCount(jobName));
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobNameDoesNotExist() {
        String jobName = "nonExistentJob";
        assertEquals(0, jobRegistry.getCurrentShardingTotalCount(jobName));
    }
}
