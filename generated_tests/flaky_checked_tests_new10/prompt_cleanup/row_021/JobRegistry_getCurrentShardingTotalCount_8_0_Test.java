package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.internal.schedule.JobRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.mockito.*;
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

    private Map<String, Integer> originalCurrentShardingTotalCountMap;

    @Before
    public void setUp() throws Exception {
        // Access the singleton instance of JobRegistry
        jobRegistry = JobRegistry.getInstance();
        // Use reflection to access the private field currentShardingTotalCountMap
        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        // Save the original state of currentShardingTotalCountMap
        originalCurrentShardingTotalCountMap = new ConcurrentHashMap<>((Map<String, Integer>) field.get(jobRegistry));
        // Clear the map for testing
        ((Map<String, Integer>) field.get(jobRegistry)).clear();
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original state of currentShardingTotalCountMap
        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        field.set(jobRegistry, originalCurrentShardingTotalCountMap);
    }

    @Test
    public void testGetCurrentShardingTotalCount_ExistingJob() throws Exception {
        // Arrange
        String jobName = "testJob";
        int expectedShardingCount = 5;
        // Use reflection to access the private field currentShardingTotalCountMap
        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        // Add a test entry to the map
        ((Map<String, Integer>) field.get(jobRegistry)).put(jobName, expectedShardingCount);
        // Act
        int actualShardingCount = jobRegistry.getCurrentShardingTotalCount(jobName);
        // Assert
        assertEquals(expectedShardingCount, actualShardingCount);
    }

    @Test
    public void testGetCurrentShardingTotalCount_NonExistingJob() {
        // Arrange
        String jobName = "nonExistingJob";
        // Act
        int actualShardingCount = jobRegistry.getCurrentShardingTotalCount(jobName);
        // Assert
        assertEquals(0, actualShardingCount);
    }
}
