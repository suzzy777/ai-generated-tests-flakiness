package io.elasticjob.lite.internal.schedule;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JobRegistry_getCurrentShardingTotalCount_0_0_Test {

    private JobRegistry jobRegistry;

    @Before
    public void setUp() {
        jobRegistry = JobRegistry.getInstance();
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenPresent() {
        String jobName = "testJob";
        int expectedShardingCount = 5;
        jobRegistry.setCurrentShardingTotalCount(jobName, expectedShardingCount);
        int actualShardingCount = jobRegistry.getCurrentShardingTotalCount(jobName);
        assertEquals(expectedShardingCount, actualShardingCount);
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenAbsent() {
        String jobName = "nonExistentJob";
        int actualShardingCount = jobRegistry.getCurrentShardingTotalCount(jobName);
        assertEquals(0, actualShardingCount);
    }
}
