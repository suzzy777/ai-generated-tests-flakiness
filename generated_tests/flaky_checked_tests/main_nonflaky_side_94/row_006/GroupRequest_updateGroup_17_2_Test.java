package com.sap.scimono.client;

import javax.ws.rs.client.WebTarget;
import com.sap.scimono.entity.Group;
import com.sap.scimono.entity.Group.Builder;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
public class GroupRequest_updateGroup_17_2_Test {

    private SCIMResourceRequest<Group> mockResourceRequestDelegate;

    private GroupRequest groupRequest;

    private WebTarget mockWebTarget;

    private SCIMRequest mockScimRequest;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        mockResourceRequestDelegate = mock(SCIMResourceRequest.class);
        mockWebTarget = mock(WebTarget.class);
        mockScimRequest = mock(SCIMRequest.class);
        groupRequest = new GroupRequest(mockWebTarget, mockScimRequest);
        // Use reflection to set the private field
        Field resourceRequestDelegateField = GroupRequest.class.getDeclaredField("resourceRequestDelegate");
        resourceRequestDelegateField.setAccessible(true);
        resourceRequestDelegateField.set(groupRequest, mockResourceRequestDelegate);
    }

    @Test
    public void testUpdateGroup() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Create a real instance using the Builder
        Group group = new Builder("id").setDisplayName("displayName").build();
        SCIMResponse<Group> mockResponse = mock(SCIMResponse.class);
        when(mockResourceRequestDelegate.updateResource(ArgumentMatchers.any(Group.class))).thenReturn(mockResponse);
        // Use reflection to invoke the private method
        Method updateGroupMethod = GroupRequest.class.getDeclaredMethod("updateGroup", Group.class);
        updateGroupMethod.setAccessible(true);
        SCIMResponse<Group> response = (SCIMResponse<Group>) updateGroupMethod.invoke(groupRequest, group);
        assertNotNull(response);
        verify(mockResourceRequestDelegate, times(1)).updateResource(group);
    }
}
