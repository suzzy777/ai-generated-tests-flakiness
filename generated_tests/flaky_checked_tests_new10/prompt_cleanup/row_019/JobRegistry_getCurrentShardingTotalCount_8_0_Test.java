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
        jobRegistry = JobRegistry.getInstance();
        // Use reflection to access the private field
        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        // Save the original state
        originalCurrentShardingTotalCountMap = new ConcurrentHashMap<>((Map<String, Integer>) field.get(jobRegistry));
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original state
        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        field.set(jobRegistry, originalCurrentShardingTotalCountMap);
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobExists() throws Exception {
        String jobName = "testJob";
        int shardingCount = 5;
        // Use reflection to set the value
        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        Map<String, Integer> map = (Map<String, Integer>) field.get(jobRegistry);
        map.put(jobName, shardingCount);
        int result = jobRegistry.getCurrentShardingTotalCount(jobName);
        assertEquals(shardingCount, result);
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobDoesNotExist() {
        String jobName = "nonExistentJob";
        int result = jobRegistry.getCurrentShardingTotalCount(jobName);
        assertEquals(0, result);
    }
}
