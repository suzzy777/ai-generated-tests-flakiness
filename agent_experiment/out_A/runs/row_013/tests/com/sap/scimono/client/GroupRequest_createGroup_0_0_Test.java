package com.sap.scimono.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.client.WebTarget;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.scimono.entity.Group;

class GroupRequest_createGroup_0_0_Test {

    private SCIMResourceRequest<Group> resourceRequestDelegateMock;
    private GroupRequest groupRequest;

    @BeforeEach
    void setUp() {
        resourceRequestDelegateMock = mock(SCIMResourceRequest.class);
        groupRequest = new GroupRequest(mock(WebTarget.class), mock(SCIMRequest.class));
        // Using reflection to set the private final field
        try {
            java.lang.reflect.Field field = GroupRequest.class.getDeclaredField("resourceRequestDelegate");
            field.setAccessible(true);
            field.set(groupRequest, resourceRequestDelegateMock);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testCreateGroup() {
        Group group = new Group.Builder().setDisplayName("Test Group").build();
        SCIMResponse<Group> expectedResponse = mock(SCIMResponse.class);

        when(resourceRequestDelegateMock.createResource(any(Group.class))).thenReturn(expectedResponse);

        SCIMResponse<Group> actualResponse = groupRequest.createGroup(group);

        assertEquals(expectedResponse, actualResponse);
    }
}
