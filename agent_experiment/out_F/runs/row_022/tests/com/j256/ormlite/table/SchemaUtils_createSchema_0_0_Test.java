package com.j256.ormlite.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.CompiledStatement;

public class SchemaUtils_createSchema_0_0_Test {

    private ConnectionSource connectionSource;
    private DatabaseType databaseType;
    private DatabaseConnection databaseConnection;

    @BeforeEach
    public void setUp() throws SQLException {
        connectionSource = mock(ConnectionSource.class);
        databaseType = mock(DatabaseType.class);
        databaseConnection = mock(DatabaseConnection.class);

        when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        when(connectionSource.getReadWriteConnection("testSchema")).thenReturn(databaseConnection);
        when(databaseType.isCreateSchemaReturnsNegative()).thenReturn(false);
        when(databaseType.isCreateSchemaReturnsZero()).thenReturn(false);
        when(databaseConnection.compileStatement((String) org.mockito.ArgumentMatchers.any(), (StatementType) org.mockito.ArgumentMatchers.any(), (FieldType[]) org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyBoolean())).thenReturn(mock(CompiledStatement.class));
    }

    @Test
    public void testCreateSchema() throws SQLException {
        int result = SchemaUtils.createSchema(connectionSource, "testSchema");
        assertEquals(1, result);
    }

    @Test
    public void testCreateSchemaWithZeroReturn() throws SQLException {
        when(databaseType.isCreateSchemaReturnsZero()).thenReturn(true);
        int result = SchemaUtils.createSchema(connectionSource, "testSchema");
        assertEquals(1, result);
    }

    @Test
    public void testCreateSchemaWithNegativeReturn() throws SQLException {
        when(databaseType.isCreateSchemaReturnsNegative()).thenReturn(true);
        int result = SchemaUtils.createSchema(connectionSource, "testSchema");
        assertEquals(1, result);
    }
}
