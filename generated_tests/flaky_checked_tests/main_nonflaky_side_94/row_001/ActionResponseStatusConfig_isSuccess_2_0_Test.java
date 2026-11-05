package com.sap.scimono.client;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class ActionResponseStatusConfig_isSuccess_2_0_Test {

    private ActionResponseStatusConfig actionResponseStatusConfig;

    @BeforeEach
    public void setUp() {
        Collection<Response.Status> successStatuses = new HashSet<>(Arrays.asList(Response.Status.OK, Response.Status.CREATED, Response.Status.ACCEPTED));
        actionResponseStatusConfig = new ActionResponseStatusConfig(successStatuses);
    }

    @Test
    public void testIsSuccess_withSuccessStatus() {
        assertTrue(actionResponseStatusConfig.isSuccess(Response.Status.OK));
        assertTrue(actionResponseStatusConfig.isSuccess(Response.Status.CREATED));
        assertTrue(actionResponseStatusConfig.isSuccess(Response.Status.ACCEPTED));
    }

    @Test
    public void testIsSuccess_withNonSuccessStatus() {
        assertFalse(actionResponseStatusConfig.isSuccess(Response.Status.NOT_FOUND));
        assertFalse(actionResponseStatusConfig.isSuccess(Response.Status.INTERNAL_SERVER_ERROR));
    }
}
