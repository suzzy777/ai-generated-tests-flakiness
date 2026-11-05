package com.sap.scimono.client;

import static org.mockito.ArgumentMatchers.any;
import javax.ws.rs.client.WebTarget;
import com.sap.scimono.client.SCIMRequest;
import com.sap.scimono.client.SCIMResourceRequest;
import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.Group;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.sap.scimono.api.API.GROUPS;
import static com.sap.scimono.client.query.ResourcePageQuery.indexPageQuery;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import com.sap.scimono.client.query.IdentityPageQuery;
import com.sap.scimono.client.query.IndexPageQuery;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class GroupRequest_createGroup_0_2_Test {

    private GroupRequest groupRequest;

    private SCIMResourceRequest<Group> mockResourceRequestDelegate;

    private SCIMRequest mockScimRequest;

    private WebTarget mockWebTarget;

    @BeforeEach
    public void setUp() {
        // Mock dependencies
        mockResourceRequestDelegate = mock(SCIMResourceRequest.class);
        mockScimRequest = mock(SCIMRequest.class);
        mockWebTarget = mock(WebTarget.class);
        // Use reflection to set the private final field
        groupRequest = new GroupRequest(mockWebTarget, mockScimRequest);
        setPrivateField(groupRequest, "resourceRequestDelegate", mockResourceRequestDelegate);
    }

    @Test
    public void testCreateGroup() {
        // Use a real instance instead of a mock
        Group mockGroup = new Group.Builder("mockGroupId").build();
        SCIMResponse<Group> expectedResponse = mock(SCIMResponse.class);
        when(mockResourceRequestDelegate.createResource(any(Group.class))).thenReturn(expectedResponse);
        SCIMResponse<Group> actualResponse = groupRequest.createGroup(mockGroup);
        assertEquals(expectedResponse, actualResponse);
        verify(mockResourceRequestDelegate).createResource(mockGroup);
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
