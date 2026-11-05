package io.elasticjob.lite.internal.failover;

import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Method;
import io.elasticjob.lite.internal.failover.FailoverNode;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.elasticjob.lite.internal.election.LeaderNode;
import io.elasticjob.lite.internal.sharding.ShardingNode;
import io.elasticjob.lite.internal.storage.JobNodePath;

public class FailoverNode_getExecutionFailoverNode_1_0_Test {

    private FailoverNode failoverNode;

    @Before
    public void setUp() {
        failoverNode = new FailoverNode("testJob");
    }

    @Test
    public void testGetExecutionFailoverNode() throws Exception {
        Method method = FailoverNode.class.getDeclaredMethod("getExecutionFailoverNode", int.class);
        method.setAccessible(true);
        int item = 1;
        String expected = "sharding/%s/failover";
        String expectedFormatted = String.format(expected, item);
        String result = (String) method.invoke(null, item);
        assertEquals(expectedFormatted, result);
    }
}
