package io.elasticjob.lite.internal.sharding;

import io.elasticjob.lite.internal.sharding.ShardingNode;
import io.elasticjob.lite.internal.storage.JobNodeStorage;
import io.elasticjob.lite.internal.sharding.ExecutionService;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.elasticjob.lite.config.LiteJobConfiguration;
import io.elasticjob.lite.executor.ShardingContexts;
import io.elasticjob.lite.internal.config.ConfigurationService;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import java.util.ArrayList;
import java.util.List;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionService_clearMisfire_10_0_Test {

    @Mock
    private CoordinatorRegistryCenter regCenter;

    @Mock
    private ConfigurationService configService;

    private ExecutionService executionService;

    private JobNodeStorage jobNodeStorage;

    private static final String JOB_NAME = "test_job";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        executionService = new ExecutionService(regCenter, JOB_NAME);
        // Use reflection to set the private field jobNodeStorage
        try {
            java.lang.reflect.Field jobNodeStorageField = ExecutionService.class.getDeclaredField("jobNodeStorage");
            jobNodeStorageField.setAccessible(true);
            jobNodeStorage = spy(new JobNodeStorage(regCenter, JOB_NAME));
            jobNodeStorageField.set(executionService, jobNodeStorage);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testClearMisfire() {
        Collection<Integer> items = Arrays.asList(0, 1, 2);
        executionService.clearMisfire(items);
        for (int each : items) {
            verify(jobNodeStorage).removeJobNodeIfExisted(ShardingNode.getMisfireNode(each));
        }
    }
}
