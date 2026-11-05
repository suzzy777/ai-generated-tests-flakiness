package com.sap.scimono.client;

import com.sap.scimono.client.SCIMResourceRequest;
import com.sap.scimono.client.SCIMResponse;
import com.sap.scimono.entity.Group;
import javax.ws.rs.client.WebTarget;
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
public class GroupRequest_createGroup_0_1_Test {

    @Mock
    private SCIMResourceRequest<Group> resourceRequestDelegate;

    @Mock
    private WebTarget webTarget;

    @Mock
    private SCIMResponse<Group> scimResponse;

    private GroupRequest groupRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        groupRequest = new GroupRequest(webTarget, null);
        // Using reflection to set the private field
        try {
            java.lang.reflect.Field resourceRequestDelegateField = GroupRequest.class.getDeclaredField("resourceRequestDelegate");
            resourceRequestDelegateField.setAccessible(true);
            resourceRequestDelegateField.set(groupRequest, resourceRequestDelegate);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreateGroup() {
        Group.Builder groupBuilder = new Group.Builder("id");
        Group group = groupBuilder.setDisplayName("testGroup").build();
        when(resourceRequestDelegate.createResource(ArgumentMatchers.any(Group.class))).thenReturn(scimResponse);
        SCIMResponse<Group> response = groupRequest.createGroup(group);
        assertEquals(scimResponse, response);
        Mockito.verify(resourceRequestDelegate).createResource(group);
    }
}
