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
    private Map<String, Integer> originalCurrentShardingTotalCountMap;

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();

        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        originalCurrentShardingTotalCountMap = new ConcurrentHashMap<>((Map<String, Integer>) field.get(jobRegistry));

        // Set up the state needed for the tests
        Map<String, Integer> currentShardingTotalCountMap = new ConcurrentHashMap<>();
        currentShardingTotalCountMap.put("testJob", 5);
        field.set(jobRegistry, currentShardingTotalCountMap);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original map
        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        field.set(jobRegistry, originalCurrentShardingTotalCountMap);
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobExists() {
        int count = jobRegistry.getCurrentShardingTotalCount("testJob");
        assertEquals(5, count);
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobDoesNotExist() {
        int count = jobRegistry.getCurrentShardingTotalCount("nonExistentJob");
        assertEquals(0, count);
    }
}
