package io.elasticjob.lite.reg.zookeeper;

import io.elasticjob.lite.reg.exception.RegExceptionHandler;
import io.elasticjob.lite.reg.zookeeper.ZookeeperConfiguration;
import io.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.ProtectACLCreateModePathAndBytesable;
import org.apache.curator.framework.api.ACLBackgroundPathAndBytesable;
import org.apache.zookeeper.CreateMode;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ZookeeperRegistryCenter_persistSequential_12_0_Test {

    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    @Mock
    private CuratorFramework client;

    @Mock
    private CreateBuilder createBuilder;

    @Mock
    private ProtectACLCreateModePathAndBytesable<String> protectACLCreateModePathAndBytesable;

    @Mock
    private ACLBackgroundPathAndBytesable<String> aclBackgroundPathAndBytesable;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("localhost:2181", "namespace");
        zookeeperRegistryCenter = new ZookeeperRegistryCenter(zkConfig);
        // Use reflection to set the private field 'client'
        Field clientField = ZookeeperRegistryCenter.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(zookeeperRegistryCenter, client);
        when(client.create()).thenReturn(createBuilder);
        when(createBuilder.creatingParentsIfNeeded()).thenReturn(protectACLCreateModePathAndBytesable);
        when(protectACLCreateModePathAndBytesable.withMode(CreateMode.PERSISTENT_SEQUENTIAL)).thenReturn(aclBackgroundPathAndBytesable);
    }

    @Test
    public void testPersistSequentialSuccess() throws Exception {
        String key = "/test/key";
        String value = "testValue";
        String expectedPath = "/test/key0000000001";
        when(aclBackgroundPathAndBytesable.forPath(eq(key), eq(value.getBytes()))).thenReturn(expectedPath);
        String actualPath = zookeeperRegistryCenter.persistSequential(key, value);
        assertEquals(expectedPath, actualPath);
    }
}
