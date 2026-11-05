package com.j256.ormlite.logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

public class LoggerFactory_setLogBackendType_0_0_Test {

    private LogBackendFactory originalLogBackendFactory;

    @BeforeEach
    public void setUp() throws Exception {
        // Save the original value of the static field
        Field field = LoggerFactory.class.getDeclaredField("logBackendFactory");
        field.setAccessible(true);
        originalLogBackendFactory = (LogBackendFactory) field.get(null);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Restore the original value of the static field
        Field field = LoggerFactory.class.getDeclaredField("logBackendFactory");
        field.setAccessible(true);
        field.set(null, originalLogBackendFactory);
    }

    @Test
    public void testSetLogBackendTypeAvailable() {
        // Assume LOCAL is always available
        LogBackendType type = LogBackendType.LOCAL;
        LoggerFactory.setLogBackendType(type);
        assertEquals(type, LoggerFactory.getLogBackendFactory());
    }

    @Test
    public void testSetLogBackendTypeNotAvailable() {
        // Assume NULL is never available
        LogBackendType type = LogBackendType.NULL;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            LoggerFactory.setLogBackendType(type);
        });
        assertEquals("Logging backend type " + type + " is not available on the classpath", exception.getMessage());
    }
}
