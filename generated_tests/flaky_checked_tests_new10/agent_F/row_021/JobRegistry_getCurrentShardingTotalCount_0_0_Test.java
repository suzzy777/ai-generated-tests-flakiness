package io.elasticjob.lite.internal.schedule;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JobRegistry_getCurrentShardingTotalCount_0_0_Test {

    private JobRegistry jobRegistry;

    @Before
    public void setUp() {
        jobRegistry = JobRegistry.getInstance();
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenNull() {
        assertThat(jobRegistry.getCurrentShardingTotalCount("non_existing_job"), is(0));
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenNotNull() {
        jobRegistry.setCurrentShardingTotalCount("existing_job", 5);
        assertThat(jobRegistry.getCurrentShardingTotalCount("existing_job"), is(5));
    }
}
