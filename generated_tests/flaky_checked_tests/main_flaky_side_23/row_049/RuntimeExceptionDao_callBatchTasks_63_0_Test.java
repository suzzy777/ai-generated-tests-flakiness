package com.j256.ormlite.dao;

import java.util.concurrent.Callable;
import org.junit.jupiter.api.function.Executable;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

public class RuntimeExceptionDao_callBatchTasks_63_0_Test {

    private Dao<Object, Object> mockDao;

    private RuntimeExceptionDao<Object, Object> runtimeExceptionDao;

    @BeforeEach
    public void setUp() {
        mockDao = mock(Dao.class);
        runtimeExceptionDao = new RuntimeExceptionDao<>(mockDao);
    }

    @Test
    public void testCallBatchTasksSuccess() throws Exception {
        Callable<String> callable = () -> "Success";
        when(mockDao.callBatchTasks(callable)).thenReturn("Success");
        String result = runtimeExceptionDao.callBatchTasks(callable);
        assertEquals("Success", result);
        verify(mockDao).callBatchTasks(callable);
    }

    @Test
    public void testCallBatchTasksThrowsException() throws Exception {
        Callable<String> callable = () -> "Failure";
        when(mockDao.callBatchTasks(callable)).thenThrow(new SQLException("SQL Exception"));
        Executable executable = () -> runtimeExceptionDao.callBatchTasks(callable);
        RuntimeException exception = assertThrows(RuntimeException.class, executable);
        assertEquals("java.sql.SQLException: SQL Exception", exception.getCause().toString());
        verify(mockDao).callBatchTasks(callable);
    }
}
