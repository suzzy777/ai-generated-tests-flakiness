package com.j256.ormlite.table;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SchemaUtils_createSchema_0_0_Test {

    private ConnectionSource connectionSourceMock;
    private DatabaseType databaseTypeMock;
    private DatabaseConnection databaseConnectionMock;
    private CompiledStatement compiledStatementMock;
    private Logger originalLogger;

    @BeforeEach
    void setUp() throws SQLException {
        connectionSourceMock = mock(ConnectionSource.class);
        databaseTypeMock = mock(DatabaseType.class);
        databaseConnectionMock = mock(DatabaseConnection.class);
        compiledStatementMock = mock(CompiledStatement.class);
        when(connectionSourceMock.getDatabaseType()).thenReturn(databaseTypeMock);
        when(connectionSourceMock.getReadWriteConnection(anyString())).thenReturn(databaseConnectionMock);

        // Mock database connection interactions
        when(databaseConnectionMock.compileStatement(anyString(), any(StatementType.class), anyInt())).thenReturn(compiledStatementMock);
        when(databaseConnectionMock.insert(anyString(), any(), any(), any())).thenReturn(1L);
        when(databaseConnectionMock.executeStatement(anyString(), anyInt())).thenReturn(1);

        // Save the original logger
        originalLogger = (Logger) getPrivateStaticField(SchemaUtils.class, "logger");
    }

    @AfterEach
    void tearDown() {
        // Restore the original logger
        setPrivateStaticField(SchemaUtils.class, "logger", originalLogger);
    }

    @Test
    void testCreateSchema() throws SQLException {
        String schemaName = "testSchema";

        // Invoke the method
        int result = SchemaUtils.createSchema(connectionSourceMock, schemaName);

        // Verify the result
        assertEquals(0, result);
    }

    /**
     * Uses reflection to get a private static field from a class.
     */
    private static Object getPrivateStaticField(Class<?> clazz, String fieldName) {
        try {
            java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Uses reflection to set a private static field in a class.
     */
    private static void setPrivateStaticField(Class<?> clazz, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
