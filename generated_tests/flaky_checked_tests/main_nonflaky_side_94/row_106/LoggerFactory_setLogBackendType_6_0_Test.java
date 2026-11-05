package com.j256.ormlite.logger;

import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.logger.LogBackendType;
import java.lang.reflect.Field;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import com.j256.ormlite.logger.backend.NullLogBackend.NullLogBackendFactory;

public class LoggerFactory_setLogBackendType_6_0_Test {

    @BeforeEach
    public void setUp() {
        // Reset the static field before each test
        setLogBackendFactory(null);
    }

    @Test
    public void testSetLogBackendTypeAvailable() {
        // Assuming LOCAL is always available
        LogBackendType type = LogBackendType.LOCAL;
        assertDoesNotThrow(() -> LoggerFactory.setLogBackendType(type));
        assertEquals(type, getLogBackendFactory());
    }

    @Test
    public void testSetLogBackendTypeNotAvailable() {
        // Assuming NULL is not available
        LogBackendType type = LogBackendType.NULL;
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            LoggerFactory.setLogBackendType(type);
        });
        assertEquals("Logging backend type " + type + " is not available on the classpath", exception.getMessage());
    }

    private void setLogBackendFactory(LogBackendType type) {
        try {
            Field field = LoggerFactory.class.getDeclaredField("logBackendFactory");
            field.setAccessible(true);
            field.set(null, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private LogBackendType getLogBackendFactory() {
        try {
            Field field = LoggerFactory.class.getDeclaredField("logBackendFactory");
            field.setAccessible(true);
            return (LogBackendType) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
