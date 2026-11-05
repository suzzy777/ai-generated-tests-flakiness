package com.sap.scimono.client;

import javax.ws.rs.client.WebTarget;
import com.sap.scimono.client.ResourceTypeRequest;
import com.sap.scimono.client.SCIMRequest;
import com.sap.scimono.client.SCIMResourceRequest;
import com.sap.scimono.entity.schema.ResourceType;
import com.sap.scimono.client.SCIMResponse;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import javax.ws.rs.core.GenericType;
import static com.sap.scimono.api.API.RESOURCE_TYPES;

public class ResourceTypeRequest_readSingleResourceType_0_1_Test {

    private ResourceTypeRequest resourceTypeRequest;

    private SCIMResourceRequest<ResourceType> resourceRequestDelegateMock;

    @BeforeEach
    public void setUp() {
        WebTarget webTargetMock = mock(WebTarget.class);
        SCIMRequest scimRequestMock = mock(SCIMRequest.class);
        resourceRequestDelegateMock = mock(SCIMResourceRequest.class);
        resourceTypeRequest = new ResourceTypeRequest(webTargetMock, scimRequestMock);
        // Use reflection to set the private field resourceRequestDelegate
        setPrivateField(resourceTypeRequest, "resourceRequestDelegate", resourceRequestDelegateMock);
    }

    @Test
    public void testReadSingleResourceType() {
        String resourceId = "testResourceId";
        SCIMResponse<ResourceType> expectedResponse = mock(SCIMResponse.class);
        when(resourceRequestDelegateMock.readSingleResource(Mockito.eq(resourceId), Mockito.any())).thenReturn(expectedResponse);
        SCIMResponse<ResourceType> actualResponse = resourceTypeRequest.readSingleResourceType(resourceId);
        assertNotNull(actualResponse);
    }

    private void setPrivateField(Object targetObject, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = targetObject.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(targetObject, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
