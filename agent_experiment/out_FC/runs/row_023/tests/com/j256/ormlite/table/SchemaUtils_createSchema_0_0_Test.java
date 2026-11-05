package com.j256.ormlite.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;

public class SchemaUtils_createSchema_0_0_Test {

    private ConnectionSource connectionSourceMock;
    private DatabaseType databaseTypeMock;
    private DatabaseConnection databaseConnectionMock;
    private CompiledStatement compiledStatementMock;

    @BeforeEach
    public void setUp() throws SQLException {
        connectionSourceMock = Mockito.mock(ConnectionSource.class);
        databaseTypeMock = Mockito.mock(DatabaseType.class);
        databaseConnectionMock = Mockito.mock(DatabaseConnection.class);
        compiledStatementMock = Mockito.mock(CompiledStatement.class);

        Mockito.when(connectionSourceMock.getDatabaseType()).thenReturn(databaseTypeMock);
        Mockito.when(connectionSourceMock.getReadWriteConnection(Mockito.anyString())).thenReturn(databaseConnectionMock);
        Mockito.when(databaseConnectionMock.compileStatement(Mockito.anyString(), Mockito.any(StatementType.class), Mockito.any(), Mockito.anyInt(), Mockito.anyBoolean())).thenReturn(compiledStatementMock);
        Mockito.when(compiledStatementMock.runExecute()).thenReturn(1);
    }

    @AfterEach
    public void tearDown() {
        Mockito.reset(connectionSourceMock, databaseTypeMock, databaseConnectionMock, compiledStatementMock);
    }

    @Test
    public void testCreateSchema() throws SQLException {
        Mockito.when(databaseTypeMock.isCreateSchemaReturnsNegative()).thenReturn(false);
        Mockito.when(databaseTypeMock.isCreateSchemaReturnsZero()).thenReturn(false);

        int result = SchemaUtils.createSchema(connectionSourceMock, "testSchema");
        
        assertEquals(1, result);
    }

    @Test
    public void testCreateSchemaWithRuntimeException() throws SQLException {
        Mockito.when(connectionSourceMock.getDatabaseType()).thenThrow(new RuntimeException("Mocked RuntimeException"));

        assertThrows(RuntimeException.class, () -> {
            SchemaUtils.createSchema(connectionSourceMock, "testSchema");
        });
    }
}
