package com.j256.ormlite.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.DatabaseConnection;

public class SchemaUtils_createSchema_0_0_Test {

    @Test
    public void testCreateSchema() throws SQLException {
        // Mock the ConnectionSource
        ConnectionSource connectionSource = mock(ConnectionSource.class);
        DatabaseType databaseType = mock(DatabaseType.class);
        DatabaseConnection databaseConnection = mock(DatabaseConnection.class);

        // Define the behavior of the mocks
        when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        when(connectionSource.getReadWriteConnection("testSchema")).thenReturn(databaseConnection);

        // Define the behavior for databaseType
        when(databaseType.isCreateSchemaReturnsNegative()).thenReturn(false);
        when(databaseType.isCreateSchemaReturnsZero()).thenReturn(false);

        // Mock the behavior of databaseConnection for executing statements
        when(databaseConnection.executeStatement("CREATE SCHEMA testSchema", 0)).thenReturn(1);
        when(databaseConnection.executeStatement("create", 0)).thenReturn(0);

        // Simulate the doStatements method behavior
        List<String> statements = new ArrayList<>();
        statements.add("CREATE SCHEMA testSchema");
        when(databaseConnection.executeStatement(statements.get(0), 0)).thenReturn(1);

        // Call the method under test
        int result = SchemaUtils.createSchema(connectionSource, "testSchema");

        // Verify the result
        assertEquals(1, result);
    }
}
