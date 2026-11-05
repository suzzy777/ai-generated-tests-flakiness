package com.j256.ormlite.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.db.DatabaseType;

public class SchemaUtils_createSchema_0_0_Test extends BaseCoreTest {

    @Mock
    private ConnectionSource mockConnectionSource;
    @Mock
    private DatabaseType mockDatabaseType;
    @Mock
    private DatabaseConnection mockDatabaseConnection;

    private AutoCloseable closeable;

    @BeforeEach
    public void setUp() throws SQLException {
        closeable = MockitoAnnotations.openMocks(this);
        Mockito.when(mockConnectionSource.getDatabaseType()).thenReturn(mockDatabaseType);
        Mockito.when(mockConnectionSource.getReadWriteConnection(Mockito.anyString())).thenReturn(mockDatabaseConnection);
    }

    @AfterEach
    public void tearDown() {
        try {
            closeable.close();
        } catch (Exception e) {
            // Log or handle exception as needed
        }
    }

    @Test
    public void testCreateSchema() throws SQLException {
        int result = SchemaUtils.createSchema(mockConnectionSource, "test_schema");
        assertEquals(0, result, "The createSchema method should return 0 when no statements are executed.");
    }

    @Test
    public void testCreateSchemaThrowsSQLException() throws SQLException {
        Mockito.doThrow(new SQLException("Test exception")).when(mockDatabaseConnection).close();
        assertThrowsExactly(SQLException.class, () -> {
            SchemaUtils.createSchema(mockConnectionSource, "test_schema");
        }, "Expected createSchema to throw SQLException");
    }
}
