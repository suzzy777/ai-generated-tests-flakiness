package io.elasticjob.lite.internal.server;

import io.elasticjob.lite.internal.server.ServerService;
import io.elasticjob.lite.internal.server.ServerNode;
import io.elasticjob.lite.internal.storage.JobNodeStorage;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.junit.After;
import org.junit.Assert;
import io.elasticjob.lite.internal.instance.InstanceNode;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import java.util.List;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ServerService_isEnableServer_4_0_Test {

    private CoordinatorRegistryCenter regCenter;

    private ServerService serverService;

    private ServerNode serverNode;

    private final String jobName = "testJob";

    private JobNodeStorage jobNodeStorage;

    @Before
    public void setUp() throws Exception {
        // Initialize CoordinatorRegistryCenter mock
        regCenter = mock(CoordinatorRegistryCenter.class);
        // Use reflection to create an instance of ServerService
        Constructor<ServerService> constructor = ServerService.class.getDeclaredConstructor(CoordinatorRegistryCenter.class, String.class);
        constructor.setAccessible(true);
        // Initialize ServerService instance
        serverService = constructor.newInstance(regCenter, jobName);
        // Initialize JobNodeStorage using reflection
        Constructor<JobNodeStorage> jobNodeStorageConstructor = JobNodeStorage.class.getDeclaredConstructor(CoordinatorRegistryCenter.class, String.class);
        jobNodeStorageConstructor.setAccessible(true);
        jobNodeStorage = jobNodeStorageConstructor.newInstance(regCenter, jobName);
        // Use reflection to set the private fields
        Field jobNodeStorageField = ServerService.class.getDeclaredField("jobNodeStorage");
        jobNodeStorageField.setAccessible(true);
        jobNodeStorageField.set(serverService, jobNodeStorage);
        Field serverNodeField = ServerService.class.getDeclaredField("serverNode");
        serverNodeField.setAccessible(true);
        serverNode = new ServerNode(jobName);
        serverNodeField.set(serverService, serverNode);
    }

    @Test
    public void testIsEnableServerWhenServerIsDisabled() throws Exception {
        String ip = "127.0.0.1";
        String serverNodePath = serverNode.getServerNode(ip);
        // Use reflection to set the private field directly
        Field jobNodeStorageField = ServerService.class.getDeclaredField("jobNodeStorage");
        jobNodeStorageField.setAccessible(true);
        JobNodeStorage realJobNodeStorage = (JobNodeStorage) jobNodeStorageField.get(serverService);
        // Mock the behavior of the method getJobNodeData
        when(realJobNodeStorage.getJobNodeData(serverNodePath)).thenReturn(ServerStatus.DISABLED.name());
        assertFalse(serverService.isEnableServer(ip));
    }

    @Test
    public void testIsEnableServerWhenServerIsEnabled() throws Exception {
        String ip = "127.0.0.1";
        String serverNodePath = serverNode.getServerNode(ip);
        // Use reflection to set the private field directly
        Field jobNodeStorageField = ServerService.class.getDeclaredField("jobNodeStorage");
        jobNodeStorageField.setAccessible(true);
        JobNodeStorage realJobNodeStorage = (JobNodeStorage) jobNodeStorageField.get(serverService);
        // Mock the behavior of the method getJobNodeData
        when(realJobNodeStorage.getJobNodeData(serverNodePath)).thenReturn("ENABLED");
        assertTrue(serverService.isEnableServer(ip));
    }
}
