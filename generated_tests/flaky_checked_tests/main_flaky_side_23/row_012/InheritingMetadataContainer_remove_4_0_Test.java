package com.epam.eco.schemacatalog.store.metadata;

import org.junit.Before;
import org.junit.Test;
import com.epam.eco.schemacatalog.domain.metadata.MetadataKey;
import com.epam.eco.schemacatalog.domain.metadata.MetadataValue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.*;
import org.junit.After;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@RunWith(MockitoJUnitRunner.class)
public class InheritingMetadataContainer_remove_4_0_Test {

    private InheritingMetadataContainer container;

    private static final String SUBJECT = "testSubject";

    @Before
    public void setUp() {
        container = new InheritingMetadataContainer(SUBJECT);
    }

    @Test
    public void testRemoveWithValidKey() throws Exception {
        MetadataKey key = mock(MetadataKey.class);
        when(key.getSubject()).thenReturn(SUBJECT);
        when(key.getVersion()).thenReturn(1);
        // Mock a MetadataValue
        MetadataValue expectedValue = mock(MetadataValue.class);
        // Use reflection to invoke private method to add data for testing
        Method addMethod = InheritingMetadataContainer.class.getDeclaredMethod("removeByOriginKey", MetadataKey.class);
        addMethod.setAccessible(true);
        addMethod.invoke(container, key);
        // Now test the remove method
        MetadataValue removedValue = container.remove(key);
        // As there is no real data, expecting null
        assertNull(removedValue);
    }

    // Reflection helper method for testing private methods
    private Object invokePrivateMethod(String methodName, Class<?>[] parameterTypes, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = InheritingMetadataContainer.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(container, args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveWithInvalidKey() throws Exception {
        MetadataKey key = mock(MetadataKey.class);
        when(key.getSubject()).thenReturn("invalidSubject");
        // This should throw IllegalArgumentException due to invalid subject
        container.remove(key);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveWithNullKey() {
        container.remove(null);
    }
}
