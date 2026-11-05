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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class GroupRequest_createGroup_0_0_Test {
    @Mock
    private SCIMResourceRequest<Group> resourceRequestDelegate;

    @Mock
    private WebTarget mockWebTarget;

    @Mock
    private SCIMRequest mockScimRequest;

    @InjectMocks
    private GroupRequest groupRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        groupRequest = new GroupRequest(mockWebTarget, mockScimRequest);
    }

    @Test
    void testCreateGroup() {
        Group group = new Group.Builder().setDisplayName("Test Group").build();
        Response mockResponse = mock(Response.class);
        SCIMResponse<Group> expectedResponse = SCIMResponse.newInstance(Group.class, mockResponse, null);

        when(resourceRequestDelegate.createResource(group)).thenReturn(expectedResponse);

        SCIMResponse<Group> actualResponse = groupRequest.createGroup(group);

        assertNotNull(actualResponse);
    }
}
