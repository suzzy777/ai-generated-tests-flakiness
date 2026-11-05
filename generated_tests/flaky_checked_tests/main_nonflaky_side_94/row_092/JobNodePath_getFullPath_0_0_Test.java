package io.elasticjob.lite.internal.storage;

import io.elasticjob.lite.internal.storage.JobNodePath;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import lombok.RequiredArgsConstructor;

public class JobNodePath_getFullPath_0_0_Test {

    private JobNodePath jobNodePath;

    @Before
    public void setUp() {
        jobNodePath = new JobNodePath("testJob");
    }

    @Test
    public void testGetFullPath() throws Exception {
        // Use reflection to access the private getFullPath method
        Method getFullPathMethod = JobNodePath.class.getDeclaredMethod("getFullPath", String.class);
        getFullPathMethod.setAccessible(true);
        // Test with different node values
        String node1 = "node1";
        String expectedPath1 = "/testJob/node1";
        String actualPath1 = (String) getFullPathMethod.invoke(jobNodePath, node1);
        assertEquals(expectedPath1, actualPath1);
        String node2 = "node2";
        String expectedPath2 = "/testJob/node2";
        String actualPath2 = (String) getFullPathMethod.invoke(jobNodePath, node2);
        assertEquals(expectedPath2, actualPath2);
        // Test with an empty node
        String emptyNode = "";
        String expectedEmptyPath = "/testJob/";
        String actualEmptyPath = (String) getFullPathMethod.invoke(jobNodePath, emptyNode);
        assertEquals(expectedEmptyPath, actualEmptyPath);
        // Test with a null node, expecting a NullPointerException
        try {
            getFullPathMethod.invoke(jobNodePath, (String) null);
        } catch (Exception e) {
            assertEquals(NullPointerException.class, e.getCause().getClass());
        }
    }
}
