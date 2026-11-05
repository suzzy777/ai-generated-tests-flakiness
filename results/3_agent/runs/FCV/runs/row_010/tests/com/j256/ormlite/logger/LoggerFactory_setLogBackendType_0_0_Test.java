package com.j256.ormlite.logger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

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
        LoggerFactory.setLogBackendType(LogBackendType.LOCAL);
        assertSame(LogBackendType.LOCAL, LoggerFactory.getLogBackendFactory());
    }

    @Test
    public void testSetLogBackendTypeNotAvailable() {
        assertThrows(IllegalArgumentException.class, () -> {
            LoggerFactory.setLogBackendType(LogBackendType.ANDROID);
        });
    }
}
