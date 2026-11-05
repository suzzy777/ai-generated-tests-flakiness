package io.elasticjob.lite.internal.schedule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JobRegistry_getCurrentShardingTotalCount_0_0_Test {

    private JobRegistry jobRegistry;
    private Integer originalValue;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        jobRegistry = JobRegistry.getInstance();
        // Save the original value if exists
        originalValue = jobRegistry.getCurrentShardingTotalCount("test_job");
        // Set up the state for testing
        jobRegistry.setCurrentShardingTotalCount("test_job", 10);
    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        // Restore the original value
        if (originalValue == null) {
            jobRegistry.setCurrentShardingTotalCount("test_job", 0);
        } else {
            jobRegistry.setCurrentShardingTotalCount("test_job", originalValue);
        }
    }

    @Test
    public void testGetCurrentShardingTotalCount() {
        assertThat(jobRegistry.getCurrentShardingTotalCount("test_job"), is(10));
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenNull() {
        jobRegistry.setCurrentShardingTotalCount("null_job", 0);
        assertThat(jobRegistry.getCurrentShardingTotalCount("null_job"), is(0));
    }
}
