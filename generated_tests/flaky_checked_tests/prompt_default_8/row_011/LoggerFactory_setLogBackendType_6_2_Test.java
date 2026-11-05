package com.j256.ormlite.logger;

import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.logger.backend.NullLogBackend.NullLogBackendFactory;
import org.junit.jupiter.api.function.Executable;
import java.lang.reflect.Field;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
public class LoggerFactory_setLogBackendType_6_2_Test {

    @Mock
    private LogBackendType mockType;

    @BeforeEach
    public void setUp() throws Exception {
        // Reset the static field to null before each test
        setLogBackendFactoryField(null);
    }

    @Test
    public void testSetLogBackendTypeAvailable() throws Exception {
        when(mockType.isAvailable()).thenReturn(true);
        LoggerFactory.setLogBackendType(mockType);
        assertEquals(mockType, getLogBackendFactoryField(), "LogBackendFactory should be set to the provided type");
    }

    @Test
    public void testSetLogBackendTypeNotAvailable() {
        when(mockType.isAvailable()).thenReturn(false);
        Executable executable = () -> LoggerFactory.setLogBackendType(mockType);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable, "Expected IllegalArgumentException when LogBackendType is not available");
        assertEquals("Logging backend type " + mockType + " is not available on the classpath", exception.getMessage());
    }

    // Helper method to set the private static field logBackendFactory
    private void setLogBackendFactoryField(LogBackendType value) throws Exception {
        Field field = LoggerFactory.class.getDeclaredField("logBackendFactory");
        field.setAccessible(true);
        field.set(null, value);
    }

    // Helper method to get the private static field logBackendFactory
    private LogBackendType getLogBackendFactoryField() throws Exception {
        Field field = LoggerFactory.class.getDeclaredField("logBackendFactory");
        field.setAccessible(true);
        return (LogBackendType) field.get(null);
    }
}
