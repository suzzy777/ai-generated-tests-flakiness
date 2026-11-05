package com.sap.scimono.client;

import com.sap.scimono.client.SCIMResourceRequest;
import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.Group;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.ws.rs.client.WebTarget;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.sap.scimono.api.API.GROUPS;
import static com.sap.scimono.client.query.ResourcePageQuery.indexPageQuery;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import com.sap.scimono.client.query.IdentityPageQuery;
import com.sap.scimono.client.query.IndexPageQuery;
import com.sap.scimono.entity.paging.PagedByIdentitySearchResult;
import com.sap.scimono.entity.paging.PagedByIndexSearchResult;
import com.sap.scimono.entity.patch.PatchBody;

@ExtendWith(MockitoExtension.class)
public class GroupRequest_createGroup_0_0_Test {

    private GroupRequest groupRequest;

    private SCIMResourceRequest<Group> mockResourceRequestDelegate;

    private WebTarget mockWebTarget;

    private SCIMRequest mockScimRequest;

    private SCIMResponse<Group> mockScimResponse;

    private java.lang.reflect.Field originalResourceRequestDelegateField;

    @BeforeEach
    public void setUp() {
        mockResourceRequestDelegate = mock(SCIMResourceRequest.class);
        mockWebTarget = mock(WebTarget.class);
        mockScimRequest = mock(SCIMRequest.class);
        mockScimResponse = mock(SCIMResponse.class);
        groupRequest = new GroupRequest(mockWebTarget, mockScimRequest);
        try {
            originalResourceRequestDelegateField = GroupRequest.class.getDeclaredField("resourceRequestDelegate");
            originalResourceRequestDelegateField.setAccessible(true);
            originalResourceRequestDelegateField.set(groupRequest, mockResourceRequestDelegate);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Error setting up the test: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        try {
            originalResourceRequestDelegateField.set(groupRequest, null);
        } catch (IllegalAccessException e) {
            fail("Error tearing down the test: " + e.getMessage());
        }
    }

    @Test
    public void testCreateGroup() {
        // Create a real instance using the appropriate constructor
        Group group = new Group.Builder("id").build();
        when(mockResourceRequestDelegate.createResource(group)).thenReturn(mockScimResponse);
        SCIMResponse<Group> response = groupRequest.createGroup(group);
        assertEquals(mockScimResponse, response);
        verify(mockResourceRequestDelegate).createResource(group);
    }
}
