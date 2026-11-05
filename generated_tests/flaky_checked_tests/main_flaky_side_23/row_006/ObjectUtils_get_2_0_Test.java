package cn.crane4j.core.util;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import java.lang.reflect.Method;
import java.util.*;
import org.mockito.*;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ObjectUtils_get_2_0_Test {

    private Method getMethod;

    @Before
    public void setUp() throws Exception {
        // Using reflection to access the private method
        getMethod = ObjectUtils.class.getDeclaredMethod("get", Object.class, int.class);
        getMethod.setAccessible(true);
    }

    @After
    public void tearDown() {
        getMethod.setAccessible(false);
    }

    @Test
    public void testGetWithNullTarget() throws Exception {
        assertNull(invokeGet(null, 0));
    }

    @Test
    public void testGetWithList() throws Exception {
        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("a", invokeGet(list, 0));
        assertEquals("b", invokeGet(list, 1));
        assertEquals("c", invokeGet(list, 2));
        // Out of bounds
        assertNull(invokeGet(list, 3));
    }

    @Test
    public void testGetWithIterator() throws Exception {
        List<String> list = Arrays.asList("a", "b", "c");
        Iterator<String> iterator = list.iterator();
        assertEquals("a", invokeGet(iterator, 0));
        // Reset iterator
        iterator = list.iterator();
        assertEquals("b", invokeGet(iterator, 1));
        // Reset iterator
        iterator = list.iterator();
        assertEquals("c", invokeGet(iterator, 2));
        // Reset iterator
        iterator = list.iterator();
        // Out of bounds
        assertNull(invokeGet(iterator, 3));
    }

    @Test
    public void testGetWithIterable() throws Exception {
        Iterable<String> iterable = Arrays.asList("a", "b", "c");
        assertEquals("a", invokeGet(iterable, 0));
        assertEquals("b", invokeGet(iterable, 1));
        assertEquals("c", invokeGet(iterable, 2));
        // Out of bounds
        assertNull(invokeGet(iterable, 3));
    }

    @Test
    public void testGetWithMap() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("1", "a");
        map.put("2", "b");
        map.put("3", "c");
        Collection<String> values = map.values();
        assertTrue(values.contains(invokeGet(map, 0)));
        assertTrue(values.contains(invokeGet(map, 1)));
        assertTrue(values.contains(invokeGet(map, 2)));
        // Out of bounds
        assertNull(invokeGet(map, 3));
    }

    @Test
    public void testGetWithArray() throws Exception {
        String[] array = { "a", "b", "c" };
        assertEquals("a", invokeGet(array, 0));
        assertEquals("b", invokeGet(array, 1));
        assertEquals("c", invokeGet(array, 2));
        // Out of bounds
        assertNull(invokeGet(array, 3));
    }

    @Test
    public void testGetWithUnsupportedType() throws Exception {
        String unsupportedType = "unsupported";
        assertNull(invokeGet(unsupportedType, 0));
    }

    @SuppressWarnings("unchecked")
    private <T> T invokeGet(Object target, int index) throws Exception {
        return (T) getMethod.invoke(null, target, index);
    }
}
