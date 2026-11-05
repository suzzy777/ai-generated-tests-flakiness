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
        // Save the original state of the currentShardingTotalCountMap
        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        originalCurrentShardingTotalCountMap = new ConcurrentHashMap<>((Map<String, Integer>) field.get(jobRegistry));
        // Set up a new map for testing
        Map<String, Integer> testMap = new ConcurrentHashMap<>();
        testMap.put("testJob1", 5);
        testMap.put("testJob2", 10);
        field.set(jobRegistry, testMap);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original state of the currentShardingTotalCountMap
        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        field.set(jobRegistry, originalCurrentShardingTotalCountMap);
    }

    @Test
    public void testGetCurrentShardingTotalCountExists() {
        int result = jobRegistry.getCurrentShardingTotalCount("testJob1");
        assertEquals(5, result);
    }

    @Test
    public void testGetCurrentShardingTotalCountNotExists() {
        int result = jobRegistry.getCurrentShardingTotalCount("nonExistentJob");
        assertEquals(0, result);
    }
}
