package com.j256.ormlite.logger;

import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.logger.backend.NullLogBackend.NullLogBackendFactory;
import java.lang.reflect.Field;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;

public class LoggerFactory_setLogBackendType_6_1_Test {

    private Field logBackendFactoryField;

    private LogBackendFactory originalLogBackendFactory;

    @BeforeEach
    public void setUp() throws Exception {
        logBackendFactoryField = LoggerFactory.class.getDeclaredField("logBackendFactory");
        logBackendFactoryField.setAccessible(true);
        originalLogBackendFactory = (LogBackendFactory) logBackendFactoryField.get(null);
    }

    @AfterEach
    public void tearDown() throws Exception {
        logBackendFactoryField.set(null, originalLogBackendFactory);
    }

    @Test
    public void testSetLogBackendTypeNotAvailable() {
        // Assuming LOG4J is a valid enum value and not available
        LogBackendType mockType = LogBackendType.LOG4J;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            LoggerFactory.setLogBackendType(mockType);
        });
        assertEquals("Logging backend type " + mockType + " is not available on the classpath", exception.getMessage());
    }
}
