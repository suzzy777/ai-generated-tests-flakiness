package com.j256.ormlite.logger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoggerFactory_setLogBackendType_0_0_Test {

    private LogBackendFactory originalFactory;

    @BeforeEach
    public void setUp() {
        // Save the original state of the logBackendFactory
        originalFactory = LoggerFactory.getLogBackendFactory();
    }

    @AfterEach
    public void tearDown() {
        // Restore the original state of the logBackendFactory
        LoggerFactory.setLogBackendFactory(originalFactory);
    }

    @Test
    public void testSetLogBackendTypeAvailable() {
        // Test with an available LogBackendType
        LoggerFactory.setLogBackendType(LogBackendType.LOCAL);
        assertSame(LogBackendType.LOCAL, LoggerFactory.getLogBackendFactory());
    }

    @Test
    public void testSetLogBackendTypeNotAvailable() {
        // Test with a non-available LogBackendType
        assertThrows(IllegalArgumentException.class, () -> {
            LoggerFactory.setLogBackendType(LogBackendType.NULL);
        });
    }
}
