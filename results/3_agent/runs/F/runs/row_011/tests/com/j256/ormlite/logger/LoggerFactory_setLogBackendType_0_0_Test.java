package com.j256.ormlite.logger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class LoggerFactory_setLogBackendType_0_0_Test {

    @AfterEach
    public void tearDown() {
        // Reset the logBackendFactory to null after each test
        LoggerFactory.setLogBackendFactory(null);
    }

    @Test
    public void testSetLogBackendTypeAvailable() {
        // Assume LOCAL is always available
        LogBackendType type = LogBackendType.LOCAL;
        LoggerFactory.setLogBackendType(type);
        assertSame(type, LoggerFactory.getLogBackendFactory());
    }

    @Test
    public void testSetLogBackendTypeNotAvailable() {
        // Assume NULL is never available
        LogBackendType type = LogBackendType.NULL;
        assertThrows(IllegalArgumentException.class, () -> {
            LoggerFactory.setLogBackendType(type);
        });
    }
}
