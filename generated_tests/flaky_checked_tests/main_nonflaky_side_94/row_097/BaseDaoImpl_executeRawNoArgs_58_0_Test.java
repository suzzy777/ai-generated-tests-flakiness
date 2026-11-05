package com.j256.ormlite.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;
import java.lang.reflect.Field;
import java.sql.SQLException;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.misc.Supplier;
import com.j256.ormlite.stmt.ArgumentHolder;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.SelectIterator;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.ObjectFactory;

public class BaseDaoImpl_executeRawNoArgs_58_0_Test {

    private BaseDaoImpl<Object, Object> baseDaoImpl;

    private ConnectionSource connectionSource;

    private StatementExecutor<Object, Object> statementExecutor;

    private TableInfo<Object, Object> tableInfo;

    private DatabaseConnection databaseConnection;

    @BeforeEach
    public void setUp() throws Exception {
        connectionSource = mock(ConnectionSource.class);
        statementExecutor = mock(StatementExecutor.class);
        tableInfo = mock(TableInfo.class);
        databaseConnection = mock(DatabaseConnection.class);
        baseDaoImpl = Mockito.mock(BaseDaoImpl.class, Mockito.CALLS_REAL_METHODS);
        Field connectionSourceField = BaseDaoImpl.class.getDeclaredField("connectionSource");
        connectionSourceField.setAccessible(true);
        connectionSourceField.set(baseDaoImpl, connectionSource);
        Field statementExecutorField = BaseDaoImpl.class.getDeclaredField("statementExecutor");
        statementExecutorField.setAccessible(true);
        statementExecutorField.set(baseDaoImpl, statementExecutor);
        Field tableInfoField = BaseDaoImpl.class.getDeclaredField("tableInfo");
        tableInfoField.setAccessible(true);
        tableInfoField.set(baseDaoImpl, tableInfo);
        Field initializedField = BaseDaoImpl.class.getDeclaredField("initialized");
        initializedField.setAccessible(true);
        initializedField.set(baseDaoImpl, true);
        when(connectionSource.getReadWriteConnection(anyString())).thenReturn(databaseConnection);
        when(tableInfo.getTableName()).thenReturn("testTable");
    }

    @Test
    public void testExecuteRawNoArgsSuccess() throws SQLException {
        String statement = "RAW SQL STATEMENT";
        int expectedResult = 1;
        when(statementExecutor.executeRawNoArgs(databaseConnection, statement)).thenReturn(expectedResult);
        int result = baseDaoImpl.executeRawNoArgs(statement);
        assertEquals(expectedResult, result);
        verify(statementExecutor).executeRawNoArgs(databaseConnection, statement);
        verify(connectionSource).releaseConnection(databaseConnection);
    }

    @Test
    public void testExecuteRawNoArgsSQLException() throws SQLException {
        String statement = "RAW SQL STATEMENT";
        when(statementExecutor.executeRawNoArgs(databaseConnection, statement)).thenThrow(new SQLException("SQL Error"));
        SQLException exception = assertThrows(SQLException.class, () -> baseDaoImpl.executeRawNoArgs(statement));
        assertEquals("Could not run raw execute statement RAW SQL STATEMENT", exception.getMessage());
        verify(connectionSource).releaseConnection(databaseConnection);
    }

    @Test
    public void testExecuteRawNoArgsNotInitialized() throws Exception {
        Field initializedField = BaseDaoImpl.class.getDeclaredField("initialized");
        initializedField.setAccessible(true);
        initializedField.set(baseDaoImpl, false);
        assertThrows(IllegalStateException.class, () -> baseDaoImpl.executeRawNoArgs("RAW SQL STATEMENT"));
    }
}
