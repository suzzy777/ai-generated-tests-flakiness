package io.elasticjob.lite.internal.storage;

import io.elasticjob.lite.internal.storage.JobNodeStorage;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import io.elasticjob.lite.internal.storage.JobNodePath;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.elasticjob.lite.exception.JobSystemException;
import io.elasticjob.lite.reg.exception.RegExceptionHandler;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.state.ConnectionStateListener;
import java.util.List;

public class JobNodeStorage_updateJobNode_9_0_Test {

    @Mock
    private CoordinatorRegistryCenter regCenter;

    private JobNodePath jobNodePath;

    private JobNodeStorage jobNodeStorage;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jobNodePath = new JobNodePath("testJob");
        jobNodeStorage = new JobNodeStorage(regCenter, "testJob");
    }

    @Test
    public void testUpdateJobNode() throws Exception {
        // Arrange
        String node = "config";
        Object value = "newValue";
        String expectedPath = "/testJob/config";
        // Act
        jobNodeStorage.updateJobNode(node, value);
        // Assert
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(regCenter, times(1)).update(pathCaptor.capture(), valueCaptor.capture());
        assertEquals(expectedPath, pathCaptor.getValue());
        assertEquals(value.toString(), valueCaptor.getValue());
    }
}
