package com.j256.ormlite.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;

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
        when(connectionSource.getReadWriteConnection("testSchema")).thenReturn(databaseConnection);
        when(databaseType.isCreateSchemaReturnsNegative()).thenReturn(false);
        when(databaseType.isCreateSchemaReturnsZero()).thenReturn(true);
        when(databaseConnection.compileStatement("create", StatementType.CREATE, null, 0)).thenReturn(compiledStatement);
        when(databaseConnection.executeStatement("create", 0)).thenReturn(0);
    }

    @Test
    public void testCreateSchema() throws SQLException {
        String schemaName = "testSchema";

        int result = SchemaUtils.createSchema(connectionSource, schemaName);
        assertEquals(0, result, "The schema creation should return 0 as it's a mock setup.");
    }
}
