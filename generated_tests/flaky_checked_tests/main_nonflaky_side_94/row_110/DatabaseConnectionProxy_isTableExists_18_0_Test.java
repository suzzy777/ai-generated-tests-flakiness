package com.j256.ormlite.support;

import com.j256.ormlite.support.DatabaseConnection;
import java.sql.SQLException;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.Connection;
import java.sql.Savepoint;
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;

public class DatabaseConnectionProxy_isTableExists_18_0_Test {

    private DatabaseConnection mockDatabaseConnection;

    private DatabaseConnectionProxy databaseConnectionProxy;

    @BeforeEach
    public void setUp() {
        mockDatabaseConnection = mock(DatabaseConnection.class);
        databaseConnectionProxy = new DatabaseConnectionProxy(mockDatabaseConnection);
    }

    @Test
    public void testIsTableExistsWhenProxyIsNull() throws Exception {
        // Use reflection to set the private field proxy to null
        java.lang.reflect.Field proxyField = DatabaseConnectionProxy.class.getDeclaredField("proxy");
        proxyField.setAccessible(true);
        proxyField.set(databaseConnectionProxy, null);
        boolean result = databaseConnectionProxy.isTableExists("testTable");
        assertFalse(result, "Expected false when proxy is null");
    }

    @Test
    public void testIsTableExistsWhenProxyIsNotNullAndTableExists() throws SQLException {
        when(mockDatabaseConnection.isTableExists("testTable")).thenReturn(true);
        boolean result = databaseConnectionProxy.isTableExists("testTable");
        assertTrue(result, "Expected true when table exists");
    }

    @Test
    public void testIsTableExistsWhenProxyIsNotNullAndTableDoesNotExist() throws SQLException {
        when(mockDatabaseConnection.isTableExists("testTable")).thenReturn(false);
        boolean result = databaseConnectionProxy.isTableExists("testTable");
        assertFalse(result, "Expected false when table does not exist");
    }

    @Test
    public void testIsTableExistsThrowsSQLException() throws SQLException {
        when(mockDatabaseConnection.isTableExists("testTable")).thenThrow(new SQLException("SQL error"));
        assertThrows(SQLException.class, () -> {
            databaseConnectionProxy.isTableExists("testTable");
        }, "Expected SQLException to be thrown");
    }
}
