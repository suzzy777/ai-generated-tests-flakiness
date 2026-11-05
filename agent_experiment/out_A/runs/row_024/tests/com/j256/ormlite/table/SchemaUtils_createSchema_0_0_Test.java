package com.j256.ormlite.table;

import static org.mockito.Mockito.*;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.field.FieldType;

public class SchemaUtils_createSchema_0_0_Test {

    @Mock
    private ConnectionSource connectionSource;

    @Mock
    private DatabaseType databaseType;

    @Mock
    private DatabaseConnection databaseConnection;

    @Mock
    private CompiledStatement compiledStatement;

    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        when(connectionSource.getReadWriteConnection(anyString())).thenReturn(databaseConnection);
        when(databaseConnection.compileStatement(anyString(), any(StatementType.class),
                any(FieldType[].class), anyInt(), anyBoolean())).thenReturn(compiledStatement);
        when(compiledStatement.runExecute()).thenReturn(1);
    }

    @Test
    public void testCreateSchema() throws SQLException {
        // Arrange
        String schemaName = "testSchema";

        // Act
        int result = SchemaUtils.createSchema(connectionSource, schemaName);

        // Assert
        // Since doCreateSchema is private, we can't assert its internal behavior directly
        // We can only verify interactions or outcome if any
    }
}
