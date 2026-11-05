package com.sap.scimono.client;

import com.sap.scimono.client.SCIMRequest;
import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.config.ServiceProviderConfig;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import static com.sap.scimono.client.ResourceAction.GET_SINGLE;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.sap.scimono.api.API.SERVICE_PROVIDER_CONFIG;

public class ServiceProviderConfigurationRequest_readServiceProviderConfiguration_0_0_Test {

    private WebTarget mockWebTarget;

    private SCIMRequest mockScimRequest;

    private ServiceProviderConfigurationRequest serviceProviderConfigurationRequest;

    @BeforeEach
    public void setUp() {
        mockWebTarget = mock(WebTarget.class);
        mockScimRequest = mock(SCIMRequest.class);
        serviceProviderConfigurationRequest = new ServiceProviderConfigurationRequest(mockWebTarget, mockScimRequest);
    }

    @Test
    public void testReadServiceProviderConfiguration() throws Exception {
        // Mock response object
        Response mockResponse = mock(Response.class);
        when(mockScimRequest.get(any(WebTarget.class))).thenReturn(mockResponse);
        // Mock ActionResponseStatusConfig retrieval
        when(mockScimRequest.getScimActionResponseStatusConfig(GET_SINGLE)).thenReturn(null);
        // Use reflection to invoke the private method
        Method method = ServiceProviderConfigurationRequest.class.getDeclaredMethod("readServiceProviderConfiguration");
        method.setAccessible(true);
        // Invoke the method and assert the result
        SCIMResponse<ServiceProviderConfig> result = (SCIMResponse<ServiceProviderConfig>) method.invoke(serviceProviderConfigurationRequest);
        assertNotNull(result);
    }
}
