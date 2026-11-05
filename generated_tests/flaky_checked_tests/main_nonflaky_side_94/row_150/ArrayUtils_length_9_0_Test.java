package cn.crane4j.core.util;

import org.junit.Test;
import java.lang.reflect.Constructor;
import org.junit.Assert;
import org.mockito.*;
import org.junit.Before;
import org.junit.After;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import lombok.NoArgsConstructor;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArrayUtils_length_9_0_Test {

    @Test
    public void testLengthWithNullArray() throws Exception {
        // Use reflection to access the private constructor
        Constructor<ArrayUtils> constructor = ArrayUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ArrayUtils arrayUtilsInstance = constructor.newInstance();
        assertNotNull(arrayUtilsInstance);
        // Test with a null array
        Integer[] nullArray = null;
        int result = ArrayUtils.length(nullArray);
        assertEquals(0, result);
    }

    @Test
    public void testLengthWithEmptyArray() {
        // Test with an empty array
        Integer[] emptyArray = new Integer[0];
        int result = ArrayUtils.length(emptyArray);
        assertEquals(0, result);
    }

    @Test
    public void testLengthWithNonEmptyArray() {
        // Test with a non-empty array
        Integer[] nonEmptyArray = new Integer[] { 1, 2, 3, 4, 5 };
        int result = ArrayUtils.length(nonEmptyArray);
        assertEquals(5, result);
    }
}
