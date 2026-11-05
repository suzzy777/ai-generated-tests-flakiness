package com.j256.ormlite.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

public class SchemaUtils_createSchema_0_0_Test {

    private ConnectionSource connectionSource;
    private DatabaseType databaseType;
    private DatabaseConnection databaseConnection;

    @BeforeEach
    public void setUp() throws SQLException {
        connectionSource = Mockito.mock(ConnectionSource.class);
        databaseType = Mockito.mock(DatabaseType.class);
        databaseConnection = Mockito.mock(DatabaseConnection.class);

        Mockito.when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        Mockito.when(connectionSource.getReadWriteConnection(Mockito.anyString())).thenReturn(databaseConnection);
    }

    @Test
    public void testCreateSchema() throws SQLException {
        Mockito.when(databaseType.isCreateSchemaReturnsNegative()).thenReturn(false);
        Mockito.when(databaseType.isCreateSchemaReturnsZero()).thenReturn(false);

        // Simulate the effect of doStatements returning a positive number
        Mockito.when(databaseConnection.executeStatement(Mockito.anyString(), Mockito.anyInt())).thenReturn(1);

        // Ensure the statements list is populated
        List<String> statements = new ArrayList<>();
        statements.add("CREATE SCHEMA testSchema");

        // Mock the behavior of the private method doCreateSchema to use the populated statements list
        Mockito.doAnswer(invocation -> {
            List<String> statementList = invocation.getArgument(2);
            statementList.addAll(statements);
            return null;
        }).when(databaseConnection).compileStatement(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any());

        int result = SchemaUtils.createSchema(connectionSource, "testSchema");

        assertEquals(1, result);
    }

    @Test
    public void testCreateSchemaWithSQLException() throws SQLException {
        Mockito.when(connectionSource.getReadWriteConnection(Mockito.anyString())).thenThrow(new SQLException("Test Exception"));

        assertThrowsExactly(SQLException.class, () -> {
            SchemaUtils.createSchema(connectionSource, "testSchema");
        });
    }
}
