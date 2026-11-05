package com.j256.ormlite.table;

import java.sql.SQLException;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.SchemaUtils;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.misc.IOUtils;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseResults;

public class SchemaUtils_createSchema_3_0_Test {

    @Mock
    private ConnectionSource connectionSource;

    @Mock
    private DatabaseType databaseType;

    @Mock
    private DatabaseConnection databaseConnection;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateSchemaThrowsSQLException() throws Exception {
        String schemaName = "testSchema";
        when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        when(connectionSource.getReadWriteConnection(schemaName)).thenThrow(new SQLException("Connection error"));
        assertThrows(SQLException.class, () -> {
            SchemaUtils.createSchema(connectionSource, schemaName);
        });
        verify(connectionSource, never()).releaseConnection(any());
    }
}
