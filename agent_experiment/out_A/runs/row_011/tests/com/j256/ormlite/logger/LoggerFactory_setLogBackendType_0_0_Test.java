package com.j256.ormlite.logger;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoggerFactory_setLogBackendType_0_0_Test {

    private LogBackendType availableLogBackendType;
    private LogBackendType unavailableLogBackendType;

    @BeforeEach
    public void setUp() {
        availableLogBackendType = LogBackendType.LOCAL; // Assuming LOCAL is always available
        unavailableLogBackendType = LogBackendType.NULL; // Assuming NULL is never available
    }

    @Test
    public void testSetLogBackendTypeAvailable() {
        LoggerFactory.setLogBackendType(availableLogBackendType);
        assertNotNull(LoggerFactory.getLogBackendFactory());
    }

    @Test
    public void testSetLogBackendTypeNotAvailable() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            LoggerFactory.setLogBackendType(unavailableLogBackendType);
        });
        assertEquals("Logging backend type " + unavailableLogBackendType + " is not available on the classpath", exception.getMessage());
    }
}
