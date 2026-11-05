package io.elasticjob.lite.internal.schedule;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class JobRegistry_getCurrentShardingTotalCount_0_0_Test {
    private JobRegistry jobRegistry;

    @Before
    public void setUp() throws Exception {
        jobRegistry = JobRegistry.getInstance();
        resetSingleton();
    }

    private void resetSingleton() throws Exception {
        Field instanceField = JobRegistry.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    public void testGetCurrentShardingTotalCountExists() throws Exception {
        String jobName = "testJob";
        int expectedCount = 5;

        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        Map<String, Integer> map = (Map<String, Integer>) field.get(jobRegistry);
        map.put(jobName, expectedCount);

        int actualCount = jobRegistry.getCurrentShardingTotalCount(jobName);
        assertEquals(expectedCount, actualCount);
    }

    @Test
    public void testGetCurrentShardingTotalCountNotExists() {
        String jobName = "nonExistentJob";

        int actualCount = jobRegistry.getCurrentShardingTotalCount(jobName);
        assertEquals(0, actualCount);
    }
}
