package cn.crane4j.core.container;

import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.util.ConfigurationUtil;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.LinkedHashMap;
import java.util.Map;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import cn.crane4j.core.util.Asserts;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class DefaultContainerManager_clear_9_0_Test {

    private DefaultContainerManager containerManager;

    @Mock
    private ConcurrentMap<Object, Object> containerMapMock;

    @Mock
    private List<ContainerLifecycleProcessor> processorListMock;

    @Before
    public void setUp() throws Exception {
        containerManager = new DefaultContainerManager();
        // Use reflection to inject mocks into the private fields
        Field containerMapField = DefaultContainerManager.class.getDeclaredField("containerMap");
        containerMapField.setAccessible(true);
        containerMapField.set(containerManager, containerMapMock);
        Field processorListField = DefaultContainerManager.class.getDeclaredField("containerLifecycleProcessorList");
        processorListField.setAccessible(true);
        processorListField.set(containerManager, processorListMock);
    }

    @Test
    public void testClear() throws Exception {
        // Mock behavior
        when(containerMapMock.values()).thenReturn(new ArrayList<>());
        // Invoke the clear method
        containerManager.clear();
        // Verify interactions
        verify(containerMapMock).clear();
        verify(processorListMock).clear();
        // Verify the containerProviderMap is also cleared
        Field containerProviderMapField = DefaultContainerManager.class.getDeclaredField("containerProviderMap");
        containerProviderMapField.setAccessible(true);
        Map<String, Object> containerProviderMap = (Map<String, Object>) containerProviderMapField.get(containerManager);
        assertTrue(containerProviderMap.isEmpty());
    }
}
