package com.sap.scimono.client;

import com.sap.scimono.entity.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class GroupRequest_createGroup_0_0_Test {

    @Mock
    private SCIMResourceRequest<Group> resourceRequestDelegate;

    @Mock
    private Response mockResponse;

    @Mock
    private ActionResponseStatusConfig mockConfig;

    @Mock
    private WebTarget mockWebTarget;

    @Mock
    private SCIMRequest mockScimRequest;

    private GroupRequest groupRequest;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        groupRequest = new GroupRequest(mockWebTarget, mockScimRequest);
        when(mockConfig.isSuccess(Response.Status.CREATED.getStatusCode())).thenReturn(true);
        when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(resourceRequestDelegate.createResource(any(Group.class))).thenReturn(SCIMResponse.fromEntity(new Group.Builder().setDisplayName("Test Group").build(), mockResponse, mockConfig));
    }

    @Test
    public void testCreateGroup() {
        Group group = new Group.Builder().setDisplayName("Test Group").build();
        SCIMResponse<Group> response = groupRequest.createGroup(group);

        assertNotNull(response);
        assertNotNull(response.get());
    }
}
