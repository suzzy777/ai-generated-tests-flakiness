package com.j256.ormlite.logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class LoggerFactory_setLogBackendType_0_0_Test {

    private LogBackendType availableLogBackendType;
    private LogBackendType notAvailableLogBackendType;

    @BeforeEach
    public void setUp() throws Exception {
        availableLogBackendType = createLogBackendTypeMock(true);
        notAvailableLogBackendType = createLogBackendTypeMock(false);
    }

    @Test
    public void testSetLogBackendType_Available() {
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                LoggerFactory.setLogBackendType(availableLogBackendType);
            }
        });

        assertEquals(availableLogBackendType, getLogBackendFactory());
    }

    @Test
    public void testSetLogBackendType_NotAvailable() {
        Exception exception = assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                LoggerFactory.setLogBackendType(notAvailableLogBackendType);
            }
        });

        assertEquals("Logging backend type " + notAvailableLogBackendType + " is not available on the classpath", exception.getMessage());
    }

    private LogBackendType createLogBackendTypeMock(boolean isAvailable) throws Exception {
        Method method = LogBackendType.class.getDeclaredMethod("values");
        method.setAccessible(true);
        LogBackendType[] types = (LogBackendType[]) method.invoke(null);
        for (LogBackendType type : types) {
            if (isAvailable == type.isAvailable()) {
                return type;
            }
        }
        throw new RuntimeException("No matching LogBackendType found");
    }

    private LogBackendType getLogBackendFactory() {
        try {
            java.lang.reflect.Field field = LoggerFactory.class.getDeclaredField("logBackendFactory");
            field.setAccessible(true);
            return (LogBackendType) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
