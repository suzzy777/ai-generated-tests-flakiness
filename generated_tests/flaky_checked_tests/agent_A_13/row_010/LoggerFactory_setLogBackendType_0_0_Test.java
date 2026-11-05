package com.j256.ormlite.logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

class LoggerFactory_setLogBackendType_0_0_Test {

    private LogBackendType availableLogBackendType;
    private LogBackendType unavailableLogBackendType;

    @BeforeEach
    void setUp() {
        availableLogBackendType = LogBackendType.LOCAL; // Assuming LOCAL is always available
        unavailableLogBackendType = LogBackendType.NULL; // Assuming NULL is never available
    }

    @Test
    void testSetLogBackendType_whenAvailable() {
        // Act
        LoggerFactory.setLogBackendType(availableLogBackendType);

        // Assert
        assertEquals(availableLogBackendType, getLogBackendFactory());
    }

    @Test
    void testSetLogBackendType_whenNotAvailable() {
        // Act & Assert
        Executable executable = () -> LoggerFactory.setLogBackendType(unavailableLogBackendType);
        assertThrows(IllegalArgumentException.class, executable);
    }

    private LogBackendType getLogBackendFactory() {
        try {
            java.lang.reflect.Field field = LoggerFactory.class.getDeclaredField("logBackendFactory");
            field.setAccessible(true);
            return (LogBackendType) field.get(null);
        } catch (Exception e) {
            fail("Reflection failed");
            return null; // This line will never be reached
        }
    }
}
