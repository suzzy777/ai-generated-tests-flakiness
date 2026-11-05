package com.j256.ormlite.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import java.sql.SQLException;
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
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.ObjectFactory;
import com.j256.ormlite.table.TableInfo;

public class RuntimeExceptionDao_countOf_72_0_Test {

    private RuntimeExceptionDao<Object, Object> runtimeExceptionDao;

    private Dao<Object, Object> mockDao;

    private PreparedQuery<Object> mockPreparedQuery;

    @BeforeEach
    public void setUp() {
        mockDao = mock(Dao.class);
        runtimeExceptionDao = new RuntimeExceptionDao<>(mockDao);
        mockPreparedQuery = mock(PreparedQuery.class);
    }

    @Test
    public void testCountOfSuccess() throws SQLException {
        long expectedCount = 5L;
        when(mockDao.countOf(mockPreparedQuery)).thenReturn(expectedCount);
        long result = runtimeExceptionDao.countOf(mockPreparedQuery);
        assertEquals(expectedCount, result);
        verify(mockDao, times(1)).countOf(mockPreparedQuery);
    }

    @Test
    public void testCountOfThrowsSQLException() throws SQLException {
        when(mockDao.countOf(mockPreparedQuery)).thenThrow(new SQLException("SQL Exception"));
        Exception exception = assertThrows(RuntimeException.class, () -> {
            runtimeExceptionDao.countOf(mockPreparedQuery);
        });
        assertTrue(exception.getCause() instanceof SQLException);
        verify(mockDao, times(1)).countOf(mockPreparedQuery);
    }
}
