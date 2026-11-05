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
        jobRegistry = JobRegistry.getInstance();
        // Use reflection to clear the singleton instance for testing purposes
        Field instanceField = JobRegistry.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        // Reinitialize the instance
        jobRegistry = JobRegistry.getInstance();
        // Use reflection to access the private map and populate it for testing
        Field currentShardingTotalCountMapField = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        currentShardingTotalCountMapField.setAccessible(true);
        Map<String, Integer> currentShardingTotalCountMap = (Map<String, Integer>) currentShardingTotalCountMapField.get(jobRegistry);
        // Populate the map with test data
        currentShardingTotalCountMap.put("testJob1", 5);
        currentShardingTotalCountMap.put("testJob2", 10);
    }

    @Test
    public void testGetCurrentShardingTotalCountExistingJob() {
        int result = jobRegistry.getCurrentShardingTotalCount("testJob1");
        assertEquals(5, result);
        result = jobRegistry.getCurrentShardingTotalCount("testJob2");
        assertEquals(10, result);
    }

    @Test
    public void testGetCurrentShardingTotalCountNonExistingJob() {
        int result = jobRegistry.getCurrentShardingTotalCount("nonExistingJob");
        assertEquals(0, result);
    }
}
