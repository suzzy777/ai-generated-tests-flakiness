package io.elasticjob.lite.internal.failover;

import io.elasticjob.lite.internal.failover.FailoverService;
import io.elasticjob.lite.internal.storage.JobNodeStorage;
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
public class FailoverService_getFailoverItems_5_0_Test {

    private FailoverService failoverService;

    @Mock
    private JobNodeStorage jobNodeStorage;

    private static final String JOB_NAME = "testJob";

    private static final String JOB_INSTANCE_ID = "instance_1";

    @Before
    public void setUp() {
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        failoverService = new FailoverService(regCenter, JOB_NAME);
        // Use reflection to set the private field 'jobNodeStorage'
        try {
            java.lang.reflect.Field jobNodeStorageField = FailoverService.class.getDeclaredField("jobNodeStorage");
            jobNodeStorageField.setAccessible(true);
            jobNodeStorageField.set(failoverService, jobNodeStorage);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
    }

    @Test
    public void testGetFailoverItemsWithMatchingInstanceId() {
        when(jobNodeStorage.getJobNodeChildrenKeys(ShardingNode.ROOT)).thenReturn(Arrays.asList("0", "1", "2"));
        when(jobNodeStorage.isJobNodeExisted(anyString())).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly(anyString())).thenReturn(JOB_INSTANCE_ID);
        List<Integer> result = failoverService.getFailoverItems(JOB_INSTANCE_ID);
        assertEquals(Arrays.asList(0, 1, 2), result);
    }

    @Test
    public void testGetFailoverItemsWithNoMatchingInstanceId() {
        when(jobNodeStorage.getJobNodeChildrenKeys(ShardingNode.ROOT)).thenReturn(Arrays.asList("0", "1", "2"));
        when(jobNodeStorage.isJobNodeExisted(anyString())).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly(anyString())).thenReturn("different_instance");
        List<Integer> result = failoverService.getFailoverItems(JOB_INSTANCE_ID);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetFailoverItemsWithNonExistentNode() {
        when(jobNodeStorage.getJobNodeChildrenKeys(ShardingNode.ROOT)).thenReturn(Arrays.asList("0", "1", "2"));
        when(jobNodeStorage.isJobNodeExisted(anyString())).thenReturn(false);
        List<Integer> result = failoverService.getFailoverItems(JOB_INSTANCE_ID);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetFailoverItems() {
        List<String> jobNodeChildrenKeys = Arrays.asList("0", "1", "2");
        when(jobNodeStorage.getJobNodeChildrenKeys(anyString())).thenReturn(jobNodeChildrenKeys);
        when(jobNodeStorage.isJobNodeExisted(anyString())).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly(anyString())).thenReturn(JOB_INSTANCE_ID);
        List<Integer> failoverItems = failoverService.getFailoverItems(JOB_INSTANCE_ID);
        assertEquals(3, failoverItems.size());
        assertTrue(failoverItems.contains(0));
        assertTrue(failoverItems.contains(1));
        assertTrue(failoverItems.contains(2));
        verify(jobNodeStorage, times(1)).getJobNodeChildrenKeys(anyString());
        verify(jobNodeStorage, times(3)).isJobNodeExisted(anyString());
        verify(jobNodeStorage, times(3)).getJobNodeDataDirectly(anyString());
    }
}
