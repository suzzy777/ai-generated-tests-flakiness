package io.elasticjob.lite.internal.schedule;

import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;

public class JobRegistry_getCurrentShardingTotalCount_0_0_Test {
    private JobRegistry jobRegistry;

    @Before
    public void setUp() throws Exception {
        // Use reflection to set the private static instance field to a new JobRegistry instance
        Field instanceField = JobRegistry.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        
        Constructor<JobRegistry> constructor = JobRegistry.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        instanceField.set(null, constructor.newInstance());
        
        jobRegistry = JobRegistry.getInstance();
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobNameExists() throws Exception {
        // Use reflection to access the private currentShardingTotalCountMap field
        Field shardingTotalCountMapField = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        shardingTotalCountMapField.setAccessible(true);
        
        ConcurrentHashMap<String, Integer> shardingTotalCountMap = new ConcurrentHashMap<>();
        shardingTotalCountMap.put("testJob", 5);
        shardingTotalCountMapField.set(jobRegistry, shardingTotalCountMap);

        assertEquals(5, jobRegistry.getCurrentShardingTotalCount("testJob"));
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobNameDoesNotExist() {
        assertEquals(0, jobRegistry.getCurrentShardingTotalCount("nonExistentJob"));
    }
}
