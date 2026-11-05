package cn.crane4j.core.util;

import org.junit.Assert;
import org.junit.Test;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.mockito.*;
import org.junit.Before;
import org.junit.After;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import com.google.common.collect.MapMaker;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CollectionUtils_defaultIfEmpty_3_0_Test {

    @Test
    public void testDefaultIfEmpty() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Create test collections
        List<String> emptyList = Collections.emptyList();
        List<String> defaultList = new ArrayList<>();
        defaultList.add("default");
        // Test when the collection is empty
        List<String> result = CollectionUtils.defaultIfEmpty(emptyList, defaultList);
        Assert.assertEquals(defaultList, result);
        // Test when the collection is not empty
        List<String> nonEmptyList = new ArrayList<>();
        nonEmptyList.add("non-empty");
        result = CollectionUtils.defaultIfEmpty(nonEmptyList, defaultList);
        Assert.assertEquals(nonEmptyList, result);
    }

    @Test
    public void testIsEmpty() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Access the private isEmpty method using reflection
        Method isEmptyMethod = CollectionUtils.class.getDeclaredMethod("isEmpty", Collection.class);
        isEmptyMethod.setAccessible(true);
        // Test cases for isEmpty
        List<String> emptyList = Collections.emptyList();
        List<String> nonEmptyList = new ArrayList<>();
        nonEmptyList.add("item");
        // Invoke the method and assert results
        boolean result = (boolean) isEmptyMethod.invoke(null, emptyList);
        Assert.assertTrue(result);
        result = (boolean) isEmptyMethod.invoke(null, nonEmptyList);
        Assert.assertFalse(result);
    }
}
