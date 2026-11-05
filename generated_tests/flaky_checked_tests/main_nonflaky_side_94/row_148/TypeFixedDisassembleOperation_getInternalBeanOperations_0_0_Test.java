package cn.crane4j.core.parser.operation;

import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import lombok.Getter;
import lombok.NonNull;

@RunWith(MockitoJUnitRunner.class)
public class TypeFixedDisassembleOperation_getInternalBeanOperations_0_0_Test {

    private TypeFixedDisassembleOperation operation;

    @Mock
    private BeanOperations mockBeanOperations;

    @Mock
    private DisassembleOperationHandler mockDisassembleOperationHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        operation = new TypeFixedDisassembleOperation("key", 1, Object.class, mockBeanOperations, mockDisassembleOperationHandler);
    }

    @Test
    public void testGetInternalBeanOperations() throws Exception {
        Method method = TypeFixedDisassembleOperation.class.getDeclaredMethod("getInternalBeanOperations", Object.class);
        method.setAccessible(true);
        BeanOperations result = (BeanOperations) method.invoke(operation, new Object());
        assertNotNull(result);
        assertEquals(mockBeanOperations, result);
    }
}
