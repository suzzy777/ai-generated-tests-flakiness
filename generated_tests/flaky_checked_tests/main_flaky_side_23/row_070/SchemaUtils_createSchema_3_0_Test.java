package com.j256.ormlite.table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.DatabaseConnection;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Collection;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.misc.IOUtils;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseResults;

@ExtendWith(MockitoExtension.class)
public class SchemaUtils_createSchema_3_0_Test {

    @Mock
    private ConnectionSource connectionSourceMock;

    @Mock
    private DatabaseType databaseTypeMock;

    @Mock
    private DatabaseConnection databaseConnectionMock;

    @InjectMocks
    private SchemaUtils schemaUtils;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateSchemaSQLException() throws Exception {
        String schemaName = "testSchema";
        when(connectionSourceMock.getDatabaseType()).thenReturn(databaseTypeMock);
        when(connectionSourceMock.getReadWriteConnection(schemaName)).thenThrow(new SQLException("Connection error"));
        assertThrows(SQLException.class, () -> {
            SchemaUtils.createSchema(connectionSourceMock, schemaName);
        });
        verify(connectionSourceMock, never()).releaseConnection(databaseConnectionMock);
    }
}
