package com.j256.ormlite.table;

import java.sql.SQLException;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableInfo;
import com.j256.ormlite.db.DatabaseType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.misc.IOUtils;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SchemaUtils_createSchema_1_2_Test {

    @Mock
    private Dao<Object, Object> daoMock;

    @Mock
    private ConnectionSource connectionSourceMock;

    @Mock
    private TableInfo<Object, Object> tableInfoMock;

    @Mock
    private DatabaseType databaseTypeMock;

    @BeforeEach
    public void setUp() {
        when(daoMock.getConnectionSource()).thenReturn(connectionSourceMock);
        when(daoMock.getTableInfo()).thenReturn(tableInfoMock);
        when(tableInfoMock.getSchemaName()).thenReturn("testSchema");
    }

    @AfterEach
    public void tearDown() {
        reset(daoMock, connectionSourceMock, tableInfoMock, databaseTypeMock);
    }

    @Test
    public void testCreateSchemaWithSQLException() throws SQLException {
        // Arrange
        when(daoMock.getConnectionSource()).thenThrow(new RuntimeException("Test Exception"));
        // Act & Assert
        assertThrows(RuntimeException.class, () -> SchemaUtils.createSchema(daoMock));
    }
}
