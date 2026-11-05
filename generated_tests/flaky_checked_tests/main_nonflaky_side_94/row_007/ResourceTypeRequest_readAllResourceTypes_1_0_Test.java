package com.sap.scimono.client;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import com.sap.scimono.client.ResourceTypeRequest;
import com.sap.scimono.client.SCIMResourceRequest;
import com.sap.scimono.client.SCIMRequest;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.schema.ResourceType;
import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.client.RequestDetails;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.sap.scimono.api.API.RESOURCE_TYPES;

public class ResourceTypeRequest_readAllResourceTypes_1_0_Test {

    private SCIMResourceRequest<ResourceType> mockResourceRequestDelegate;

    private ResourceTypeRequest resourceTypeRequest;

    private SCIMResponse<PagedByIndexSearchResult<ResourceType>> mockResponse;

    @BeforeEach
    public void setUp() {
        mockResourceRequestDelegate = mock(SCIMResourceRequest.class);
        mockResponse = mock(SCIMResponse.class);
        resourceTypeRequest = new ResourceTypeRequest(mock(WebTarget.class), mock(SCIMRequest.class));
        setResourceRequestDelegate(resourceTypeRequest, mockResourceRequestDelegate);
    }

    @Test
    public void testReadAllResourceTypes() {
        when(mockResourceRequestDelegate.readMultipleResources(any(GenericType.class), eq(RequestDetails.DEFAULT))).thenReturn(mockResponse);
        SCIMResponse<PagedByIndexSearchResult<ResourceType>> response = resourceTypeRequest.readAllResourceTypes();
        assertNotNull(response);
        assertEquals(mockResponse, response);
        verify(mockResourceRequestDelegate).readMultipleResources(any(GenericType.class), eq(RequestDetails.DEFAULT));
    }

    private void setResourceRequestDelegate(ResourceTypeRequest resourceTypeRequest, SCIMResourceRequest<ResourceType> delegate) {
        try {
            java.lang.reflect.Field field = ResourceTypeRequest.class.getDeclaredField("resourceRequestDelegate");
            field.setAccessible(true);
            field.set(resourceTypeRequest, delegate);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
