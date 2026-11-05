package com.sap.scimono.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sap.scimono.entity.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.WebTarget;

class GroupRequest_createGroup_0_0_Test {

    @Mock
    private SCIMResourceRequest<Group> resourceRequestDelegate;

    private GroupRequest groupRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        WebTarget mockWebTarget = mock(WebTarget.class);
        SCIMRequest mockScimRequest = mock(SCIMRequest.class);
        groupRequest = new GroupRequest(mockWebTarget, mockScimRequest);

        // Use reflection to set the private field
        try {
            java.lang.reflect.Field field = GroupRequest.class.getDeclaredField("resourceRequestDelegate");
            field.setAccessible(true);
            field.set(groupRequest, resourceRequestDelegate);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testCreateGroup() {
        Group group = new Group.Builder().setDisplayName("Test Group").build();
        SCIMResponse<Group> expectedResponse = mock(SCIMResponse.class);

        when(resourceRequestDelegate.createResource(any(Group.class))).thenReturn(expectedResponse);

        SCIMResponse<Group> actualResponse = groupRequest.createGroup(group);

        assertEquals(expectedResponse, actualResponse);
    }
}
