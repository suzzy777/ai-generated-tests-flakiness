package io.elasticjob.lite.reg.zookeeper;

import io.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import io.elasticjob.lite.reg.zookeeper.ZookeeperConfiguration;
import io.elasticjob.lite.reg.exception.RegExceptionHandler;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.DeleteBuilder;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
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
import org.apache.zookeeper.CreateMode;
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

public class ZookeeperRegistryCenter_remove_14_3_Test {

    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    private CuratorFramework client;

    @Before
    public void setUp() throws Exception {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("localhost:2181", "namespace");
        zookeeperRegistryCenter = new ZookeeperRegistryCenter(zkConfig);
        client = mock(CuratorFramework.class);
        // Use reflection to set the private client field
        Field clientField = ZookeeperRegistryCenter.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(zookeeperRegistryCenter, client);
    }

    @Test
    public void testRemoveSuccess() throws Exception {
        String key = "/test/key";
        DeleteBuilder deleteBuilder = mock(DeleteBuilder.class);
        when(client.delete()).thenReturn(deleteBuilder);
        when(deleteBuilder.deletingChildrenIfNeeded()).thenReturn(deleteBuilder);
        when(deleteBuilder.forPath(key)).thenReturn(null);
        zookeeperRegistryCenter.remove(key);
        verify(deleteBuilder, times(1)).forPath(key);
    }
}
