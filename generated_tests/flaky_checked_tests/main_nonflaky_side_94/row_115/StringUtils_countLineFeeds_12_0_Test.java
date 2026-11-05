package com.intuit.karate;

import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtils_countLineFeeds_12_0_Test {

    @Test
    public void testCountLineFeeds() throws Exception {
        // Using reflection to access the private constructor
        Method countLineFeedsMethod = StringUtils.class.getDeclaredMethod("countLineFeeds", String.class);
        countLineFeedsMethod.setAccessible(true);
        // Test case 1: No line feeds
        String input1 = "This is a single line text.";
        int result1 = (int) countLineFeedsMethod.invoke(null, input1);
        assertEquals(0, result1, "Expected 0 line feeds");
        // Test case 2: One line feed
        String input2 = "This is a single line text.\n";
        int result2 = (int) countLineFeedsMethod.invoke(null, input2);
        assertEquals(1, result2, "Expected 1 line feed");
        // Test case 3: Multiple line feeds
        String input3 = "Line 1\nLine 2\nLine 3\n";
        int result3 = (int) countLineFeedsMethod.invoke(null, input3);
        assertEquals(3, result3, "Expected 3 line feeds");
        // Test case 4: Line feeds with other characters
        String input4 = "\nLine 1\nLine 2\n";
        int result4 = (int) countLineFeedsMethod.invoke(null, input4);
        assertEquals(3, result4, "Expected 3 line feeds");
        // Test case 5: Empty string
        String input5 = "";
        int result5 = (int) countLineFeedsMethod.invoke(null, input5);
        assertEquals(0, result5, "Expected 0 line feeds");
        // Test case 6: String with only line feeds
        String input6 = "\n\n\n";
        int result6 = (int) countLineFeedsMethod.invoke(null, input6);
        assertEquals(3, result6, "Expected 3 line feeds");
    }
}
