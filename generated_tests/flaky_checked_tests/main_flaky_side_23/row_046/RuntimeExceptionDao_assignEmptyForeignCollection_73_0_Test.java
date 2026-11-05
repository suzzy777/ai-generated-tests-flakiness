package com.j256.ormlite.dao;

import java.sql.SQLException;
import org.junit.jupiter.api.function.Executable;
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

public class RuntimeExceptionDao_assignEmptyForeignCollection_73_0_Test {

    private Dao<Object, Object> mockDao;

    private RuntimeExceptionDao<Object, Object> runtimeExceptionDao;

    @BeforeEach
    public void setUp() {
        mockDao = mock(Dao.class);
        runtimeExceptionDao = new RuntimeExceptionDao<>(mockDao);
    }

    @Test
    public void testAssignEmptyForeignCollectionSuccess() throws SQLException {
        Object parent = new Object();
        String fieldName = "testField";
        // No exception should be thrown
        assertDoesNotThrow(() -> runtimeExceptionDao.assignEmptyForeignCollection(parent, fieldName));
        // Verify that the dao's method was called
        verify(mockDao).assignEmptyForeignCollection(parent, fieldName);
    }

    @Test
    public void testAssignEmptyForeignCollectionThrowsSQLException() throws SQLException {
        Object parent = new Object();
        String fieldName = "testField";
        // Simulate SQLException being thrown
        doThrow(new SQLException("Test SQL Exception")).when(mockDao).assignEmptyForeignCollection(parent, fieldName);
        // Execute and verify that RuntimeException is thrown
        Executable executable = () -> runtimeExceptionDao.assignEmptyForeignCollection(parent, fieldName);
        RuntimeException thrown = assertThrows(RuntimeException.class, executable);
        // Verify the cause of the thrown exception
        assertTrue(thrown.getCause() instanceof SQLException);
        assertEquals("Test SQL Exception", thrown.getCause().getMessage());
        // Verify that the dao's method was called
        verify(mockDao).assignEmptyForeignCollection(parent, fieldName);
    }
}
