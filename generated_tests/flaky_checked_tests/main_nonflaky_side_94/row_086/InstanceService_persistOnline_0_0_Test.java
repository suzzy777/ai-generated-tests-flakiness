package io.elasticjob.lite.internal.instance;

import io.elasticjob.lite.internal.instance.InstanceService;
import io.elasticjob.lite.internal.storage.JobNodeStorage;
import io.elasticjob.lite.internal.instance.InstanceNode;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.internal.server.ServerService;
import java.util.LinkedList;
import java.util.List;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class InstanceService_persistOnline_0_0_Test {

    private InstanceService instanceService;

    @Mock
    private InstanceNode instanceNodeMock;

    @Mock
    private CoordinatorRegistryCenter regCenterMock;

    @Mock
    private ServerService serverServiceMock;

    @Before
    public void setUp() throws Exception {
        // Create instance of InstanceService
        instanceService = new InstanceService(regCenterMock, "testJob");
        // Use reflection to set private fields
        Field instanceNodeField = InstanceService.class.getDeclaredField("instanceNode");
        instanceNodeField.setAccessible(true);
        instanceNodeField.set(instanceService, instanceNodeMock);
        Field serverServiceField = InstanceService.class.getDeclaredField("serverService");
        serverServiceField.setAccessible(true);
        serverServiceField.set(instanceService, serverServiceMock);
        // Mock InstanceNode behavior
        Mockito.when(instanceNodeMock.getLocalInstanceNode()).thenReturn("instances/testInstance");
        // Use reflection to set JobNodeStorage
        Field jobNodeStorageField = InstanceService.class.getDeclaredField("jobNodeStorage");
        jobNodeStorageField.setAccessible(true);
        jobNodeStorageField.set(instanceService, new JobNodeStorage(regCenterMock, "testJob"));
    }

    @Test
    public void testPersistOnline() throws Exception {
        // Use reflection to get the private method
        JobNodeStorage jobNodeStorage = spy(new JobNodeStorage(regCenterMock, "testJob"));
        Field jobNodeStorageField = InstanceService.class.getDeclaredField("jobNodeStorage");
        jobNodeStorageField.setAccessible(true);
        jobNodeStorageField.set(instanceService, jobNodeStorage);
        // Invoke the method under test
        instanceService.persistOnline();
        // Verify the interaction with the mocked JobNodeStorage
        verify(jobNodeStorage).fillEphemeralJobNode("instances/testInstance", "");
    }
}
