package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.internal.schedule.JobRegistry;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

public class JobRegistry_getCurrentShardingTotalCount_8_0_Test {

    private JobRegistry jobRegistry;

    @Before
    public void setUp() throws Exception {
        // Use reflection to access the private constructor and create an instance
        Field instanceField = JobRegistry.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        // Ensure the instance is null before creating a new one
        instanceField.set(null, null);
        jobRegistry = JobRegistry.getInstance();
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobExists() throws Exception {
        String jobName = "testJob";
        int expectedCount = 5;
        // Use reflection to access the private currentShardingTotalCountMap
        Field currentShardingTotalCountMapField = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        currentShardingTotalCountMapField.setAccessible(true);
        Map<String, Integer> currentShardingTotalCountMap = (ConcurrentHashMap<String, Integer>) currentShardingTotalCountMapField.get(jobRegistry);
        // Set the expected sharding total count for the job
        currentShardingTotalCountMap.put(jobName, expectedCount);
        // Assert that the method returns the expected count
        assertEquals(expectedCount, jobRegistry.getCurrentShardingTotalCount(jobName));
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobDoesNotExist() {
        String jobName = "nonExistentJob";
        // Assert that the method returns 0 when the job does not exist in the map
        assertEquals(0, jobRegistry.getCurrentShardingTotalCount(jobName));
    }
}
