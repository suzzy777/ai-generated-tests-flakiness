package com.j256.ormlite.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.field.FieldType;

public class SchemaUtils_createSchema_0_0_Test {

    private ConnectionSource connectionSourceMock;
    private DatabaseType databaseTypeMock;
    private DatabaseConnection databaseConnectionMock;

    @BeforeEach
    public void setUp() throws SQLException {
        connectionSourceMock = Mockito.mock(ConnectionSource.class);
        databaseTypeMock = Mockito.mock(DatabaseType.class);
        databaseConnectionMock = Mockito.mock(DatabaseConnection.class);

        Mockito.when(connectionSourceMock.getDatabaseType()).thenReturn(databaseTypeMock);
        Mockito.when(connectionSourceMock.getReadWriteConnection(Mockito.anyString())).thenReturn(databaseConnectionMock);
        Mockito.when(databaseConnectionMock.compileStatement(Mockito.anyString(), Mockito.any(StatementType.class), Mockito.<FieldType[]>any(), Mockito.anyInt())).thenReturn(Mockito.mock(CompiledStatement.class));
    }

    @AfterEach
    public void tearDown() {
        Mockito.reset(connectionSourceMock, databaseTypeMock, databaseConnectionMock);
    }

    @Test
    public void testCreateSchema() throws SQLException {
        Mockito.when(databaseTypeMock.isCreateSchemaReturnsNegative()).thenReturn(false);
        Mockito.when(databaseTypeMock.isCreateSchemaReturnsZero()).thenReturn(false);

        int result = SchemaUtils.createSchema(connectionSourceMock, "testSchema");

        assertEquals(0, result);
    }

    @Test
    public void testCreateSchemaWithSQLException() throws SQLException {
        Mockito.when(connectionSourceMock.getReadWriteConnection(Mockito.anyString())).thenThrow(new SQLException("Test Exception"));

        assertThrows(SQLException.class, () -> {
            SchemaUtils.createSchema(connectionSourceMock, "testSchema");
        });
    }
}
