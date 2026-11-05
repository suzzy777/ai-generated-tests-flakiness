package com.sap.scimono.client;

import javax.ws.rs.client.WebTarget;
import com.sap.scimono.client.SCIMRequest;
import com.sap.scimono.client.SCIMResourceRequest;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.client.SCIMResponse;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import javax.ws.rs.core.GenericType;
import static com.sap.scimono.api.API.SCHEMAS;

public class SchemaRequest_createSchema_0_1_Test {

    private SchemaRequest schemaRequest;

    private SCIMResourceRequest<Schema> mockResourceRequest;

    private SCIMRequest mockSCIMRequest;

    private WebTarget mockWebTarget;

    @BeforeEach
    public void setUp() {
        mockWebTarget = mock(WebTarget.class);
        mockSCIMRequest = mock(SCIMRequest.class);
        mockResourceRequest = mock(SCIMResourceRequest.class);
        schemaRequest = new SchemaRequest(mockWebTarget, mockSCIMRequest);
        setPrivateField(schemaRequest, "resourceRequestDelegate", mockResourceRequest);
    }

    @Test
    public void testCreateSchema() {
        Schema mockSchema = mock(Schema.class);
        SCIMResponse<Schema> mockResponse = mock(SCIMResponse.class);
        when(mockResourceRequest.createResource(mockSchema)).thenReturn(mockResponse);
        SCIMResponse<Schema> response = schemaRequest.createSchema(mockSchema);
        assertNotNull(response);
    }

    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
