package com.sap.scimono.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.WebTarget;

import com.sap.scimono.entity.Group;

class GroupRequest_createGroup_0_0_Test {

    @Mock
    private SCIMResourceRequest<Group> resourceRequestDelegate;
    @Mock
    private WebTarget mockWebTarget;
    @Mock
    private SCIMRequest mockScimRequest;

    private GroupRequest groupRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        groupRequest = new GroupRequest(mockWebTarget, mockScimRequest);
        // Use reflection to set the private field
        java.lang.reflect.Field field;
        try {
            field = GroupRequest.class.getDeclaredField("resourceRequestDelegate");
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

        // Set up the mock behavior
        when(resourceRequestDelegate.createResource(group)).thenReturn(expectedResponse);

        // Call the method under test
        SCIMResponse<Group> actualResponse = groupRequest.createGroup(group);

        // Assert the results
        assertEquals(expectedResponse, actualResponse);
    }
}
