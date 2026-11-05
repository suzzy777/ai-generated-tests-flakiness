package com.j256.ormlite.table;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;

public class SchemaUtils_createSchema_0_0_Test {

    private ConnectionSource connectionSource;
    private DatabaseConnection databaseConnection;
    private DatabaseType databaseType;

    @BeforeEach
    public void setUp() {
        connectionSource = Mockito.mock(ConnectionSource.class);
        databaseConnection = Mockito.mock(DatabaseConnection.class);
        databaseType = Mockito.mock(DatabaseType.class);
    }

    @Test
    public void testCreateSchema() throws Exception {
        Mockito.when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        Mockito.when(connectionSource.getReadWriteConnection(anyString())).thenReturn(databaseConnection);
        Mockito.doNothing().when(connectionSource).releaseConnection(databaseConnection);
        Mockito.when(databaseType.isCreateSchemaReturnsNegative()).thenReturn(false);
        Mockito.when(databaseType.isCreateSchemaReturnsZero()).thenReturn(false);

        // Mock the method executeStatement to return 0
        Mockito.when(databaseConnection.executeStatement(anyString(), anyInt())).thenReturn(0);

        // Use reflection to access the private method doStatements
        Method doStatementsMethod = SchemaUtils.class.getDeclaredMethod("doStatements", DatabaseConnection.class, String.class, java.util.Collection.class, boolean.class, boolean.class, boolean.class);
        doStatementsMethod.setAccessible(true);

        // Stub the private method doStatements to return 0
        Mockito.doReturn(0).when(doStatementsMethod.invoke(null, databaseConnection, "create", anyList(), anyBoolean(), anyBoolean(), anyBoolean()));

        int result = SchemaUtils.createSchema(connectionSource, "testSchema");

        assertEquals(0, result, "The schema creation should return 0 when no statements are executed.");
    }
}
