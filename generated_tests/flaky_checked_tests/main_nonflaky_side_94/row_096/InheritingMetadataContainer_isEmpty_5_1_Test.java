package com.epam.eco.schemacatalog.store.metadata;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import com.epam.eco.schemacatalog.domain.metadata.MetadataKey;
import com.epam.eco.schemacatalog.domain.metadata.MetadataValue;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class InheritingMetadataContainer_isEmpty_5_1_Test {

    private InheritingMetadataContainer container;

    @Before
    public void setUp() {
        container = new InheritingMetadataContainer("testSubject");
    }

    @Test
    public void testIsEmptyWhenContainerIsEmpty() throws Exception {
        assertTrue(invokeIsEmpty());
    }

    @Test
    public void testIsEmptyWhenContainerIsNotEmpty() throws Exception {
        populateContainer();
        assertFalse(invokeIsEmpty());
    }

    private boolean invokeIsEmpty() throws Exception {
        return container.isEmpty();
    }

    private void populateContainer() throws Exception {
        Field containerField = InheritingMetadataContainer.class.getDeclaredField("container");
        containerField.setAccessible(true);
        NavigableMap<Integer, Map<Object, MetadataValue>> map = (NavigableMap<Integer, Map<Object, MetadataValue>>) containerField.get(container);
        map.put(1, Collections.singletonMap(new Object(), new MetadataValue("key", new HashMap<>(), new java.util.Date(), "description")));
    }
}
