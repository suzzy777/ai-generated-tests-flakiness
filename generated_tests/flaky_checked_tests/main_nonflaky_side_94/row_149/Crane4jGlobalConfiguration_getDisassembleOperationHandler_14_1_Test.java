package cn.crane4j.core.support;

import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.reflect.PropertyOperator;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.mockito.*;
import org.junit.After;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class Crane4jGlobalConfiguration_getDisassembleOperationHandler_14_1_Test {

    @Mock
    private Crane4jGlobalConfiguration configuration;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetDisassembleOperationHandler() {
        DisassembleOperationHandler handler = mock(DisassembleOperationHandler.class);
        when(configuration.getDisassembleOperationHandler(any(Class.class))).thenReturn(handler);
        DisassembleOperationHandler result = configuration.getDisassembleOperationHandler(Object.class);
        assertNotNull(result);
        assertEquals(handler, result);
    }
}
