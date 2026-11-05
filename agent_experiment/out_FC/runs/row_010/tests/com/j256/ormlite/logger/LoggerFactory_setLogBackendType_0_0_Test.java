package com.j256.ormlite.logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

class LoggerFactory_setLogBackendType_0_0_Test {

    private LogBackendFactory originalLogBackendFactory;

    @BeforeEach
    void setUp() throws Exception {
        // Save the original static field value
        Field field = LoggerFactory.class.getDeclaredField("logBackendFactory");
        field.setAccessible(true);
        originalLogBackendFactory = (LogBackendFactory) field.get(null);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restore the original static field value
        Field field = LoggerFactory.class.getDeclaredField("logBackendFactory");
        field.setAccessible(true);
        field.set(null, originalLogBackendFactory);
    }

    @Test
    void testSetLogBackendTypeAvailable() {
        LogBackendType availableType = LogBackendType.LOCAL; // Assuming LOCAL is always available

        LoggerFactory.setLogBackendType(availableType);

        assertEquals(availableType, LoggerFactory.getLogBackendFactory());
    }

    @Test
    void testSetLogBackendTypeNotAvailable() {
        LogBackendType notAvailableType = LogBackendType.NULL; // Assuming NULL is never available

        assertThrows(IllegalArgumentException.class, () -> {
            LoggerFactory.setLogBackendType(notAvailableType);
        });
    }
}
