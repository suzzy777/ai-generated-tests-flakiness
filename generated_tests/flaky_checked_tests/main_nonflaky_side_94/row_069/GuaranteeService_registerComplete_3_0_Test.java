package io.elasticjob.lite.internal.guarantee;

import io.elasticjob.lite.internal.guarantee.GuaranteeService;
import io.elasticjob.lite.internal.storage.JobNodeStorage;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.elasticjob.lite.internal.config.ConfigurationService;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class GuaranteeService_registerComplete_3_0_Test {

    private GuaranteeService guaranteeService;

    @Mock
    private JobNodeStorage jobNodeStorageMock;

    @Before
    public void setUp() throws Exception {
        // Mock the CoordinatorRegistryCenter
        CoordinatorRegistryCenter regCenterMock = mock(CoordinatorRegistryCenter.class);
        // Create an instance of GuaranteeService using reflection
        Constructor<GuaranteeService> constructor = GuaranteeService.class.getDeclaredConstructor(CoordinatorRegistryCenter.class, String.class);
        constructor.setAccessible(true);
        guaranteeService = constructor.newInstance(regCenterMock, "testJob");
        // Inject the mock into GuaranteeService
        Field jobNodeStorageField = GuaranteeService.class.getDeclaredField("jobNodeStorage");
        jobNodeStorageField.setAccessible(true);
        jobNodeStorageField.set(guaranteeService, jobNodeStorageMock);
    }

    @Test
    public void testRegisterComplete() {
        Collection<Integer> shardingItems = Arrays.asList(0, 1, 2);
        guaranteeService.registerComplete(shardingItems);
        for (int each : shardingItems) {
            verify(jobNodeStorageMock).createJobNodeIfNeeded(GuaranteeNode.getCompletedNode(each));
        }
    }
}
