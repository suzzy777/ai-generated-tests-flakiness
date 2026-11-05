package cn.crane4j.core.util;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.util.ReflectUtils;
import org.junit.Test;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.ParameterNameFinder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ReflectUtils_invokeRaw_0_0_Test {

    private static class TestClass {

        private String privateMethod(String input) {
            return "Hello, " + input;
        }
    }

    @Test
    public void testInvokeRaw_successfulInvocation() throws Exception {
        // Arrange
        TestClass testObject = new TestClass();
        Method method = TestClass.class.getDeclaredMethod("privateMethod", String.class);
        // Act
        String result = ReflectUtils.invokeRaw(testObject, method, "World");
        // Assert
        assertEquals("Hello, World", result);
    }

    @Test(expected = Crane4jException.class)
    public void testInvokeRaw_withException() throws Exception {
        // Arrange
        TestClass testObject = new TestClass();
        Method method = TestClass.class.getDeclaredMethod("privateMethod", String.class);
        method = spy(method);
        // Simulate an exception being thrown
        doThrow(new InvocationTargetException(new RuntimeException())).when(method).invoke(any(), any());
        // Act
        ReflectUtils.invokeRaw(testObject, method, "World");
    }

    @Test(expected = Crane4jException.class)
    public void testInvokeRaw_withNonInvocationTargetException() throws Exception {
        // Arrange
        TestClass testObject = new TestClass();
        Method method = TestClass.class.getDeclaredMethod("privateMethod", String.class);
        method = spy(method);
        // Simulate a different exception being thrown
        doThrow(new IllegalAccessException()).when(method).invoke(any(), any());
        // Act
        ReflectUtils.invokeRaw(testObject, method, "World");
    }
}
