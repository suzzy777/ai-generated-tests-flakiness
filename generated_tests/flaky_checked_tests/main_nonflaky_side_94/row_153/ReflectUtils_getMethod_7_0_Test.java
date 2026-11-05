package cn.crane4j.core.util;

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
import cn.crane4j.core.exception.Crane4jException;
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

public class ReflectUtils_getMethod_7_0_Test {

    @Test
    public void testGetMethodWithExistingMethod() {
        // Test case where the method exists in the class
        Method method = ReflectUtils.getMethod(TestClass.class, "existingMethod", String.class);
        assertNotNull(method);
        assertEquals("existingMethod", method.getName());
    }

    @Test
    public void testGetMethodWithNonExistingMethod() {
        // Test case where the method does not exist in the class
        Method method = ReflectUtils.getMethod(TestClass.class, "nonExistingMethod", String.class);
        assertNull(method);
    }

    @Test
    public void testGetMethodWithNullParameterTypes() {
        // Test case where parameter types are null
        Method method = ReflectUtils.getMethod(TestClass.class, "existingMethodWithNoParams", (Class<?>[]) null);
        assertNotNull(method);
        assertEquals("existingMethodWithNoParams", method.getName());
    }

    @Test
    public void testGetMethodWithEmptyParameterTypes() {
        // Test case where parameter types are empty
        Method method = ReflectUtils.getMethod(TestClass.class, "existingMethodWithNoParams");
        assertNotNull(method);
        assertEquals("existingMethodWithNoParams", method.getName());
    }

    private static class TestClass {

        public void existingMethod(String param) {
        }

        public void existingMethodWithNoParams() {
        }
    }
}
