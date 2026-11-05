package com.sap.scimono.client;

import javax.ws.rs.client.WebTarget;
import com.sap.scimono.client.SCIMRequest;
import com.sap.scimono.client.SCIMResourceRequest;
import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.Group;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
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

public class GroupRequest_createGroup_0_2_Test {

    @Mock
    private WebTarget mockWebTarget;

    @Mock
    private SCIMRequest mockSCIMRequest;

    @Mock
    private SCIMResourceRequest<Group> mockResourceRequestDelegate;

    private GroupRequest groupRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        groupRequest = new GroupRequest(mockWebTarget, mockSCIMRequest);
        // Use reflection to set the private field resourceRequestDelegate
        try {
            java.lang.reflect.Field field = GroupRequest.class.getDeclaredField("resourceRequestDelegate");
            field.setAccessible(true);
            field.set(groupRequest, mockResourceRequestDelegate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreateGroup() {
        // Use reflection to create a Group instance using the Builder
        Group group = new Group.Builder("groupId").build();
        SCIMResponse<Group> expectedResponse = mock(SCIMResponse.class);
        when(mockResourceRequestDelegate.createResource(group)).thenReturn(expectedResponse);
        SCIMResponse<Group> actualResponse = groupRequest.createGroup(group);
        assertEquals(expectedResponse, actualResponse);
        verify(mockResourceRequestDelegate).createResource(group);
    }
}
