package com.sap.scimono.client;

import com.sap.scimono.client.SchemaRequest;
import com.sap.scimono.client.SCIMResourceRequest;
import com.sap.scimono.client.SCIMRequest;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.client.SCIMResponse;
import javax.ws.rs.client.WebTarget;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import javax.ws.rs.core.GenericType;
import static com.sap.scimono.api.API.SCHEMAS;

public class SchemaRequest_readSingleSchema_1_0_Test {

    private SchemaRequest schemaRequest;

    private SCIMResourceRequest<Schema> resourceRequestDelegateMock;

    @BeforeEach
    public void setUp() {
        WebTarget mockWebTarget = Mockito.mock(WebTarget.class);
        SCIMRequest mockScimRequest = Mockito.mock(SCIMRequest.class);
        resourceRequestDelegateMock = Mockito.mock(SCIMResourceRequest.class);
        schemaRequest = new SchemaRequest(mockWebTarget, mockScimRequest);
        setPrivateField(schemaRequest, "resourceRequestDelegate", resourceRequestDelegateMock);
    }

    @Test
    public void testReadSingleSchema() {
        String schemaUrn = "urn:example:schema";
        SCIMResponse<Schema> mockResponse = Mockito.mock(SCIMResponse.class);
        when(resourceRequestDelegateMock.readSingleResource(eq(schemaUrn), any())).thenReturn(mockResponse);
        SCIMResponse<Schema> response = schemaRequest.readSingleSchema(schemaUrn);
        assertNotNull(response);
    }

    private void setPrivateField(Object targetObject, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = targetObject.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(targetObject, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
