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
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobNameExists() throws Exception {
        String jobName = "testJob";
        int expectedCount = 5;
        setCurrentShardingTotalCount(jobName, expectedCount);
        int actualCount = jobRegistry.getCurrentShardingTotalCount(jobName);
        assertEquals(expectedCount, actualCount);
    }

    @Test
    public void testGetCurrentShardingTotalCountWhenJobNameDoesNotExist() {
        String jobName = "nonExistentJob";
        int actualCount = jobRegistry.getCurrentShardingTotalCount(jobName);
        assertEquals(0, actualCount);
    }

    private void setCurrentShardingTotalCount(String jobName, int count) throws Exception {
        Field field = JobRegistry.class.getDeclaredField("currentShardingTotalCountMap");
        field.setAccessible(true);
        Map<String, Integer> map = (Map<String, Integer>) field.get(jobRegistry);
        map.put(jobName, count);
    }
}
