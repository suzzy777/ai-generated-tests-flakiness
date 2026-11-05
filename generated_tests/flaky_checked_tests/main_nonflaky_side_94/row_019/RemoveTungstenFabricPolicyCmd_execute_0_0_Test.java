package org.apache.cloudstack.network.tungsten.api.command;

import org.apache.cloudstack.network.tungsten.api.command.RemoveTungstenFabricPolicyCmd;
import org.apache.cloudstack.network.tungsten.api.response.TungstenFabricPolicyResponse;
import org.apache.cloudstack.network.tungsten.service.TungstenService;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.ApiErrorCode;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import com.cloud.dc.DataCenter;
import net.juniper.tungsten.api.types.NetworkPolicy;
import org.apache.cloudstack.network.tungsten.model.TungstenNetworkPolicy;
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
import org.apache.log4j.Logger;
import javax.inject.Inject;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class RemoveTungstenFabricPolicyCmd_execute_0_0_Test {

    @Mock
    private TungstenService tungstenService;

    @InjectMocks
    private RemoveTungstenFabricPolicyCmd cmd;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = RemoveTungstenFabricPolicyCmd.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(cmd, value);
    }

    @Test
    public void testExecuteSuccess() throws Exception {
        // Arrange
        TungstenFabricPolicyResponse response = mock(TungstenFabricPolicyResponse.class);
        when(tungstenService.removeTungstenPolicy(anyLong(), anyString(), anyString())).thenReturn(response);
        setPrivateField("zoneId", 1L);
        setPrivateField("networkUuid", "network-uuid");
        setPrivateField("policyUuid", "policy-uuid");
        // Act
        cmd.execute();
        // Assert
        verify(tungstenService, times(1)).removeTungstenPolicy(1L, "network-uuid", "policy-uuid");
        verify(response, times(1)).setResponseName(anyString());
    }

    @Test
    public void testExecuteFailure() throws Exception {
        // Arrange
        when(tungstenService.removeTungstenPolicy(anyLong(), anyString(), anyString())).thenReturn(null);
        setPrivateField("zoneId", 1L);
        setPrivateField("networkUuid", "network-uuid");
        setPrivateField("policyUuid", "policy-uuid");
        // Act & Assert
        try {
            cmd.execute();
            fail("Expected ServerApiException to be thrown");
        } catch (ServerApiException e) {
            assertEquals(ApiErrorCode.INTERNAL_ERROR, e.getErrorCode());
            assertEquals("Failed to remove Tungsten-Fabric policy", e.getDescription());
        }
        verify(tungstenService, times(1)).removeTungstenPolicy(1L, "network-uuid", "policy-uuid");
    }
}
