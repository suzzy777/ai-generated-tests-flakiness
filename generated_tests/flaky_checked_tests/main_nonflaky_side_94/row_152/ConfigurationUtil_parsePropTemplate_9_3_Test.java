package cn.crane4j.core.util;

import cn.crane4j.annotation.MappingTemplate;
import cn.crane4j.core.parser.PropertyMapping;
import org.junit.Test;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Before;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import cn.crane4j.annotation.Mapping;
import java.util.stream.Stream;
import org.mockito.*;
import org.junit.After;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.SimplePropertyMapping;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.DefaultContainerAdapterRegister;
import cn.crane4j.core.support.OperateTemplate;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleParameterNameFinder;
import cn.crane4j.core.support.container.ContainerMethodAnnotationProcessor;
import cn.crane4j.core.support.container.DefaultMethodContainerFactory;
import cn.crane4j.core.support.container.MethodContainerFactory;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.core.support.operator.DefaultOperatorProxyMethodFactory;
import cn.crane4j.core.support.operator.DynamicContainerOperatorProxyMethodFactory;
import cn.crane4j.core.support.operator.OperatorProxyFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationUtil_parsePropTemplate_9_3_Test {

    @Mock
    private MappingTemplate mockTemplate;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testParsePropTemplate() throws Exception {
        // Arrange
        Mapping mockMapping = mock(Mapping.class);
        when(mockTemplate.value()).thenReturn(new Mapping[] { mockMapping });
        // Act
        Method method = ConfigurationUtil.class.getDeclaredMethod("parsePropTemplate", MappingTemplate.class);
        method.setAccessible(true);
        List<PropertyMapping> result = (List<PropertyMapping>) method.invoke(null, mockTemplate);
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
