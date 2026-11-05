package com.sap.scimono.client;

import com.sap.scimono.entity.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class GroupRequest_createGroup_0_0_Test {

    @Mock
    private SCIMResourceRequest<Group> resourceRequestDelegate;

    @Mock
    private WebTarget targetSystem;

    @Mock
    private SCIMRequest scimRequest;

    private GroupRequest groupRequest;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        groupRequest = new GroupRequest(targetSystem, scimRequest);
        Field field = GroupRequest.class.getDeclaredField("resourceRequestDelegate");
        field.setAccessible(true);
        field.set(groupRequest, resourceRequestDelegate);
    }

    @Test
    void testCreateGroup() {
        Group group = new Group.Builder().setId("test-id").build();
        SCIMResponse<Group> mockResponse = SCIMResponse.fromEntity(group, Response.ok().build(), null);

        when(resourceRequestDelegate.createResource(any(Group.class))).thenReturn(mockResponse);

        SCIMResponse<Group> response = groupRequest.createGroup(group);

        assertNotNull(response);
        assertNotNull(response.get());
    }
}
