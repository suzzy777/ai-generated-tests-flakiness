package io.elasticjob.lite.internal.failover;

import io.elasticjob.lite.internal.failover.FailoverService;
import io.elasticjob.lite.internal.storage.JobNodeStorage;
import io.elasticjob.lite.internal.failover.FailoverNode;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.schedule.JobScheduleController;
import io.elasticjob.lite.internal.sharding.ShardingNode;
import io.elasticjob.lite.internal.sharding.ShardingService;
import io.elasticjob.lite.internal.storage.LeaderExecutionCallback;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class FailoverService_removeFailoverInfo_8_0_Test {

    @Mock
    private CoordinatorRegistryCenter regCenter;

    @Mock
    private ShardingService shardingService;

    @Mock
    private JobNodeStorage jobNodeStorage;

    private FailoverService failoverService;

    private static final String JOB_NAME = "testJob";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        failoverService = new FailoverService(regCenter, JOB_NAME);
        // Use reflection to set the private jobNodeStorage field
        try {
            java.lang.reflect.Field field = FailoverService.class.getDeclaredField("jobNodeStorage");
            field.setAccessible(true);
            field.set(failoverService, jobNodeStorage);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testRemoveFailoverInfo() {
        List<String> childrenKeys = Arrays.asList("0", "1", "2");
        when(jobNodeStorage.getJobNodeChildrenKeys(ShardingNode.ROOT)).thenReturn(childrenKeys);
        failoverService.removeFailoverInfo();
        for (String each : childrenKeys) {
            verify(jobNodeStorage).removeJobNodeIfExisted(FailoverNode.getExecutionFailoverNode(Integer.parseInt(each)));
        }
    }
}
