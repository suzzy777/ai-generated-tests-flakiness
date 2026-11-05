package cn.crane4j.core.util;

import cn.crane4j.core.support.AnnotationFinder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.ParameterNameFinder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

@RunWith(MockitoJUnitRunner.class)
public class ReflectUtils_scanAllAnnotationFromElements_15_0_Test {

    @Mock
    private AnnotationFinder annotationFinder;

    @Mock
    private AnnotatedElement annotatedElement;

    @Mock
    private Annotation annotation;

    @Mock
    private BiConsumer<AnnotatedElement, Annotation> consumer;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testScanAllAnnotationFromElements_withNonEmptyArrayAndNonEmptyAnnotations() {
        // Arrange
        AnnotatedElement[] elements = new AnnotatedElement[] { annotatedElement };
        Set<Annotation> annotations = new HashSet<>(Collections.singletonList(annotation));
        when(annotationFinder.getAllAnnotations(annotatedElement, Annotation.class)).thenReturn(annotations);
        // Act
        ReflectUtils.scanAllAnnotationFromElements(annotationFinder, Annotation.class, elements, consumer);
        // Assert
        verify(consumer, times(1)).accept(annotatedElement, annotation);
    }

    @Test
    public void testScanAllAnnotationFromElements_withEmptyArray() {
        // Arrange
        AnnotatedElement[] elements = new AnnotatedElement[] {};
        // Act
        ReflectUtils.scanAllAnnotationFromElements(annotationFinder, Annotation.class, elements, consumer);
        // Assert
        verify(consumer, never()).accept(any(), any());
    }

    @Test
    public void testScanAllAnnotationFromElements_withNonEmptyArrayAndEmptyAnnotations() {
        // Arrange
        AnnotatedElement[] elements = new AnnotatedElement[] { annotatedElement };
        when(annotationFinder.getAllAnnotations(annotatedElement, Annotation.class)).thenReturn(Collections.emptySet());
        // Act
        ReflectUtils.scanAllAnnotationFromElements(annotationFinder, Annotation.class, elements, consumer);
        // Assert
        verify(consumer, never()).accept(any(), any());
    }
}
