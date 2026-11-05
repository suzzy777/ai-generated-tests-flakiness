package com.sap.scimono.client;

import com.sap.scimono.entity.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GroupRequest_createGroup_0_0_Test {

    @Mock
    private SCIMResourceRequest<Group> resourceRequestDelegate;

    private GroupRequest groupRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        WebTarget mockWebTarget = mock(WebTarget.class);
        SCIMRequest mockScimRequest = mock(SCIMRequest.class);
        groupRequest = new GroupRequest(mockWebTarget, mockScimRequest);
    }

    @Test
    public void testCreateGroup() {
        Group group = new Group.Builder("displayName").setId("id").setExternalId("externalId").build();
        SCIMResponse<Group> mockResponse = createMockResponse();
        when(resourceRequestDelegate.createResource(any(Group.class))).thenReturn(mockResponse);

        SCIMResponse<Group> response = groupRequest.createGroup(group);

        assertNotNull(response);
    }

    private SCIMResponse<Group> createMockResponse() {
        return SCIMResponse.fromEntity(null, Response.ok().build(), null);
    }
}
