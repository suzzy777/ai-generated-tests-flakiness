package com.j256.ormlite.table;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.CompiledStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class SchemaUtils_createSchema_0_0_Test {

    private ConnectionSource connectionSourceMock;
    private DatabaseType databaseTypeMock;
    private DatabaseConnection databaseConnectionMock;
    private CompiledStatement compiledStatementMock;

    @BeforeEach
    void setUp() throws SQLException {
        connectionSourceMock = Mockito.mock(ConnectionSource.class);
        databaseTypeMock = Mockito.mock(DatabaseType.class);
        databaseConnectionMock = Mockito.mock(DatabaseConnection.class);
        compiledStatementMock = Mockito.mock(CompiledStatement.class);

        Mockito.when(connectionSourceMock.getDatabaseType()).thenReturn(databaseTypeMock);
        Mockito.when(connectionSourceMock.getReadWriteConnection(Mockito.anyString())).thenReturn(databaseConnectionMock);
        Mockito.when(databaseConnectionMock.compileStatement(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyBoolean())).thenReturn(compiledStatementMock);
        Mockito.when(compiledStatementMock.runExecute()).thenReturn(1);
    }

    @Test
    void testCreateSchema() throws SQLException {
        Mockito.when(databaseTypeMock.isCreateSchemaReturnsNegative()).thenReturn(false);
        Mockito.when(databaseTypeMock.isCreateSchemaReturnsZero()).thenReturn(false);

        int result = SchemaUtils.createSchema(connectionSourceMock, "testSchema");

        assertEquals(1, result, "Expected schema creation result to be 1");
    }
}
