package org.apache.cloudstack.network.tungsten.api.command;

import org.apache.cloudstack.network.tungsten.api.command.CreateTungstenFabricLogicalRouterCmd;
import org.apache.cloudstack.network.tungsten.service.TungstenService;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.ApiErrorCode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ZoneResponse;
import org.apache.cloudstack.network.tungsten.api.response.TungstenFabricLogicalRouterResponse;
import org.apache.log4j.Logger;
import javax.inject.Inject;

@RunWith(MockitoJUnitRunner.class)
public class CreateTungstenFabricLogicalRouterCmd_execute_0_0_Test {

    @Mock
    private TungstenService tungstenService;

    @InjectMocks
    private CreateTungstenFabricLogicalRouterCmd cmd;

    private static final long ZONE_ID = 1L;

    private static final String ROUTER_NAME = "testRouter";

    @Before
    public void setUp() throws Exception {
        // Use reflection to set private fields
        java.lang.reflect.Field zoneIdField = CreateTungstenFabricLogicalRouterCmd.class.getDeclaredField("zoneId");
        zoneIdField.setAccessible(true);
        zoneIdField.set(cmd, ZONE_ID);
        java.lang.reflect.Field nameField = CreateTungstenFabricLogicalRouterCmd.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(cmd, ROUTER_NAME);
    }

    @Test
    public void testExecuteFailure() throws Exception {
        when(tungstenService.createRoutingLogicalRouter(ZONE_ID, null, ROUTER_NAME)).thenReturn(null);
        try {
            cmd.execute();
            fail("Expected ServerApiException");
        } catch (ServerApiException e) {
            assertEquals(ApiErrorCode.INTERNAL_ERROR, e.getErrorCode());
            assertEquals("Failed to create Tungsten-Fabric logical router", e.getDescription());
        }
        verify(tungstenService, times(1)).createRoutingLogicalRouter(ZONE_ID, null, ROUTER_NAME);
    }
}
