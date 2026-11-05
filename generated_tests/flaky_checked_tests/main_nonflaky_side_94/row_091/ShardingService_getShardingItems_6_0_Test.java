package io.elasticjob.lite.internal.sharding;

import io.elasticjob.lite.internal.sharding.ShardingService;
import io.elasticjob.lite.internal.config.ConfigurationService;
import io.elasticjob.lite.internal.server.ServerService;
import io.elasticjob.lite.internal.storage.JobNodeStorage;
import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.config.LiteJobConfiguration;
import io.elasticjob.lite.config.JobCoreConfiguration;
import io.elasticjob.lite.config.JobTypeConfiguration;
import io.elasticjob.lite.internal.sharding.ShardingNode;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.elasticjob.lite.api.strategy.JobShardingStrategy;
import io.elasticjob.lite.api.strategy.JobShardingStrategyFactory;
import io.elasticjob.lite.internal.election.LeaderService;
import io.elasticjob.lite.internal.instance.InstanceNode;
import io.elasticjob.lite.internal.instance.InstanceService;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.storage.JobNodePath;
import io.elasticjob.lite.internal.storage.TransactionExecutionCallback;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import io.elasticjob.lite.util.concurrent.BlockUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import java.util.LinkedList;
import java.util.Map;
import java.util.Arrays;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ShardingService_getShardingItems_6_0_Test {

    @Mock
    private JobNodeStorage jobNodeStorage;

    @Mock
    private ServerService serverService;

    @Mock
    private ConfigurationService configService;

    private ShardingService shardingService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        shardingService = new ShardingService(null, "testJob");
        setPrivateField(shardingService, "jobNodeStorage", jobNodeStorage);
        setPrivateField(shardingService, "serverService", serverService);
        setPrivateField(shardingService, "configService", configService);
    }

    @Test
    public void testGetShardingItemsWhenServerIsNotAvailable() {
        JobInstance jobInstance = new JobInstance("127.0.0.1@-@12345");
        when(serverService.isAvailableServer(jobInstance.getIp())).thenReturn(false);
        List<Integer> result = shardingService.getShardingItems(jobInstance.getJobInstanceId());
        assertEquals(Collections.emptyList(), result);
        verify(serverService).isAvailableServer(jobInstance.getIp());
        verifyNoMoreInteractions(serverService, configService, jobNodeStorage);
    }

    @Test
    public void testGetShardingItemsWhenServerIsAvailable() {
        JobInstance jobInstance = new JobInstance("127.0.0.1@-@12345");
        when(serverService.isAvailableServer(jobInstance.getIp())).thenReturn(true);
        LiteJobConfiguration liteJobConfig = mock(LiteJobConfiguration.class);
        JobTypeConfiguration jobTypeConfig = mock(JobTypeConfiguration.class);
        JobCoreConfiguration jobCoreConfig = mock(JobCoreConfiguration.class);
        when(configService.load(true)).thenReturn(liteJobConfig);
        when(liteJobConfig.getTypeConfig()).thenReturn(jobTypeConfig);
        when(jobTypeConfig.getCoreConfig()).thenReturn(jobCoreConfig);
        when(jobCoreConfig.getShardingTotalCount()).thenReturn(3);
        when(jobNodeStorage.getJobNodeData(ShardingNode.getInstanceNode(0))).thenReturn("127.0.0.1@-@12345");
        when(jobNodeStorage.getJobNodeData(ShardingNode.getInstanceNode(1))).thenReturn("127.0.0.1@-@12345");
        when(jobNodeStorage.getJobNodeData(ShardingNode.getInstanceNode(2))).thenReturn("127.0.0.2@-@12345");
        List<Integer> result = shardingService.getShardingItems(jobInstance.getJobInstanceId());
        assertEquals(Arrays.asList(0, 1), result);
        verify(serverService).isAvailableServer(jobInstance.getIp());
        verify(configService).load(true);
        verify(jobNodeStorage, times(3)).getJobNodeData(anyString());
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}
