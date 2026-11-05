package io.elasticjob.lite.internal.schedule;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
    public void testGetCurrentShardingTotalCountWhenNull() {
        assertThat(jobRegistry.getCurrentShardingTotalCount("non_existent_job"), is(0));
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenNotNull() throws Exception {
        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        Map<String, Integer> currentShardingTotalCountMap = (Map<String, Integer>) field.get(jobRegistry);
        currentShardingTotalCountMap.put("existent_job", 5);
        assertThat(jobRegistry.getCurrentShardingTotalCount("existent_job"), is(5));
    }
}
