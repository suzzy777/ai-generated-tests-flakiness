package com.j256.ormlite.logger;

import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.LoggerFactory;
import org.junit.jupiter.api.function.Executable;
import java.lang.reflect.Field;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import com.j256.ormlite.logger.backend.NullLogBackend.NullLogBackendFactory;

@ExtendWith(MockitoExtension.class)
public class LoggerFactory_setLogBackendType_6_0_Test {

    @Mock
    private LogBackendType mockLogBackendType;

    @Test
    public void testSetLogBackendTypeWhenAvailable() throws Exception {
        when(mockLogBackendType.isAvailable()).thenReturn(true);
        LoggerFactory.setLogBackendType(mockLogBackendType);
        Field field = LoggerFactory.class.getDeclaredField("logBackendFactory");
        field.setAccessible(true);
        assertEquals(mockLogBackendType, field.get(null));
    }

    @Test
    public void testSetLogBackendTypeWhenNotAvailable() {
        when(mockLogBackendType.isAvailable()).thenReturn(false);
        Executable executable = () -> LoggerFactory.setLogBackendType(mockLogBackendType);
        assertThrows(IllegalArgumentException.class, executable);
    }
}
