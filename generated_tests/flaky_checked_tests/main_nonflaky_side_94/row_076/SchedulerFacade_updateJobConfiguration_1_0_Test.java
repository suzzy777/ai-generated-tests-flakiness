package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.config.LiteJobConfiguration;
import io.elasticjob.lite.internal.config.ConfigurationService;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import java.lang.reflect.Constructor;
import java.util.List;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.elasticjob.lite.api.listener.ElasticJobListener;
import io.elasticjob.lite.internal.election.LeaderService;
import io.elasticjob.lite.internal.instance.InstanceService;
import io.elasticjob.lite.internal.listener.ListenerManager;
import io.elasticjob.lite.internal.monitor.MonitorService;
import io.elasticjob.lite.internal.reconcile.ReconcileService;
import io.elasticjob.lite.internal.server.ServerService;
import io.elasticjob.lite.internal.sharding.ExecutionService;
import io.elasticjob.lite.internal.sharding.ShardingService;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class SchedulerFacade_updateJobConfiguration_1_0_Test {

    @Mock
    private CoordinatorRegistryCenter regCenter;

    @Mock
    private ConfigurationService configService;

    private SchedulerFacade schedulerFacade;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        schedulerFacade = new SchedulerFacade(regCenter, "testJob");
        setPrivateField(schedulerFacade, "configService", configService);
    }

    private void setPrivateField(Object targetObject, String fieldName, Object fieldValue) throws Exception {
        java.lang.reflect.Field field = targetObject.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(targetObject, fieldValue);
    }

    @Test
    public void testUpdateJobConfiguration() {
        LiteJobConfiguration liteJobConfig = mock(LiteJobConfiguration.class);
        LiteJobConfiguration persistedConfig = mock(LiteJobConfiguration.class);
        when(configService.load(false)).thenReturn(persistedConfig);
        LiteJobConfiguration result = schedulerFacade.updateJobConfiguration(liteJobConfig);
        verify(configService).persist(liteJobConfig);
        assertEquals(persistedConfig, result);
    }
}
