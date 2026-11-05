package cn.crane4j.core.util;

import org.junit.Test;
import org.mockito.*;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import lombok.NoArgsConstructor;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StringUtils_contains_2_0_Test {

    @Test
    public void testContainsBothNull() {
        // Test case where both strings are null
        assertFalse(StringUtils.contains(null, null));
    }

    @Test
    public void testContainsBothEqual() {
        // Test case where both strings are equal
        assertTrue(StringUtils.contains("test", "test"));
    }

    @Test
    public void testContainsStrNull() {
        // Test case where the first string is null
        assertFalse(StringUtils.contains(null, "search"));
    }

    @Test
    public void testContainsSearchStrNull() {
        // Test case where the second string is null
        assertFalse(StringUtils.contains("str", null));
    }

    @Test
    public void testContainsSubstringPresent() {
        // Test case where the search string is a substring of the main string
        assertTrue(StringUtils.contains("hello world", "world"));
    }

    @Test
    public void testContainsSubstringAbsent() {
        // Test case where the search string is not a substring of the main string
        assertFalse(StringUtils.contains("hello world", "planet"));
    }
}
