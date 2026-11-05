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
        jobRegistry = JobRegistry.getInstance();
        // Use reflection to clear the currentShardingTotalCountMap before each test
        Field currentShardingTotalCountMapField = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        currentShardingTotalCountMapField.setAccessible(true);
        Map<String, Integer> currentShardingTotalCountMap = (ConcurrentHashMap<String, Integer>) currentShardingTotalCountMapField.get(jobRegistry);
        currentShardingTotalCountMap.clear();
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobExists() throws Exception {
        // Use reflection to access the private method setCurrentShardingTotalCount
        jobRegistry.setCurrentShardingTotalCount("testJob", 5);
        int shardingTotalCount = jobRegistry.getCurrentShardingTotalCount("testJob");
        assertEquals(5, shardingTotalCount);
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobDoesNotExist() {
        int shardingTotalCount = jobRegistry.getCurrentShardingTotalCount("nonExistentJob");
        assertEquals(0, shardingTotalCount);
    }
}
