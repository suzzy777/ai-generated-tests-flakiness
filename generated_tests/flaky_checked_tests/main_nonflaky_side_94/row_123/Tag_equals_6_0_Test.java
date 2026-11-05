package com.intuit.karate.core;

import com.intuit.karate.core.Tag;
import java.util.Collections;
import java.util.List;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.intuit.karate.StringUtils;

class Tag_equals_6_0_Test {

    @Test
    void testEqualsSameObject() {
        Tag tag = new Tag(1, "test");
        assertTrue(tag.equals(tag), "A tag should be equal to itself");
    }

    @Test
    void testEqualsNullObject() {
        Tag tag = new Tag(1, "test");
        assertFalse(tag.equals(null), "A tag should not be equal to null");
    }

    @Test
    void testEqualsDifferentClass() {
        Tag tag = new Tag(1, "test");
        assertFalse(tag.equals("test"), "A tag should not be equal to an object of a different class");
    }

    @Test
    void testEqualsDifferentText() {
        Tag tag1 = new Tag(1, "test1");
        Tag tag2 = new Tag(2, "test2");
        assertFalse(tag1.equals(tag2), "Tags with different text should not be equal");
    }

    @Test
    void testEqualsSameText() {
        Tag tag1 = new Tag(1, "test");
        Tag tag2 = new Tag(2, "test");
        assertTrue(tag1.equals(tag2), "Tags with the same text should be equal");
    }
}
