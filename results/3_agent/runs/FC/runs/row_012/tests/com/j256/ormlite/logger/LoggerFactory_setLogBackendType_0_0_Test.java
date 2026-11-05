package com.j256.ormlite.logger;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoggerFactory_setLogBackendType_0_0_Test {

    private LogBackendFactory originalLogBackendFactory;

    @BeforeEach
    public void setUp() {
        originalLogBackendFactory = LoggerFactory.getLogBackendFactory();
    }

    @AfterEach
    public void tearDown() {
        LoggerFactory.setLogBackendFactory(originalLogBackendFactory);
    }

    @Test
    public void testSetLogBackendTypeAvailable() {
        LogBackendType availableType = LogBackendType.LOCAL; // Assume LOCAL is always available
        LoggerFactory.setLogBackendType(availableType);
        assertEquals(availableType, LoggerFactory.getLogBackendFactory());
    }

    @Test
    public void testSetLogBackendTypeUnavailable() {
        LogBackendType unavailableType = LogBackendType.NULL; // Assume NULL is never available
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            LoggerFactory.setLogBackendType(unavailableType);
        });
        assertEquals("Logging backend type " + unavailableType + " is not available on the classpath", exception.getMessage());
    }
}
