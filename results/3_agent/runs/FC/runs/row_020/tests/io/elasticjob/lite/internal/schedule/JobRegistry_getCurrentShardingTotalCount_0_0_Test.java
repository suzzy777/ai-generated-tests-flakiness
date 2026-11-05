package io.elasticjob.lite.internal.schedule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JobRegistry_getCurrentShardingTotalCount_0_0_Test {
    private JobRegistry jobRegistry;
    private Integer originalShardingTotalCount;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        jobRegistry = JobRegistry.getInstance();
        originalShardingTotalCount = getCurrentShardingTotalCount("test_job");
    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        setCurrentShardingTotalCount("test_job", originalShardingTotalCount);
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenNull() {
        assertThat(jobRegistry.getCurrentShardingTotalCount("nonexistent_job"), is(0));
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenNotNull() throws NoSuchFieldException, IllegalAccessException {
        setCurrentShardingTotalCount("test_job", 5);
        assertThat(jobRegistry.getCurrentShardingTotalCount("test_job"), is(5));
    }

    private Integer getCurrentShardingTotalCount(String jobName) throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Integer> map = (java.util.Map<String, Integer>) field.get(jobRegistry);
        return map.get(jobName);
    }

    private void setCurrentShardingTotalCount(String jobName, Integer count) throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Integer> map = (java.util.Map<String, Integer>) field.get(jobRegistry);
        if (count == null) {
            map.remove(jobName);
        } else {
            map.put(jobName, count);
        }
    }
}
