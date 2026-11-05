package com.sap.scimono.client;

import javax.ws.rs.client.WebTarget;
import com.sap.scimono.client.SCIMRequest;
import com.sap.scimono.client.SCIMResourceRequest;
import com.sap.scimono.entity.Group;
import com.sap.scimono.client.SCIMResponse;
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
public class GroupRequest_createGroup_0_0_Test {

    private GroupRequest groupRequest;

    private SCIMResourceRequest<Group> resourceRequestDelegateMock;

    private SCIMRequest scimRequestMock;

    private WebTarget webTargetMock;

    private java.lang.reflect.Field originalField;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        webTargetMock = mock(WebTarget.class);
        scimRequestMock = mock(SCIMRequest.class);
        resourceRequestDelegateMock = mock(SCIMResourceRequest.class);
        groupRequest = new GroupRequest(webTargetMock, scimRequestMock);
        originalField = GroupRequest.class.getDeclaredField("resourceRequestDelegate");
        originalField.setAccessible(true);
        setResourceRequestDelegate(groupRequest, resourceRequestDelegateMock);
    }

    @AfterEach
    public void tearDown() throws IllegalAccessException {
        // Restore the original field value (if needed)
        if (originalField != null) {
            originalField.setAccessible(true);
            originalField.set(groupRequest, null);
        }
    }

    @Test
    public void testCreateGroup() {
        Group mockGroup = createMockGroup();
        SCIMResponse<Group> expectedResponse = mock(SCIMResponse.class);
        when(resourceRequestDelegateMock.createResource(mockGroup)).thenReturn(expectedResponse);
        SCIMResponse<Group> actualResponse = groupRequest.createGroup(mockGroup);
        assertEquals(expectedResponse, actualResponse);
        verify(resourceRequestDelegateMock, times(1)).createResource(mockGroup);
    }

    private Group createMockGroup() {
        // Create a real Group object instead of mocking
        return new Group.Builder().setDisplayName("Test Group").build();
    }

    private void setResourceRequestDelegate(GroupRequest groupRequest, SCIMResourceRequest<Group> resourceRequestDelegate) {
        try {
            java.lang.reflect.Field field = GroupRequest.class.getDeclaredField("resourceRequestDelegate");
            field.setAccessible(true);
            field.set(groupRequest, resourceRequestDelegate);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
