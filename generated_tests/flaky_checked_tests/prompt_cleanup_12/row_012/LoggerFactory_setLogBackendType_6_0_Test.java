package com.j256.ormlite.logger;

import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.logger.backend.NullLogBackend.NullLogBackendFactory;
import com.j256.ormlite.logger.backend.LocalLogBackend.LocalLogBackendFactory;
import com.j256.ormlite.logger.backend.ConsoleLogBackend.ConsoleLogBackendFactory;
import java.lang.reflect.Field;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;

public class LoggerFactory_setLogBackendType_6_0_Test {

    private LogBackendFactory originalLogBackendFactory;

    @BeforeEach
    public void setUp() throws Exception {
        // Save the original state
        Field logBackendFactoryField = LoggerFactory.class.getDeclaredField("logBackendFactory");
        logBackendFactoryField.setAccessible(true);
        originalLogBackendFactory = (LogBackendFactory) logBackendFactoryField.get(null);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Restore the original state
        Field logBackendFactoryField = LoggerFactory.class.getDeclaredField("logBackendFactory");
        logBackendFactoryField.setAccessible(true);
        logBackendFactoryField.set(null, originalLogBackendFactory);
    }

    @Test
    public void testSetLogBackendTypeAvailable() {
        // Assume LOCAL is always available
        LogBackendType availableType = LogBackendType.LOCAL;
        assertDoesNotThrow(() -> LoggerFactory.setLogBackendType(availableType));
        LogBackendFactory currentFactory = getCurrentLogBackendFactory();
        assertEquals(availableType, currentFactory);
    }

    @Test
    public void testSetLogBackendTypeNotAvailable() {
        // Assume NULL is never available
        LogBackendType notAvailableType = LogBackendType.NULL;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            LoggerFactory.setLogBackendType(notAvailableType);
        });
        assertEquals("Logging backend type " + notAvailableType + " is not available on the classpath", exception.getMessage());
    }

    private LogBackendFactory getCurrentLogBackendFactory() {
        try {
            Field logBackendFactoryField = LoggerFactory.class.getDeclaredField("logBackendFactory");
            logBackendFactoryField.setAccessible(true);
            return (LogBackendFactory) logBackendFactoryField.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access logBackendFactory field", e);
        }
    }
}
