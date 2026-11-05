package com.sap.scimono.client;

import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class ActionResponseStatusConfig_isSuccess_1_0_Test {

    private ActionResponseStatusConfig config;

    private Collection<Response.Status> successStatuses;

    @BeforeEach
    public void setUp() {
        successStatuses = new ArrayList<>();
        successStatuses.add(Response.Status.OK);
        successStatuses.add(Response.Status.CREATED);
        config = new ActionResponseStatusConfig(successStatuses);
    }

    @Test
    public void testIsSuccessWithSuccessCode() throws Exception {
        assertTrue(config.isSuccess(Response.Status.OK.getStatusCode()));
        assertTrue(config.isSuccess(Response.Status.CREATED.getStatusCode()));
    }

    @Test
    public void testIsSuccessWithFailureCode() throws Exception {
        assertFalse(config.isSuccess(Response.Status.BAD_REQUEST.getStatusCode()));
        assertFalse(config.isSuccess(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void testIsSuccessWithPrivateMethod() throws Exception {
        Method isSuccessMethod = ActionResponseStatusConfig.class.getDeclaredMethod("isSuccess", Response.Status.class);
        isSuccessMethod.setAccessible(true);
        assertTrue((Boolean) isSuccessMethod.invoke(config, Response.Status.OK));
        assertFalse((Boolean) isSuccessMethod.invoke(config, Response.Status.BAD_REQUEST));
    }

    @Test
    public void testGetSuccessResponseStatuses() {
        Collection<Response.Status> statuses = config.getSuccessResponseStatuses();
        assertEquals(2, statuses.size());
        assertTrue(statuses.contains(Response.Status.OK));
        assertTrue(statuses.contains(Response.Status.CREATED));
    }
}
