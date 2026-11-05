package cn.crane4j.core.executor.handler;

import cn.crane4j.core.executor.handler.ReflectiveDisassembleOperationHandler;
import cn.crane4j.core.parser.operation.DisassembleOperation;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.Assert;
import org.mockito.junit.MockitoJUnitRunner;
import cn.crane4j.core.util.Asserts;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.runner.RunWith;
import lombok.RequiredArgsConstructor;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ReflectiveDisassembleOperationHandler_process_0_0_Test {

    private ReflectiveDisassembleOperationHandler handler;

    @Mock
    private PropertyOperator propertyOperator;

    @Mock
    private DisassembleOperation operation;

    @Mock
    private MethodInvoker methodInvoker;

    @Before
    public void setUp() {
        handler = new ReflectiveDisassembleOperationHandler(propertyOperator);
    }

    @Test
    public void testProcessWithEmptyTargets() throws Exception {
        Collection<Object> result = handler.process(operation, Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testProcessWithNonEmptyTargets() throws Exception {
        when(operation.getSourceType()).thenReturn((Class) Object.class);
        when(operation.getKey()).thenReturn("key");
        when(propertyOperator.findGetter((Class) Object.class, "key")).thenReturn(methodInvoker);
        when(methodInvoker.invoke(any())).thenReturn("value");
        Collection<Object> targets = Arrays.asList(new Object(), new Object(), null);
        Collection<Object> result = handler.process(operation, targets);
        assertEquals(2, result.size());
        assertTrue(result.contains("value"));
    }

    @Test(expected = cn.crane4j.core.exception.Crane4jException.class)
    public void testProcessWithNullGetter() throws Exception {
        when(operation.getSourceType()).thenReturn((Class) Object.class);
        when(operation.getKey()).thenReturn("key");
        when(propertyOperator.findGetter((Class) Object.class, "key")).thenReturn(null);
        handler.process(operation, Collections.singletonList(new Object()));
    }

    @Test
    public void testProcessWithCollectionItem() throws Exception {
        when(operation.getSourceType()).thenReturn((Class) Object.class);
        when(operation.getKey()).thenReturn("key");
        when(propertyOperator.findGetter((Class) Object.class, "key")).thenReturn(methodInvoker);
        when(methodInvoker.invoke(any())).thenReturn(Arrays.asList("value1", "value2"));
        Collection<Object> targets = Collections.singletonList(new Object());
        Collection<Object> result = handler.process(operation, targets);
        assertEquals(2, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
    }

    @Test
    public void testProcessWithArrayItem() throws Exception {
        when(operation.getSourceType()).thenReturn((Class) Object.class);
        when(operation.getKey()).thenReturn("key");
        when(propertyOperator.findGetter((Class) Object.class, "key")).thenReturn(methodInvoker);
        when(methodInvoker.invoke(any())).thenReturn(new String[] { "value1", "value2" });
        Collection<Object> targets = Collections.singletonList(new Object());
        Collection<Object> result = handler.process(operation, targets);
        assertEquals(2, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
    }
}
