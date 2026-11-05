package com.j256.ormlite.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.field.FieldType;

public class SchemaUtils_createSchema_0_0_Test {

    private ConnectionSource connectionSource;
    private DatabaseType databaseType;
    private DatabaseConnection databaseConnection;
    private CompiledStatement compiledStatement;

    @BeforeEach
    public void setUp() throws SQLException {
        connectionSource = mock(ConnectionSource.class);
        databaseType = mock(DatabaseType.class);
        databaseConnection = mock(DatabaseConnection.class);
        compiledStatement = mock(CompiledStatement.class);

        when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        when(connectionSource.getReadWriteConnection("testSchema")).thenReturn(databaseConnection);
        when(databaseConnection.compileStatement("CREATE SCHEMA testSchema", StatementType.EXECUTE, new FieldType[0], -1, false)).thenReturn(compiledStatement);
        when(compiledStatement.runExecute()).thenReturn(1);
        when(databaseConnection.executeStatement("CREATE SCHEMA testSchema", -1)).thenReturn(1);
    }

    @Test
    public void testCreateSchema() throws SQLException {
        when(databaseType.isCreateSchemaReturnsNegative()).thenReturn(false);
        when(databaseType.isCreateSchemaReturnsZero()).thenReturn(false);

        int result = SchemaUtils.createSchema(connectionSource, "testSchema");

        // Assuming the doCreateSchema method returns 1 for this mock setup
        assertEquals(1, result);
    }
}
