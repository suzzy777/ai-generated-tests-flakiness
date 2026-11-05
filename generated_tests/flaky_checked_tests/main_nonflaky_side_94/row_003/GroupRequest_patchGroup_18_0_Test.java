package com.sap.scimono.client;

import javax.ws.rs.client.WebTarget;
import com.sap.scimono.entity.patch.PatchBody;
import com.sap.scimono.client.SCIMRequest;
import com.sap.scimono.client.SCIMResourceRequest;
import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.Group;
import java.lang.reflect.Field;
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

public class GroupRequest_patchGroup_18_0_Test {

    private GroupRequest groupRequest;

    private SCIMResourceRequest<Group> mockResourceRequestDelegate;

    private WebTarget mockWebTarget;

    private SCIMRequest mockScimRequest;

    @BeforeEach
    public void setUp() throws Exception {
        mockWebTarget = mock(WebTarget.class);
        mockScimRequest = mock(SCIMRequest.class);
        mockResourceRequestDelegate = mock(SCIMResourceRequest.class);
        groupRequest = new GroupRequest(mockWebTarget, mockScimRequest);
        // Use reflection to set the private field 'resourceRequestDelegate'
        Field field = GroupRequest.class.getDeclaredField("resourceRequestDelegate");
        field.setAccessible(true);
        field.set(groupRequest, mockResourceRequestDelegate);
    }

    @Test
    public void testPatchGroup() {
        PatchBody mockPatchBody = mock(PatchBody.class);
        String groupId = "testGroupId";
        SCIMResponse<Void> expectedResponse = mock(SCIMResponse.class);
        when(mockResourceRequestDelegate.patchResource(mockPatchBody, groupId)).thenReturn(expectedResponse);
        SCIMResponse<Void> actualResponse = groupRequest.patchGroup(mockPatchBody, groupId);
        assertEquals(expectedResponse, actualResponse);
        ArgumentCaptor<PatchBody> patchBodyCaptor = ArgumentCaptor.forClass(PatchBody.class);
        ArgumentCaptor<String> groupIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockResourceRequestDelegate).patchResource(patchBodyCaptor.capture(), groupIdCaptor.capture());
        assertEquals(mockPatchBody, patchBodyCaptor.getValue());
        assertEquals(groupId, groupIdCaptor.getValue());
    }
}
