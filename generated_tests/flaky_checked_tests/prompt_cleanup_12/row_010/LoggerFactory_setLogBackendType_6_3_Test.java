package com.j256.ormlite.logger;

import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.LoggerFactory;
import java.lang.reflect.Field;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import com.j256.ormlite.logger.backend.NullLogBackend.NullLogBackendFactory;

public class LoggerFactory_setLogBackendType_6_3_Test {

    private LogBackendFactory originalLogBackendFactory;

    @BeforeEach
    public void setUp() throws Exception {
        // Save the original logBackendFactory
        Field field = LoggerFactory.class.getDeclaredField("logBackendFactory");
        field.setAccessible(true);
        originalLogBackendFactory = (LogBackendFactory) field.get(null);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Restore the original logBackendFactory
        Field field = LoggerFactory.class.getDeclaredField("logBackendFactory");
        field.setAccessible(true);
        field.set(null, originalLogBackendFactory);
    }

    @Test
    public void testSetLogBackendTypeNotAvailable() {
        // Use a real LogBackendType that is not available
        LogBackendType notAvailableType = LogBackendType.NULL;
        // Call the method under test and expect an exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            LoggerFactory.setLogBackendType(notAvailableType);
        });
        assertEquals("Logging backend type " + notAvailableType + " is not available on the classpath", exception.getMessage());
    }

    private LogBackendFactory getLogBackendFactory() {
        try {
            Field field = LoggerFactory.class.getDeclaredField("logBackendFactory");
            field.setAccessible(true);
            return (LogBackendFactory) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
