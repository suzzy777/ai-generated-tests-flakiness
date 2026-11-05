package com.sap.scimono.client;

import com.sap.scimono.client.SchemaRequest;
import com.sap.scimono.client.SCIMResourceRequest;
import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.schema.Schema;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.sap.scimono.api.API.SCHEMAS;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SchemaRequest_readAllSchemas_2_0_Test {

    private SchemaRequest schemaRequest;

    private SCIMResourceRequest<Schema> resourceRequestDelegateMock;

    private WebTarget webTargetMock;

    private SCIMRequest scimRequestMock;

    @BeforeEach
    public void setUp() {
        webTargetMock = mock(WebTarget.class);
        scimRequestMock = mock(SCIMRequest.class);
        resourceRequestDelegateMock = mock(SCIMResourceRequest.class);
        schemaRequest = new SchemaRequest(webTargetMock, scimRequestMock);
        setPrivateField(schemaRequest, "resourceRequestDelegate", resourceRequestDelegateMock);
    }

    @Test
    public void testReadAllSchemas() {
        // Arrange
        PagedByIndexSearchResult<Schema> expectedResult = mock(PagedByIndexSearchResult.class);
        SCIMResponse<PagedByIndexSearchResult<Schema>> scimResponseMock = mock(SCIMResponse.class);
        when(scimResponseMock.get()).thenReturn(expectedResult);
        when(resourceRequestDelegateMock.readMultipleResources(any(GenericType.class), any())).thenReturn(scimResponseMock);
        // Act
        SCIMResponse<PagedByIndexSearchResult<Schema>> result = schemaRequest.readAllSchemas();
        // Assert
        assertNotNull(result);
        assertNotNull(result.get());
    }

    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
