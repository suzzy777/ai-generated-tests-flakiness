package io.elasticjob.lite.internal.sharding;

import io.elasticjob.lite.internal.sharding.ShardingNode;
import io.elasticjob.lite.internal.storage.JobNodePath;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.elasticjob.lite.internal.election.LeaderNode;

public class ShardingNode_getItemByRunningItemPath_4_0_Test {

    private ShardingNode shardingNode;

    private String jobName = "testJob";

    @Before
    public void setUp() {
        shardingNode = new ShardingNode(jobName);
    }

    @Test
    public void testGetItemByRunningItemPathValidPath() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String validPath = "/testJob/sharding/0/running";
        Method isRunningItemPathMethod = ShardingNode.class.getDeclaredMethod("isRunningItemPath", String.class);
        isRunningItemPathMethod.setAccessible(true);
        boolean isRunningItemPath = (boolean) isRunningItemPathMethod.invoke(shardingNode, validPath);
        assertEquals(true, isRunningItemPath);
        Integer result = shardingNode.getItemByRunningItemPath(validPath);
        assertEquals(Integer.valueOf(0), result);
    }

    @Test
    public void testGetItemByRunningItemPathInvalidPath() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String invalidPath = "/testJob/invalid/path";
        Method isRunningItemPathMethod = ShardingNode.class.getDeclaredMethod("isRunningItemPath", String.class);
        isRunningItemPathMethod.setAccessible(true);
        boolean isRunningItemPath = (boolean) isRunningItemPathMethod.invoke(shardingNode, invalidPath);
        assertEquals(false, isRunningItemPath);
        Integer result = shardingNode.getItemByRunningItemPath(invalidPath);
        assertNull(result);
    }
}
