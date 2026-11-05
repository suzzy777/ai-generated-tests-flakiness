package com.j256.ormlite.dao;

import java.sql.SQLException;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.misc.Supplier;
import com.j256.ormlite.stmt.ArgumentHolder;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.ObjectFactory;
import com.j256.ormlite.table.TableInfo;

public class RuntimeExceptionDao_createObjectInstance_95_0_Test {

    private RuntimeExceptionDao<Object, Object> runtimeExceptionDao;

    @Mock
    private Dao<Object, Object> daoMock;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        runtimeExceptionDao = new RuntimeExceptionDao<>(daoMock);
    }

    @Test
    public void testCreateObjectInstanceSuccess() throws SQLException {
        Object expectedObject = new Object();
        when(daoMock.createObjectInstance()).thenReturn(expectedObject);
        Object result = runtimeExceptionDao.createObjectInstance();
        assertEquals(expectedObject, result);
        verify(daoMock).createObjectInstance();
    }

    @Test
    public void testCreateObjectInstanceThrowsSQLException() throws SQLException {
        when(daoMock.createObjectInstance()).thenThrow(new SQLException("SQL exception"));
        Exception exception = assertThrows(RuntimeException.class, () -> {
            runtimeExceptionDao.createObjectInstance();
        });
        assertTrue(exception.getCause() instanceof SQLException);
        assertEquals("SQL exception", exception.getCause().getMessage());
        verify(daoMock).createObjectInstance();
    }
}
