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
        originalFactory = LoggerFactory.getLogBackendFactory();
    }

    @AfterEach
    public void tearDown() {
        LoggerFactory.setLogBackendFactory(originalFactory);
    }

    @Test
    public void testSetLogBackendTypeAvailable() {
        LoggerFactory.setLogBackendType(LogBackendType.LOCAL);
        assertSame(LogBackendType.LOCAL, LoggerFactory.getLogBackendFactory());
    }

    @Test
    public void testSetLogBackendTypeNotAvailable() {
        assertThrows(IllegalArgumentException.class, () -> {
            LoggerFactory.setLogBackendType(LogBackendType.NULL);
        });
    }
}
