package com.sap.scimono.client;

import com.sap.scimono.entity.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.WebTarget;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroupRequest_createGroup_0_0_Test {

    @Mock
    private SCIMResourceRequest<Group> resourceRequestDelegate;

    private GroupRequest groupRequest;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        groupRequest = new GroupRequest(mock(WebTarget.class), mock(SCIMRequest.class));
        Field field = GroupRequest.class.getDeclaredField("resourceRequestDelegate");
        field.setAccessible(true);
        field.set(groupRequest, resourceRequestDelegate);
    }

    @Test
    void testCreateGroup() {
        Group group = new Group.Builder("Test Group").build();

        SCIMResponse<Group> expectedResponse = mock(SCIMResponse.class);
        when(resourceRequestDelegate.createResource(any(Group.class))).thenReturn(expectedResponse);

        SCIMResponse<Group> response = groupRequest.createGroup(group);

        assertNotNull(response);
    }
}
